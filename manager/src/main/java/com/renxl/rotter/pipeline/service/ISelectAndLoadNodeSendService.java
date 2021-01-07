package com.renxl.rotter.pipeline.service;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-07 16:30
 */
public interface ISelectAndLoadNodeSendService {
    void sendWhenLoadReady(Integer pipelineId);

    void sendWhenSelectorReady(Integer pipelineId);
}
