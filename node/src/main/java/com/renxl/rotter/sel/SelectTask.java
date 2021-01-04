package com.renxl.rotter.sel;

import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.rpcclient.events.RelpInfoResponse;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;

import java.io.IOException;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:02
 */
public class SelectTask extends Task {
    private String sourceRedises;
    private Integer parallelism;
    private RelpInfoResponse relpInfoResponse;


    public SelectTask(Integer pipelineId, String sourceRedises, Integer parallelism, RelpInfoResponse relpInfoResponse) {
        this.setPipelineId(pipelineId);
        this.sourceRedises = sourceRedises;
        this.parallelism = parallelism;
        this.relpInfoResponse = relpInfoResponse;
    }

    /**
     * 滑动窗口协议已经完毕
     * 接下来需要设计数据管道传输
     * 和队列缓冲
     */

    @Override
    public void run() {
        Selector selector = SelectorFactory.buildSelector(new SelectorParam(getPipelineId(), sourceRedises, parallelism, relpInfoResponse));
        selector.sync();
        // 启动 todo 启动优化
        while (true) {
            try {
                // 启动链接
                selector.open();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 消费redis replicator 如果读不到数据还是要发送心跳 消费端不可以阻塞 否则会造成manager看到假死现象导致同步任务被转移
        while (true) {
            if (permit) {

            }
            WindowBuffer selectBuffer = CompomentManager.getInstance().getWindowManager().getSelectBuffer(getPipelineId());
            // 阻塞式buffer 等到ack完毕会被添回来从而保障滑动窗口 batchid是递增趋势 从而保障滑动窗口确认序列号按顺序的特点
            long batchId = selectBuffer.get();


        }
    }


    @Override
    boolean getPermit() {
        return permit;
    }
}
