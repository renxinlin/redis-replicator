package com.renxl.rotter.sel;

/**
 *
 *
 * 通过管道拉取 sedg
 * @description:
 * @author: renxl
 * @create: 2021-01-07 19:05
 */
public interface Pipe {


    SelectorBatchEvent getSelectBatchEvent(Integer pipelineId, long seqNumber);
}
