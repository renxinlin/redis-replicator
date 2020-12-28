package com.renxl.rotter.pipeline.service;

/**
 *
 * 允许node正式工作
 * @description:
 * @author: renxl
 * @create: 2020-12-28 16:33
 */
public interface IPermitService {

    /**
     * 进行许可
     * @param pipelineId
     */
    void permit(Integer pipelineId);

}
