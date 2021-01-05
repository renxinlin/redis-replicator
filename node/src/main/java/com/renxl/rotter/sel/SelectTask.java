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
        try {
            // 启动链接
            selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
   public boolean getPermit() {
        return permit;
    }
}
