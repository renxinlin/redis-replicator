package com.renxl.rotter.sel;

import com.google.common.collect.Queues;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.RedisURI;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.event.*;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.constants.Constants;
import com.renxl.rotter.rpcclient.events.RelpInfoResponse;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import static com.renxl.rotter.config.CompomentManager.getInstance;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 19:12
 */
@Slf4j
public class DefaultSelector extends Selector {


    SelectorParam param;
    Replicator r;
    int retry = 0;
    ThreadPoolExecutor executor;
    /**
     * 承接redisio线程
     */
    RingBuffer<SelectorEvent> ringBuffer;

    public DefaultSelector(SelectorParam param) {
        this.param = param;
        this.param = param;
        // 根据并行数创建滑动窗口大小
        CompomentManager.getInstance().getWindowManagerWatcher().initPipelined(param.getPipelineId(), param.getParallelism());
        executor = new ThreadPoolExecutor(1, 1, 300, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(0),
                new DefaultThreadFactory("selector-pipelineId-" + param.getPipelineId()),
                new ThreadPoolExecutor.CallerRunsPolicy());


    }

    private static String buildMasterAddress(String redisUrl, Jedis jedis) {
        String replicationInfo = jedis.info("replication");
        String[] redisInfo = replicationInfo.split("\r\n");
        String roleInfo = redisInfo[1].split(":")[1];
        String master = roleInfo.equals("master") ? null : redisInfo[2].split(":")[1];
        master = master == null ? redisUrl : master;
        return master;
    }

    @Override
    public void open()     {
        executor.execute(()->{
            retry = 0;
            int retriesFromRedisServerInfo = r.getConfiguration().getRetriesFromRedisServerInfo();
            while (retry < retriesFromRedisServerInfo) {
                try {
                    retry++;
                    // TODO 同步检查
                    r.open();
                    log.info("open times " + retry);
                } catch (IOException e) {
                    // 构建同步信息
                    sync();
                    // 开始aof rdb 复制
                    open();
                }
            }
        });



    }

    @Override
    public void sync() {

        // disruptor 生产者信息
        Disruptor<SelectorEvent> disruptor = new Disruptor<>(
                SelectorEvent::new,
                1024 * 1024,
                Executors.defaultThreadFactory(),
                // 单生产
                ProducerType.SINGLE,
                new YieldingWaitStrategy()
        );
        // 单消费
        disruptor.handleEventsWith(new RotterSelectorEventHandler());
        disruptor.start();
        // 每次aof异常 则需要重置ringbuffer
        ringBuffer = disruptor.getRingBuffer();


        // 获取新的主从配置信息
        String sourceUri = param.getSourceRedises();
        String redisUrl = sourceUri.split(Constants.MULT_NODE_SPLIT)[0];
        Jedis jedis = new Jedis(sourceUri);
        String master = buildMasterAddress(redisUrl, jedis);
        jedis.close();

        // 通过manager获取复制进度信息
        RelpInfoResponse relpInfoResponse = getInstance().callSyncInfo(param.getPipelineId());
        sourceUri = "redis://" + master + "" + 6379 + "?verbose=yes&retries=10&replId=" + relpInfoResponse.getReplid() + "&replOffset=" + relpInfoResponse.getOffset();
        RedisURI suri = null;
        try {
            suri = new RedisURI(sourceUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            // 未来如果redis版本迭代  这里需要自定义新的命令解析器
            // 添加命令解析器
            r = dress(new RedisReplicator(suri));
        } catch (IOException e) {
            e.printStackTrace();
        }

        r.addEventListener(new EventListener() {
            /**
             *
             * ringbuffer 满了之后当前线程会阻塞
             * @param replicator
             * @param event
             */
            @Override
            public void onEvent(Replicator replicator, Event event) {

                if (event instanceof PreRdbSyncEvent) {
                    log.info("start rdb==>");
                }
                if (event instanceof DumpKeyValuePair) {
                    DumpKeyValuePair dkv = (DumpKeyValuePair) event;
                    rdb((DumpKeyValuePair) event);
                }
                if (event instanceof PostRdbSyncEvent) {
                    log.info("end rdb==>");
                }
                if (event instanceof PreCommandSyncEvent) {
                    log.info("start aof==>");

                }
                if (event instanceof DefaultCommand) {
                    aof((DefaultCommand) event);
                }

                if (event instanceof PostCommandSyncEvent) {
                    log.info("end aof==>");

                }
            }
        });

    }

    @Override
    public void aof(DefaultCommand event) {
        // disruptor 生产
        long sequence = ringBuffer.next();
        try {
            SelectorEvent selectorEvent = ringBuffer.get(sequence);
            selectorEvent.setDefaultCommand(event);
        } finally {
            // 生产
            ringBuffer.publish(sequence);
        }
    }

    @Override
    public void rdb(DumpKeyValuePair event) {
        long sequence = ringBuffer.next();
        try {
            SelectorEvent selectorEvent = ringBuffer.get(sequence);
            selectorEvent.setDumpKeyValuePair(event);
        } finally {
            // 生产
            ringBuffer.publish(sequence);
        }
    }



    /**
     * redis - disruptor 复制消费者
     */

    @Slf4j
    class RotterSelectorEventHandler implements EventHandler<SelectorEvent> {
        ArrayBlockingQueue<SelectorEvent> arrayBlockingQueue = new ArrayBlockingQueue(1024*1024);
        List buffer = new CopyOnWriteArrayList<>();
        @Override
        public void onEvent(SelectorEvent event, long sequence, boolean endOfBatch) {
            arrayBlockingQueue.add(event);
            // disputor 消费 批量发送到[多线程的]extractTask的batch buffer
            try {
                Queues.drain(arrayBlockingQueue, buffer, 10, 500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error(" thread interrupted");
                e.printStackTrace();
            }
            if(!buffer.isEmpty()){
                SelectorBatchEvent selectorBatchEvent = new SelectorBatchEvent();
                selectorBatchEvent.setSelectorEvent(buffer);
                // todo 设置滑动窗口递增序列号保障顺序
                selectorBatchEvent.setBatchId(-1L);
                CompomentManager.getInstance().getMetaManager().addEvent(param.getPipelineId(),selectorBatchEvent);
                buffer.clear();

            }
        }

    }

}
