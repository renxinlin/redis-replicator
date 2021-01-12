package com.renxl.rotter.task;

import com.renxl.rotter.rpcclient.CommunicationRegistry;
import com.renxl.rotter.rpcclient.events.*;
import com.renxl.rotter.sel.ExtractTask;
import com.renxl.rotter.sel.LoadTask;
import com.renxl.rotter.sel.SelectTask;
import com.renxl.rotter.sel.SelectorBatchEvent;

import static com.renxl.rotter.config.CompomentManager.getInstance;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-29 00:25
 */
public class TaskServiceListener {


    public TaskServiceListener() {

        CommunicationRegistry.regist(TaskEventType.selectTask, this);
        CommunicationRegistry.regist(TaskEventType.loadTask, this);
        CommunicationRegistry.regist(TaskEventType.selectAndLoadIp, this);
        CommunicationRegistry.regist(TaskEventType.getExtractBatch, this);
        CommunicationRegistry.regist(TaskEventType.ping, this);

        CommunicationRegistry.regist(TaskEventType.selectPermit, this);
        CommunicationRegistry.regist(TaskEventType.loadPermit, this);



    }

    public PongResponse onPing(PingEvent pingEvent) {
        return new PongResponse();
    }

    /**
     *
     * 启动selectTask
     * selectTask 和extract task 同时构建
     *
     * @param selectTaskEvent
     * @return
     */
    public boolean onSelectTask(SelectTaskEvent selectTaskEvent) {
        Integer pipelineId = selectTaskEvent.getPipelineId();
        // 获取sel 中e的并行度 类似otter的滑动窗口
        Integer parallelism = selectTaskEvent.getParallelism();
        // 获取同步的redis信息
        String sourceRedises = selectTaskEvent.getSourceRedises();
        // 获取pipelineId的复制进度
        RelpInfoResponse relpInfoResponse = getInstance().callSyncInfo(pipelineId);
        // 创建并启动 select task
        SelectTask selectTask = new SelectTask(pipelineId, sourceRedises, parallelism, relpInfoResponse);
        selectTask.setPipelineId(pipelineId);


        // extract task 和 select task 于同一台机器上
        ExtractTask extractTask = new ExtractTask(pipelineId, parallelism);

        // 添加到任务池
        getInstance().getMetaManager().addTask(extractTask);
        getInstance().getMetaManager().addTask(selectTask);

        // 阻塞等待selectPermit
        selectTask.start();
        extractTask.start();


        // **********************************************************完成任务初始化等待manager授权


        // 发送rpc ready 事件
        getInstance().callSelectPermit(pipelineId);
        return true;
    }


    public boolean onLoadTask(LoadTaskEvent loadTaskEvent) {
        Integer pipelineId = loadTaskEvent.getPipelineId();

        String targetRedis = loadTaskEvent.getTargetRedis();
        Integer parallelism = loadTaskEvent.getParallelism();
        // 发送rpc ready 事件

        LoadTask loadTask = new LoadTask(pipelineId, targetRedis, parallelism);
        loadTask.setPipelineId(pipelineId);
        getInstance().getMetaManager().addTask(loadTask);

        loadTask.start();
        getInstance().callLoadPermit(pipelineId);
        return true;
    }

    /**
     * 由manager通知pipeline的工作节点对应的相关信息
     *
     * @param selectAndLoadIpEvent
     */
    public void onSelectAndLoadIp(SelectAndLoadIpEvent selectAndLoadIpEvent) {
        getInstance().getMetaManager().addIps(selectAndLoadIpEvent);
    }


    /**
     * 收到manager节点的许可凭证
     *
     * @param event
     * @return
     */
    public void onSelectPermit(SelectPermitEvent event) {
        Integer pipelineId = event.getPipelineId();
        SelectTask selectTask = getInstance().getMetaManager().getPipelineSelectTasks().get(pipelineId);
        selectTask.permit();
    }

    /**
     * 收到manager节点的许可凭证
     *
     * @param event
     * @return
     */
    public void onLoadPermit(LoadPermitEvent event) {
        Integer pipelineId = event.getPipelineId();
        LoadTask loadTask = getInstance().getMetaManager().getPipelineLoadTasks().get(pipelineId);
        loadTask.permit();
    }


    /**
     * load节点通过pipe拉取数据
     *
     * @param event
     * @return
     */
    public SelectorBatchEvent onGetExtractBatchEvent(GetExtractBatchEvent event) {
        SelectorBatchEvent selectorBatchEvent = getInstance().getMetaManager().takeExtractEvent(event.getPipelineId(), event.getSeqNumber());
        return selectorBatchEvent;
    }

    /**
     * todo
     *
     * @param event
     * @return
     */
    public void onSelectUnpermit(SelectPermitEvent event) {
        Integer pipelineId = event.getPipelineId();
        SelectTask selectTask = getInstance().getMetaManager().getPipelineSelectTasks().get(pipelineId);
        selectTask.permit();
    }

    /**
     * todo
     *
     * @param event
     * @return
     */
    public void onLoadUnpermit(LoadPermitEvent event) {
        Integer pipelineId = event.getPipelineId();
        LoadTask loadTask = getInstance().getMetaManager().getPipelineLoadTasks().get(pipelineId);
        loadTask.permit();
    }
}
