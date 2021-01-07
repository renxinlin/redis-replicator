package com.renxl.rotter.pipeline.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.renxl.rotter.pipeline.domain.PipelineSyncInfo;
import com.renxl.rotter.pipeline.domain.PipelineTaskReading;
import com.renxl.rotter.pipeline.service.IPermitService;
import com.renxl.rotter.pipeline.service.IPipelineSyncInfoService;
import com.renxl.rotter.pipeline.service.IPipelineTaskReadingService;
import com.renxl.rotter.pipeline.service.ISelectAndLoadNodeSendService;
import com.renxl.rotter.rpcclient.CommunicationRegistry;
import com.renxl.rotter.rpcclient.events.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 15:45
 */
@Component
public class RotterReadingEventListener {

    @Autowired
    private IPipelineTaskReadingService taskReadingService;




    @Autowired
    private IPipelineSyncInfoService syncInfoService;


    @Autowired
    private IPermitService permitService;

    @Autowired
    ISelectAndLoadNodeSendService selectAndLoadNodeSendService;

    RotterReadingEventListener() {
        CommunicationRegistry.regist(TaskEventType.selectReading, this);
        CommunicationRegistry.regist(TaskEventType.loadReading, this);
    }


    public void onSelectReading(SelectReadingEvent event) {
        LambdaQueryWrapper<PipelineTaskReading> queryByPipelineId = PipelineTaskReadingQueryWrapper.buildQuery(event.getPipelineId());
        PipelineTaskReading pipelineTaskReading = taskReadingService.getOne(queryByPipelineId);
        if (pipelineTaskReading == null) {
            pipelineTaskReading.initSelectReading(event.getPipelineId());
            taskReadingService.getBaseMapper().insert(pipelineTaskReading);
            return;
        }
        taskReadingService.updateById(pipelineTaskReading);
        // 准备完毕则许可node节点进行工作
        boolean loadReading = pipelineTaskReading.isLoadReading();
        // 更新节点相关信息
        selectAndLoadNodeSendService.sendWhenSelectorReady(event.getPipelineId());
        if (loadReading) {
            permitService.permit(event.getPipelineId());
            taskReadingService.getBaseMapper().deleteById(pipelineTaskReading.getId());
        }


        // 添加node上的的同步任务信息
    }


    public void onLoadReading(LoadReadingEvent event) {
        LambdaQueryWrapper<PipelineTaskReading> queryByPipelineId = PipelineTaskReadingQueryWrapper.buildQuery(event.getPipelineId());
        PipelineTaskReading pipelineTaskReading = taskReadingService.getOne(queryByPipelineId);
        if (pipelineTaskReading == null) {
            pipelineTaskReading.initLoadReading(event.getPipelineId());
            taskReadingService.getBaseMapper().insert(pipelineTaskReading);
            return;
        }
        taskReadingService.updateById(pipelineTaskReading);
        // 更新节点相关信息
        selectAndLoadNodeSendService.sendWhenLoadReady(event.getPipelineId());
        // 准备完毕则许可node节点进行工作
        boolean selectReading = pipelineTaskReading.isselectReading();
        if (selectReading) {
            permitService.permit(event.getPipelineId());
            taskReadingService.getBaseMapper().deleteById(pipelineTaskReading.getId());
        }
        // 添加node上的的同步任务信息
    }

    /**
     * 获取当前同步进度
     * @param event
     * @return
     */
    public RelpInfoResponse onRelpInfo(RelpInfoEvent event) {
        Integer pipelineId = event.getPipelineId();
        PipelineSyncInfo syncInfo = syncInfoService.getOne(syncInfoService.lambdaQuery().eq(PipelineSyncInfo::getPipelineId, pipelineId).getWrapper());
        RelpInfoResponse relpInfoResponse = new RelpInfoResponse();
        if(syncInfo!=null){
            relpInfoResponse.setOffset(syncInfo.getOffset());
            relpInfoResponse.setPipelineId(syncInfo.getPipelineId());
            relpInfoResponse.setReplid(syncInfo.getReplid());
            relpInfoResponse.setReplJson(syncInfo.getReplJson());
            relpInfoResponse.setOffset(syncInfo.getOffset());
        }

        return relpInfoResponse;
    }




}


class PipelineTaskReadingQueryWrapper {
    public static LambdaQueryWrapper<PipelineTaskReading> buildQuery(Integer pipelineId) {
        LambdaQueryWrapper<PipelineTaskReading> queryByPipelineId = new LambdaQueryWrapper<>();
        queryByPipelineId.eq(PipelineTaskReading::getPipelineId, pipelineId);
        queryByPipelineId.last(" for update ");
        return queryByPipelineId;
    }

}