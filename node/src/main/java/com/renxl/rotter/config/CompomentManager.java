package com.renxl.rotter.config;

import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.renxl.rotter.LifeCycle;
import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.common.IdWorker;
import com.renxl.rotter.manager.*;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.events.LoadReadingEvent;
import com.renxl.rotter.rpcclient.events.RelpInfoEvent;
import com.renxl.rotter.rpcclient.events.RelpInfoResponse;
import com.renxl.rotter.rpcclient.events.SelectReadingEvent;
import com.renxl.rotter.rpcclient.impl.CommunicationConnectionFactory;
import com.renxl.rotter.rpcclient.impl.dubbo.DubboCommunicationEndpoint;
import com.renxl.rotter.sel.window.WindowData;
import com.renxl.rotter.sel.window.WindowType;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;
import com.renxl.rotter.task.HeartbeatScheduler;
import com.renxl.rotter.task.TaskServiceListener;
import com.renxl.rotter.zookeeper.ZKclient;
import lombok.Data;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.renxl.rotter.config.HeartBeatConfig.EXCTRACT_DEFAULT_ACCEPT_COUNT;
import static com.renxl.rotter.config.HeartBeatConfig.EXCTRACT_DEFAULT_POOL_SIZE;
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
     * 接收manager的 同步调度任务
     */
    TaskServiceListener taskServiceListener;
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

    private WindowManager windowManager;
    private WindowManagerWatcher windowManagerWatcher;
    private int poolSize = EXCTRACT_DEFAULT_POOL_SIZE;

    private int acceptCount = EXCTRACT_DEFAULT_ACCEPT_COUNT;
    private String name = "rotter-extract";

    private IdWorker idWorker = new IdWorker();
    /**
     * node节点配置的extracttask线程池的大小
     */
    private ExecutorService extractThreads = new ThreadPoolExecutor(poolSize, poolSize, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue(acceptCount), new NamedThreadFactory(name),
            new ThreadPoolExecutor.CallerRunsPolicy());


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
        windowManagerWatcher.init();

    }

    @Override
    public void stop() {
        dubboCommunicationEndpoint.destory();
        communicationClient.destory();
        metaManagerWatcher.destory();
        windowManagerWatcher.destory();
    }


    /**
     * 初始化获取manager信息 node启动时调用
     *
     * @return
     */
    public String callInitManagerAdress() {
        boolean nodeExist = ZKclient.instance.isNodeExist(managerMaster);
        if (nodeExist) {
            return ZKclient.instance.getNode(managerMaster);
        }
        return null;
    }

    /**
     * 更新node节点的manager master信息
     * manager采用抢占式选举
     * manager变更时调用
     *
     * @param managerAddress
     */
    public void onUpdateMeta(String managerAddress) {
        ManagerInfo managerInfo = new ManagerInfo();
        managerInfo.setManagerAddress(managerAddress);
        metaManager.setManager(managerInfo);

    }

    /**
     * 通知manager进行load授权许可
     *
     * @param pipelineId
     */
    public void callLoadPermit(Integer pipelineId) {
        String managerAddress = metaManager.getManager().getManagerAddress();
        communicationClient.call(managerAddress, new LoadReadingEvent(pipelineId, AddressUtils.getHostAddress().getHostAddress()));

    }

    /**
     * 通知manager进行授权许可
     *
     * @param pipelineId
     */
    public void callSelectPermit(Integer pipelineId) {
        String managerAddress = metaManager.getManager().getManagerAddress();
        communicationClient.call(managerAddress, new SelectReadingEvent(pipelineId, AddressUtils.getHostAddress().getHostAddress()));

    }

    /**
     * 获取当前同步任务的同步进度相关信息
     *
     * @param pipelineId
     * @return
     */
    public RelpInfoResponse callSyncInfo(Integer pipelineId) {
        String managerAddress = metaManager.getManager().getManagerAddress();
        RelpInfoResponse relpInfoResponse = (RelpInfoResponse) communicationClient.call(managerAddress, new RelpInfoEvent(pipelineId));
        return relpInfoResponse;
    }

    /**
     * 处理滑动窗口
     *
     * @param windowData
     */
    public void onUpdateWindow(WindowData windowData) {
        String ip = windowData.getIp();
        Integer pipeLineId = windowData.getPipeLineId();
        short windowType = windowData.getWindowType();
        // 滑动窗口序列号 采用idworker自增
        long batchId = windowData.getBatchId();
        if (!metaManager.getNodeIp().equals(ip)) {
            return;
        }
        if (windowType == WindowType.s) {
            WindowBuffer selectBuffer = windowManager.getSelectBuffer(pipeLineId);
            selectBuffer.put(batchId);
        }

        if (windowType == WindowType.e) {
            WindowBuffer buffer = windowManager.getExtractBuffer(pipeLineId);
            buffer.put(batchId);
        }

        if (windowType == WindowType.l) {
            WindowBuffer buffer = windowManager.getLoadBuffer(pipeLineId);
            buffer.put(batchId);
        }


    }
}
