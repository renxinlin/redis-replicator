package com.renxl.rotter.task;

import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.CommunicationRegistry;
import com.renxl.rotter.rpcclient.events.LoadTaskEvent;
import com.renxl.rotter.rpcclient.events.SelectTaskEvent;
import com.renxl.rotter.rpcclient.events.TaskEventType;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-29 00:25
 */
public class TaskServiceListener {


    public  TaskServiceListener (){

        CommunicationRegistry.regist(TaskEventType.startTask, this);
        CommunicationRegistry.regist(TaskEventType.loadTask, this);
    }

    public boolean onStartTask(SelectTaskEvent selectTaskEvent){
        // 获取sel 中e的并行度 类似otter的滑动窗口
        Integer parallelism = selectTaskEvent.getParallelism();
        // 获取同步的redis信息
        String sourceRedises = selectTaskEvent.getSourceRedises();
        // 发送rpc ready 事件


        CompomentManager.getInstance().callSelectPermit(selectTaskEvent.getPipelineId());
        // 创建 select
        return true;
    }


    public boolean onLoadTask(LoadTaskEvent loadTaskEvent){

        String targetRedis = loadTaskEvent.getTargetRedis();
        // 发送rpc ready 事件
        CompomentManager.getInstance().callLoadPermit(loadTaskEvent.getPipelineId());
        return true;
    }
}
