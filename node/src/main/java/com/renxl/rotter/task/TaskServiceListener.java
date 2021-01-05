package com.renxl.rotter.task;

import com.renxl.rotter.rpcclient.events.*;
import com.renxl.rotter.sel.ExtractTask;
import com.renxl.rotter.sel.LoadTask;
import com.renxl.rotter.rpcclient.CommunicationRegistry;
import com.renxl.rotter.sel.SelectTask;

import static com.renxl.rotter.config.CompomentManager.*;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-29 00:25
 */
public class TaskServiceListener {


    public  TaskServiceListener (){

        CommunicationRegistry.regist(TaskEventType.selectTask, this);
        CommunicationRegistry.regist(TaskEventType.loadTask, this);
    }

    /**
     * selectTask 和extract task 同时构建
     * @param selectTaskEvent
     * @return
     */
    public boolean onSelectTask(SelectTaskEvent selectTaskEvent){
        Integer pipelineId = selectTaskEvent.getPipelineId();
        // 获取sel 中e的并行度 类似otter的滑动窗口
        Integer parallelism = selectTaskEvent.getParallelism();
        // 获取同步的redis信息
        String sourceRedises = selectTaskEvent.getSourceRedises();
        // 获取pipelineId的复制进度
        RelpInfoResponse relpInfoResponse = getInstance().callSyncInfo(pipelineId);
        // 创建并启动 select task
        SelectTask selectTask = new SelectTask(pipelineId,sourceRedises,parallelism,relpInfoResponse);
        selectTask.setPipelineId(pipelineId);
        selectTask.start(); // 阻塞等待selectPermit

        // 添加到同步任务池
        getInstance().getMetaManager().addTask(selectTask);


        // extract task
        // extract task 和 select task 于同一台机器上

        ExtractTask extractTask = new ExtractTask(pipelineId,parallelism);
        extractTask.start();


        // **********************************************************完成任务初始化等待manager授权


        // 发送rpc ready 事件
        getInstance().callSelectPermit(pipelineId);
        return true;
    }


    public boolean onLoadTask(LoadTaskEvent loadTaskEvent){
        Integer pipelineId = loadTaskEvent.getPipelineId();

        String targetRedis = loadTaskEvent.getTargetRedis();
        // 发送rpc ready 事件

        LoadTask loadTask = new LoadTask(pipelineId,targetRedis);
        loadTask.setPipelineId(pipelineId);
        getInstance().getMetaManager().addTask(loadTask);

        getInstance().callLoadPermit(pipelineId);
        return true;
    }



    /**
     * 收到manager节点的许可凭证
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
     * @param event
     * @return
     */
    public void onLoadPermit(LoadPermitEvent event) {
        Integer pipelineId = event.getPipelineId();
        LoadTask loadTask = getInstance().getMetaManager().getPipelineLoadTasks().get(pipelineId);
        loadTask.permit();
    }




    /**
     * todo
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
     * @param event
     * @return
     */
    public void onLoadUnpermit(LoadPermitEvent event) {
        Integer pipelineId = event.getPipelineId();
        LoadTask loadTask = getInstance().getMetaManager().getPipelineLoadTasks().get(pipelineId);
        loadTask.permit();
    }
}
