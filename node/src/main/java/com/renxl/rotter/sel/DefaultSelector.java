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
import com.moilioncircle.redis.replicator.cmd.impl.*;
import com.moilioncircle.redis.replicator.event.*;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.constants.Constants;
import com.renxl.rotter.rpcclient.events.RelpInfoResponse;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    AtomicInteger currentDb = new AtomicInteger(0);
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
        /**
         * select一个线程 ack一个线程
         */
        executor = new ThreadPoolExecutor(2, 2, 300, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new DefaultThreadFactory("selector-pipelineId-" + param.getPipelineId()),
                new ThreadPoolExecutor.CallerRunsPolicy());


    }

    private static String buildMasterAddress(String redisUrl, Jedis jedis) {
        String replicationInfo = jedis.info("replication");
        String[] redisInfo = replicationInfo.split("\r\n");
        String roleInfo = redisInfo[1].split(":")[1];
        String master = roleInfo.equals("master") ? null : redisInfo[2].split(":")[1];
        master = master == null ? redisUrl.split(Constants.IP_PORT_SPLIT)[0] : master;
        return master;
    }

    @Override
    public void open() {
        executor.execute(() -> {
            retry = 0;
            int retriesFromRedisServerInfo = r.getConfiguration().getRetriesFromRedisServerInfo();
            while (retry < retriesFromRedisServerInfo) {
                try {
                    retry++;
                    // TODO 阻塞同步检查
                    r.open();
                    log.info("open times " + retry);
                } catch (IOException e) {
                    e.printStackTrace();
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
        String port = redisUrl.split(Constants.IP_PORT_SPLIT).length == 1 ? "6379" : redisUrl.split(Constants.IP_PORT_SPLIT)[1];
        Jedis jedis = new Jedis("redis://" + redisUrl);
        String master = buildMasterAddress(redisUrl, jedis);
        CompomentManager.getInstance().getMetaManager().addPipelineSourceMaster(param.getPipelineId(), master, port, null);
        jedis.close();

        // 通过manager获取复制进度信息
        RelpInfoResponse relpInfoResponse = param.getRelpInfoResponse();
        if (relpInfoResponse != null && StringUtils.isEmpty(relpInfoResponse.getReplid()) && StringUtils.isEmpty(relpInfoResponse.getOffset())) {
            sourceUri = "redis://" + master + ":" + port + "?verbose=yes&replId=" + relpInfoResponse.getReplid() + "&replOffset=" + relpInfoResponse.getOffset();
        } else {
            sourceUri = "redis://" + master + ":" + port + "?verbose=yes";
        }
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
                Boolean permit = getInstance().getMetaManager().isPermit(param.getPipelineId());
                while (!permit) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        log.warn("wait for manager  permit ");
                    }
                }
                if (event instanceof PreRdbSyncEvent) {
                    log.info("start rdb==>");
                }
                if (event instanceof KeyValuePair) {
                    DumpKeyValuePair dkv = (DumpKeyValuePair) event;
                    rdb((KeyValuePair) event);
                }
                if (event instanceof PostRdbSyncEvent) {
                    log.info("end rdb==>");
                }
                if (event instanceof PreCommandSyncEvent) {
                    log.info("start aof==>");

                }

                if (event instanceof SelectCommand) {
                    int index = ((SelectCommand) event).getIndex();
                    currentDb.set(index);
                    ((SelectCommand) event).setDbNumber(currentDb.get());
                    aof((AbstractCommand) event);

                }

                if (event instanceof PingCommand) {
                    return;
                }


                if (event instanceof ReplConfCommand) {
                    return;
                }
                if (event instanceof DefaultCommand) {
                    ((DefaultCommand) event).setDbNumber(currentDb.get());

                    aof((AbstractCommand) event);
                }

                if (event instanceof PostCommandSyncEvent) {
                    log.info("end aof==>");

                }
            }
        });

    }

    @Override
    public void close() {
        try {
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void aof(AbstractCommand event) {
        // disruptor 生产
        long sequence = ringBuffer.next();
        try {
            SelectorEvent selectorEvent = ringBuffer.get(sequence);
            selectorEvent.setAbstartCommand(event);
        } finally {
            // 生产
            ringBuffer.publish(sequence);
        }
    }

    @Override
    public void rdb(KeyValuePair event) {
        long sequence = ringBuffer.next();
        try {
            SelectorEvent selectorEvent = ringBuffer.get(sequence);
            selectorEvent.setKeyValuePair(event);
        } finally {
            // 生产
            ringBuffer.publish(sequence);
        }
    }


    /**
     * redis - disruptor 复制消费者
     */

    class RotterSelectorEventHandler implements EventHandler<SelectorEvent> {
        ArrayBlockingQueue<SelectorEvent> arrayBlockingQueue = new ArrayBlockingQueue(1024 * 1024);
        List buffer = new CopyOnWriteArrayList<>();

        @Override
        public void onEvent(SelectorEvent event, long sequence, boolean endOfBatch) {
            arrayBlockingQueue.add(event);
            // disputor 消费 批量发送到[多线程的]extractTask的batch buffer
            try {
                // todo 参数配置化
                Queues.drain(arrayBlockingQueue, buffer, 10, 500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // todo 堆栈打印指定行数工具
                log.error(" thread interrupted", e);
            }
            if (!buffer.isEmpty()) {
                Integer pipelineId = param.getPipelineId();
                WindowBuffer selectWindowBuffer = getInstance().getWindowManager().getSelectBuffer(pipelineId);
                SelectorBatchEvent selectorBatchEvent = new SelectorBatchEvent();
                selectorBatchEvent.setSelectorEvent(buffer);

                //  阻塞获取滑动窗口信息
                long batchId = selectWindowBuffer.get();

                selectorBatchEvent.setBatchId(batchId);
                CompomentManager.getInstance().getMetaManager().addEvent(pipelineId, selectorBatchEvent);
                buffer.clear();

                // 滑动窗口向下传递到Extract Task 通知e task 工作  etask 是核心 消费较慢  允许多线程 同时在load阶段通过滑动窗口序列号保障顺序性
                getInstance().getWindowManager().singleExtract(pipelineId, batchId);


            }
        }

    }

}
