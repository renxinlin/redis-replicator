package com.renxl.rotter.sel;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.fastjson.JSON;
import com.moilioncircle.examples.migration.MigrationExample;
import com.moilioncircle.redis.replicator.cmd.impl.AbstractCommand;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.cmd.impl.SelectCommand;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.domain.RedisMasterInfo;
import com.renxl.rotter.sel.extract.LoadMarkFilter;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.PipelineWithCommand;
import redis.clients.jedis.Protocol;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static redis.clients.jedis.Protocol.toByteArray;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:02
 */
@Slf4j
public class LoadTask extends Task {


    final AtomicInteger dbnum;
    PipelineWithCommand pipeline;
    private String targetRedis;
    /**
     * 按照滑动窗口wait队列 有序队列 顺序保障
     */
    private PriorityBlockingQueue<Long> currentWaitSeqNum;
    /**
     * 滑动窗口就绪队列 先进先出
     */
    private ArrayBlockingQueue<Long> currentReadySeqNum;
    private ExecutorService waitSeqProcessor;
    private ExecutorService readySeqProcessor;
    private LoadMarkFilter loadMarkFilter;
    /**
     * 滑动窗口确认序列号 递增确认
     */
    private AtomicLong currentSeqNum = new AtomicLong(0L);
    private JedisPool jedisPool;

    public LoadTask(Integer pipelineId, String targetRedis, int parallelism) {
        currentWaitSeqNum = new PriorityBlockingQueue(2 * parallelism);
        currentReadySeqNum = new ArrayBlockingQueue(2 * parallelism);
        this.setPipelineId(pipelineId);
        this.targetRedis = targetRedis;
        dbnum = new AtomicInteger(-1);

        /**
         * 滑动窗口阻塞队列处理器
         */
        waitSeqProcessor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
                  new SynchronousQueue(), new NamedThreadFactory("load-window-wait-" + pipelineId),
                new ThreadPoolExecutor.CallerRunsPolicy());

        /**
         * 滑动窗口就绪队列处理器
         */
        readySeqProcessor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
                  new SynchronousQueue(), new NamedThreadFactory("load-window-ready-" + pipelineId),
                new ThreadPoolExecutor.CallerRunsPolicy());

        loadMarkFilter = new LoadMarkFilter();
        initRedis();

    }

    /**
     *
     */
    public void initRedis() {
        RedisMasterInfo redisMasterInfo = new RedisMasterInfo();
        redisMasterInfo.parse(targetRedis);
        MigrationExample.ExampleClient exampleClient = new MigrationExample.ExampleClient(redisMasterInfo.getIp(),  Integer.valueOf(redisMasterInfo.getPort()));
        pipeline = new PipelineWithCommand();
        pipeline.setClient(exampleClient);
    }

    @Override
    boolean getPermit() {
        // manager管理授权
        return permit;
    }

    public static void main(String[] args) throws InterruptedException {
        PriorityBlockingQueue<Long> currentWaitSeqNum = new PriorityBlockingQueue<>(8);
        currentWaitSeqNum.add(1L);
        Long take = currentWaitSeqNum.take();
        System.out.println(1);

          take = currentWaitSeqNum.take();
        System.out.println(1);

    }
    public void run() {


        // 滑动窗口处理
        waitSeqProcessor.execute(() -> {
            while (true) {
                WindowBuffer loadBuffer = CompomentManager.getInstance().getWindowManager().getLoadBuffer(getPipelineId());
                long seqNumber = loadBuffer.get();
                currentWaitSeqNum.add(seqNumber);
            }
        });
        readySeqProcessor.execute(() -> {
            while (true) {
                Long seqNumber = null;
                try {
                    seqNumber = currentWaitSeqNum.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (currentSeqNum.get() == seqNumber) {
                    // tcp就绪队列
                    try {
                        currentReadySeqNum.put(seqNumber);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 滑动
                    currentSeqNum.addAndGet(1);
                } else {
                    // 放回去 等待正确的数据处理
                    currentWaitSeqNum.add(seqNumber);
                }
            }
        });

        // load 阶段单线程处理 pipeline发送 增加速度
        while (true) {

            while (!permit) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.warn("wait for manager  permit ");
                }
            }

            try {
                // 通过上述的处理确保滑动窗口并发能力和有序性
                Long seqNumber = currentReadySeqNum.take();
                // 自动识别基于内存进行管道传输还是基于rpc进行管道传输
                SelectorBatchEvent selectBatchEvent = CompomentManager.getInstance().getPipe().getSelectBatchEvent(getPipelineId(), seqNumber);
                List<SelectorEvent> selectorEvents = selectBatchEvent.getSelectorEvent();
                // 构建【添加删除保护指令 数据回环指令】 mark的时候select的顺序不可以变
                selectorEvents = loadMarkFilter.mark(selectorEvents);
                System.out.println("load " + selectorEvents);
                if (!CollectionUtils.isEmpty(selectorEvents)) {

                    selectorEvents.forEach(selectorEvent -> {
                        AbstractCommand abstartCommand = selectorEvent.getAbstartCommand();
                        KeyValuePair keyValuePair = selectorEvent.getKeyValuePair();
                        if (abstartCommand != null) {
                            if (abstartCommand instanceof DefaultCommand) {
                                System.out.println("loadaof Default" + abstartCommand);

                                pipeline.send(((DefaultCommand) abstartCommand).getCommand(), ((DefaultCommand) abstartCommand).getArgs());
                            }
                            if (abstartCommand instanceof SelectCommand) {
                                System.out.println("loadaof SELECT " + abstartCommand);

                                pipeline.send("SELECT".getBytes(), toByteArray(((SelectCommand) abstartCommand).getIndex()));
                            }

                        }

                        if (null != keyValuePair) {
                            if (keyValuePair instanceof DumpKeyValuePair) {
                                DumpKeyValuePair dkv = (DumpKeyValuePair) keyValuePair;
                                // Step1: select db
                                DB db = dkv.getDb();
                                int index;
                                if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {
                                    System.out.println("loadkeypair SELECT" + dkv);
                                    pipeline.send("SELECT".getBytes(), Protocol.toByteArray(index));
                                    dbnum.set(index);
                                }
                                if (dkv.getExpiredMs() == null) {

                                    byte[][] args = new byte[4][];
                                    args[0] = ((DumpKeyValuePair) keyValuePair).getKey();
                                    args[1] = Protocol.toByteArray(0L);
                                    args[2] = dkv.getValue();
                                    args[3] = "REPLACE".getBytes();
                                    System.out.println("loadkeypair SELECT" +dkv);
                                    pipeline.send("RESTORE".getBytes(), args);

                                } else {

                                    long ms = dkv.getExpiredMs() - System.currentTimeMillis();
                                    // 过期key 不在处理
                                    if (ms <= 0) return;
                                    byte[][] args = new byte[4][];
                                    args[0] = ((DumpKeyValuePair) keyValuePair).getKey();
                                    args[1] = Protocol.toByteArray(dkv.getExpiredMs());
                                    args[2] = dkv.getValue();
                                    args[3] = "REPLACE".getBytes();
                                    System.out.println("loadkeypair SELECT" + dkv);
                                    pipeline.send("RESTORE".getBytes(), args);
                                }
                            }
                        }
                    });

                    // 通过redis pipeline 进行批量发送
                    System.out.println("pipelie  sync event: "+selectorEvents + "size"+selectorEvents.size());

                    pipeline.sync();
                }

                // 滑动窗口尾部推进
                String selecterIp = CompomentManager.getInstance().getMetaManager().getPipelineTaskIps().get(getPipelineId()).getSelecterIp();
                CompomentManager.getInstance().getWindowManager().singleSelect(getPipelineId(), selecterIp);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     *
     *  redis 回复协议体格式说明:
     *  http://www.redis.cn/topics/protocol.html
     *
     *
     * Redis用不同的回复类型回复命令。它可能从服务器发送的第一个字节开始校验回复类型：
     *
     * 用单行回复，回复的第一个字节将是“+”
     * 错误消息，回复的第一个字节将是“-”
     * 整型数字，回复的第一个字节将是“:”
     * 批量回复，回复的第一个字节将是“$”
     * 多个批量回复，回复的第一个字节将是“*”
     *
     *
     * 可以参考dump命令和restore命令
     * 127.0.0.1:6379> dump asda
     * "\x00\x01a\b\x00\x06\xd3\x03$ &/\xf7\x8c"
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */

}