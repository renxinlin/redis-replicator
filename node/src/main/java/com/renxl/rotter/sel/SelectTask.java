package com.renxl.rotter.sel;

import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.rpcclient.events.RelpInfoResponse;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;

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
     * 和
     */

    @Override
    public void run() {
        WindowBuffer selectBuffer = CompomentManager.getInstance().getWindowManager().getSelectBuffer(getPipelineId());
        // 阻塞式buffer 等到ack完毕会被添加回来
        long batchId = selectBuffer.get();
        Selector selector = SelectorFactory.buildSelector(new SelectorParam(getPipelineId(),sourceRedises,parallelism,relpInfoResponse));

    }


    @Override
    boolean getPermit() {
        return permit;
    }
}
