package com.renxl.rotter.pipeline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.renxl.rotter.pipeline.domain.PipelineNodeInfo;
import com.renxl.rotter.pipeline.domain.PipelineTaskReading;
import com.renxl.rotter.pipeline.mapper.PipelineTaskReadingMapper;
import com.renxl.rotter.pipeline.service.IPermitService;
import com.renxl.rotter.pipeline.service.IPipelineNodeInfoService;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.events.LoadPermitEvent;
import com.renxl.rotter.rpcclient.events.SelectPermitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * <p>
 *
 * </p>
 *
 * @author renxl
 * @since 2020-12-28
 */
@Service
@Primary
public class RpcPermitServiceImpl extends ServiceImpl<PipelineTaskReadingMapper, PipelineTaskReading> implements IPermitService {


    @Autowired
    IPipelineNodeInfoService iPipelineNodeInfoService;

    @Autowired
    CommunicationClient communicationClient;


    @Override
    public void permit(Integer pipelineId) {
        PipelineNodeInfo nodeInfo = iPipelineNodeInfoService.getOne(iPipelineNodeInfoService.lambdaQuery().eq(PipelineNodeInfo::getPipelineId, pipelineId).getWrapper());
        // 许可 select load 两个节点执行任务
        communicationClient.call(nodeInfo.getLastSelectNode(), new SelectPermitEvent(pipelineId, nodeInfo.getLastSelectNode()));
        communicationClient.call(nodeInfo.getLastLoadNode(), new LoadPermitEvent(pipelineId, nodeInfo.getLastLoadNode()));


    }
}
