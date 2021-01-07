package com.renxl.rotter.sel.window;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 滑动窗口生成器
 * @description:
 * @author: renxl
 * @create: 2020-12-30 19:53
 */
public class WindowSeqGenerator {


    private  ConcurrentMap<Integer, AtomicLong> piplineIdAndSelectTaskGenerator;

    public void init() {
        piplineIdAndSelectTaskGenerator = new ConcurrentHashMap<>();

    }


    public void destory() {
        piplineIdAndSelectTaskGenerator = null;

    }

    /**
     * 滑动窗口初始序列号是0
     * @param pipelineId
     * @return
     */
    public  long gene(Integer pipelineId) {
        AtomicLong geneoratorBatchId = piplineIdAndSelectTaskGenerator.getOrDefault(pipelineId, new AtomicLong(0));
        return geneoratorBatchId.getAndAdd(1);

    }

}
