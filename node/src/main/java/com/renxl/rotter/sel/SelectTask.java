package com.renxl.rotter.sel;

import com.renxl.rotter.rpcclient.events.RelpInfoResponse;

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


    @Override
    public void run() {

        SelectorFactory.buildSelector(null);

    }


    @Override
    boolean getPermit() {
        return permit;
    }
}
