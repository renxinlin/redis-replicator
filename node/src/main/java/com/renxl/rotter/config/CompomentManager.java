package com.renxl.rotter.config;

import com.renxl.rotter.LifeCycle;
import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.manager.ManagerInfo;
import com.renxl.rotter.manager.MetaManager;
import com.renxl.rotter.manager.MetaManagerWatcher;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.events.LoadPermitEvent;
import com.renxl.rotter.rpcclient.events.SelectPermitEvent;
import com.renxl.rotter.rpcclient.impl.CommunicationConnectionFactory;
import com.renxl.rotter.rpcclient.impl.dubbo.DubboCommunicationEndpoint;
import com.renxl.rotter.task.HeartbeatScheduler;
import com.renxl.rotter.task.TaskServiceListener;
import com.renxl.rotter.zookeeper.ZKclient;
import lombok.Data;

import static com.renxl.rotter.zookeeper.ZookeeperConfig.managerMaster;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:06
 */
@Data
public class CompomentManager implements LifeCycle {

    /**
     * node核心组件
     */
    private static volatile CompomentManager INSTANCE;

    /**
     * rpc连接工厂
     */
    private CommunicationConnectionFactory dubboCommunicationConnectionFactory;

    /**
     * rpc客户端
     */
    private CommunicationClient communicationClient;

    /**
     * dubbo服务暴露
     */
    private DubboCommunicationEndpoint dubboCommunicationEndpoint;

    /**
     * node 心跳
     */
    private HeartbeatScheduler hearbeatScheduler;

    /**
     * manager master
     */
    private MetaManager metaManager;

    /**
     * manager master
     */
    private MetaManagerWatcher metaManagerWatcher;

    /**
     * 接收manager的 同步调度任务
     */
    TaskServiceListener taskServiceListener;

    private CompomentManager() {

    }

    public static CompomentManager getInstance() {

        if (INSTANCE == null) {
            synchronized (CompomentManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CompomentManager();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public void start() {
        communicationClient.initial();
        dubboCommunicationEndpoint.initial();
        metaManager.init();
        metaManagerWatcher.init();

    }

    @Override
    public void stop() {
        dubboCommunicationEndpoint.destory();
        communicationClient.destory();
        metaManagerWatcher.destory();
    }

    public String getManagerAdress() {
        boolean nodeExist = ZKclient.instance.isNodeExist(managerMaster);
        if (nodeExist) {
            return ZKclient.instance.getNode(managerMaster);
        }
        return null;
    }

    public void updateMeta(String managerAddress) {
        ManagerInfo managerInfo = new ManagerInfo();
        managerInfo.setManagerAddress(managerAddress);
        metaManager.setManager(managerInfo);

    }

    public void callLoadPermit(Integer pipelineId) {
        String managerAddress = metaManager.getManager().getManagerAddress();
        communicationClient.call(managerAddress,new LoadPermitEvent(pipelineId, AddressUtils.getHostAddress().getHostAddress()));

    }

    public void callSelectPermit(Integer pipelineId) {
        String managerAddress = metaManager.getManager().getManagerAddress();
        communicationClient.call(managerAddress,new SelectPermitEvent(pipelineId, AddressUtils.getHostAddress().getHostAddress()));

    }
}
