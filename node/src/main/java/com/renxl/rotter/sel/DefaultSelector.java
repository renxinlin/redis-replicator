package com.renxl.rotter.sel;

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
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;

import static com.renxl.rotter.config.CompomentManager.getInstance;
import static org.apache.commons.lang.StringUtils.split;

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
    /**
     * 承接redisio线程
     */
    RingBuffer<SelectorEvent> ringBuffer ;
    public DefaultSelector(SelectorParam param) {
        this.param = param;
        // 根据并行数创建滑动窗口大小
        CompomentManager.getInstance().getWindowManagerWatcher().initPipelined(param.getPipelineId(), param.getParallelism());



    }

    @Override
    public void open() throws IOException {
        retry = 0;
        int retriesFromRedisServerInfo = r.getConfiguration().getRetriesFromRedisServerInfo();
        while (retry < retriesFromRedisServerInfo) {
            try {
                retry++;
                // TODO 同步检查
                r.open();
                log.info("open times "+retry);
            } catch (IOException e) {
                // 构建同步信息
                sync();
                // 开始aof rdb 复制
                open();
            }
        }


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
        String redisUrl= sourceUri.split(Constants.MULT_NODE_SPLIT)[0];
        Jedis jedis  = new Jedis(sourceUri);
        String master = buildMasterAddress(redisUrl, jedis);
        jedis.close();

        // 通过manager获取复制进度信息
        RelpInfoResponse relpInfoResponse = getInstance().callSyncInfo(param.getPipelineId());
        sourceUri =  "redis://"+master+""+6379+"?verbose=yes&retries=10&replId=" + relpInfoResponse.getReplid() + "&replOffset=" + relpInfoResponse.getOffset();
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




    private static String buildMasterAddress(String redisUrl, Jedis jedis) {
        String replicationInfo = jedis.info("replication");
        String[] redisInfo = replicationInfo.split("\r\n");
        String roleInfo = redisInfo[1].split(":")[1];
        String master = roleInfo.equals("master") ? null : redisInfo[2].split(":")[1];
        master = master == null ? redisUrl : master;
        return master;
    }




}

/**
 * redis - disruptor 复制消费者
 */

@Slf4j
class RotterSelectorEventHandler implements EventHandler<Object> {
    @Override
    public void onEvent(Object event, long sequence, boolean endOfBatch) {
        // disputor 消费
        log.info("event: {}, sequence: {}, endOfBatch: {}", event, sequence, endOfBatch);
    }

}