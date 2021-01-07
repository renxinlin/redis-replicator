package com.renxl.rotter.sel;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:02
 */
public class LoadTask extends Task {


    private String targetRedis;

    /**
     * 按照滑动窗口wait队列 有序队列 顺序保障
     */
    private TreeSet<Long> currentWaitSeqNum;


    /**
     * 滑动窗口就绪队列 先进先出
     */
    private ArrayBlockingQueue<Long> currentReadySeqNum;


    private ExecutorService waitSeqProcessor;
    private ExecutorService readySeqProcessor;

    /**
     * 滑动窗口确认序列号 递增确认
     */
    private AtomicLong currentSeqNum = new AtomicLong(0L);

    public LoadTask(Integer pipelineId, String targetRedis, int parallelism) {
        currentWaitSeqNum = new TreeSet();
        currentReadySeqNum = new ArrayBlockingQueue(parallelism);
        this.setPipelineId(pipelineId);
        this.targetRedis = targetRedis;
        /**
         * 滑动窗口阻塞队列处理器
         */
        waitSeqProcessor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue(0), new NamedThreadFactory("load-window-wait-" + pipelineId),
                new ThreadPoolExecutor.CallerRunsPolicy());

        /**
         * 滑动窗口就绪队列处理器
         */
        readySeqProcessor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue(0), new NamedThreadFactory("load-window-ready-" + pipelineId),
                new ThreadPoolExecutor.CallerRunsPolicy());

    }



    @Override
    boolean getPermit() {
        return permit;
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
                long seqNumber = currentWaitSeqNum.pollFirst();
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
            try {
                // 通过上述的处理确保滑动窗口并发能力和有序性
                Long seqNumber = currentReadySeqNum.take();

                // 自动识别基于内存进行管道传输还是基于rpc进行管道传输
                SelectorBatchEvent selectBatchEvent = CompomentManager.getInstance().getPipe().getSelectBatchEvent(getPipelineId(), seqNumber);
                List<SelectorEvent> selectorEvent = selectBatchEvent.getSelectorEvent();
                if (!CollectionUtils.isEmpty(selectorEvent)) {
                    // 构建【添加删除保护指令 数据回环指令】


                    // 通过redis pipeline 进行批量发送
                }

                // 滑动窗口尾部推进
                String selecterIp = CompomentManager.getInstance().getMetaManager().getPipelineTaskIps().get(getPipelineId()).getSelecterIp();
                CompomentManager.getInstance().getWindowManager().singleSelect(getPipelineId(), selecterIp);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


}