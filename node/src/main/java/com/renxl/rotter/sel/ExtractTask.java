package com.renxl.rotter.sel;

import com.renxl.rotter.config.CompomentManager;

import java.util.concurrent.ExecutorService;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:02
 */
public class ExtractTask extends Task {

    private ExecutorService executorService;

    public ExtractTask(Integer pipelineId) {
        this.setPipelineId(pipelineId);
        executorService = CompomentManager.getInstance().getExtractThreads();

    }


    @Override
    boolean getPermit() {
        return true;
    }

    public void run() {

    }

}
