package com.renxl.rotter.task;

import com.renxl.rotter.LoadTask;
import com.renxl.rotter.rpcclient.CommunicationRegistry;
import com.renxl.rotter.rpcclient.events.LoadTaskEvent;
import com.renxl.rotter.rpcclient.events.RelpInfoResponse;
import com.renxl.rotter.rpcclient.events.SelectTaskEvent;
import com.renxl.rotter.rpcclient.events.TaskEventType;
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
    public boolean onSelectTask(SelectTaskEvent selectTaskEvent){
        Integer pipelineId = selectTaskEvent.getPipelineId();
        // 获取sel 中e的并行度 类似otter的滑动窗口
        Integer parallelism = selectTaskEvent.getParallelism();
        // 获取同步的redis信息
        String sourceRedises = selectTaskEvent.getSourceRedises();
        // 获取pipelineId的复制进度
        RelpInfoResponse relpInfoResponse = getInstance().callSyncInfo(pipelineId);
        // 创建并启动 select task
        SelectTask selectTask = new SelectTask(sourceRedises,parallelism,relpInfoResponse);
        selectTask.setPipelineId(pipelineId);
        selectTask.start();
        // 发送rpc ready 事件
        getInstance().callSelectPermit(pipelineId);
        // 添加到同步任务池
        getInstance().getMetaManager().addTask(selectTask);


        // extract task
        // extract task 和 select task 于同一台机器上


        return true;
    }


    public boolean onLoadTask(LoadTaskEvent loadTaskEvent){
        Integer pipelineId = loadTaskEvent.getPipelineId();

        String targetRedis = loadTaskEvent.getTargetRedis();
        // 发送rpc ready 事件

        LoadTask loadTask = new LoadTask(targetRedis);
        loadTask.setPipelineId(pipelineId);
        getInstance().getMetaManager().addTask(loadTask);

        getInstance().callLoadPermit(pipelineId);
        return true;
    }
}
