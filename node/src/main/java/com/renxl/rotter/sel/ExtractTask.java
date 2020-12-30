package com.renxl.rotter.sel;

import com.renxl.rotter.rpcclient.events.RelpInfoResponse;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:02
 */
public class ExtractTask extends Task{

    public ExtractTask(Integer pipelineId) {

    }

    public void start( ) {


    }

    @Override
    boolean getPermit() {
        return true;
    }
}
