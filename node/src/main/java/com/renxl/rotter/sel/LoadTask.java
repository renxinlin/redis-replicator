package com.renxl.rotter.sel;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:02
 */
public class LoadTask extends Task {

    private String  targetRedis ;
    public LoadTask(Integer pipelineId, String targetRedis) {
        this.setPipelineId(pipelineId);
        this.targetRedis = targetRedis;

    }
    @Override
    boolean getPermit() {
        return permit;
    }


     public void run() {

    }

}
