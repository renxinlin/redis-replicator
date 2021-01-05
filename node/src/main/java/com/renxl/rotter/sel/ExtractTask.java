package com.renxl.rotter.sel;

import com.alibaba.dubbo.common.utils.NamedThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:02
 */
public class ExtractTask extends Task {

    private ExecutorService extractThreads;


    public ExtractTask(Integer pipelineId, Integer parallelism) {
        this.setPipelineId(pipelineId);
        /**
         * node节点配置的extracttask线程池的大小
         */
        extractThreads = new ThreadPoolExecutor(parallelism, parallelism, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue(0), new NamedThreadFactory("extract-pipelineId-" + pipelineId),
                new ThreadPoolExecutor.CallerRunsPolicy());


    }


    @Override
    boolean getPermit() {
        return true;
    }

    public void run() {



    }

}
