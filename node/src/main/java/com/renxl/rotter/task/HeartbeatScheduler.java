package com.renxl.rotter.task;

import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.config.HeartBeatConfig;
import com.renxl.rotter.manager.ManagerInfo;
import com.renxl.rotter.manager.MetaManager;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.NodeHeartEvent;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 21:52
 */
public class HeartbeatScheduler  {


    public List<String> selectPipelines;
    public List<String> nodePipelines;



    public ScheduledExecutorService executors = Executors.newScheduledThreadPool(1,new DefaultThreadFactory("rotter-heartbeat"));


    public void  addSelectPipeline(String selectPipeline){
        if(selectPipelines == null){
            selectPipelines = new ArrayList<>();
        }
        selectPipelines.add(selectPipeline);
    }


    public void  addLoadPipeline(String nodePipeline){
        if(nodePipelines == null){
            nodePipelines = new ArrayList<>();
        }
        nodePipelines.add(nodePipeline);
    }

    public void init(){
        // 心跳15秒一次 超时 60秒   [类似eureka] 后期将这些都配置化
        executors.scheduleAtFixedRate(()->{
            // 获取当前的manager信息
            ManagerInfo manager = CompomentManager.getInstance().getMetaManager().getManager();

            CommunicationClient communicationClient = CompomentManager.getInstance().getCommunicationClient();
            communicationClient.call(manager.getManagerAddress(),manager.getPort(), new NodeHeartEvent(AddressUtils.getHostAddress().getHostAddress()));

        },10*1000, HeartBeatConfig.heartBeatTime, TimeUnit.MILLISECONDS);
    }
}
