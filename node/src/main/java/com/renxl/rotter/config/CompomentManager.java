package com.renxl.rotter.config;

import com.renxl.rotter.LifeCycle;
import com.renxl.rotter.constants.Constants;
import com.renxl.rotter.manager.*;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.events.LoadReadingEvent;
import com.renxl.rotter.rpcclient.events.RelpInfoEvent;
import com.renxl.rotter.rpcclient.events.RelpInfoResponse;
import com.renxl.rotter.rpcclient.events.SelectReadingEvent;
import com.renxl.rotter.rpcclient.impl.CommunicationConnectionFactory;
import com.renxl.rotter.rpcclient.impl.dubbo.DubboCommunicationEndpoint;
import com.renxl.rotter.sel.Pipe;
import com.renxl.rotter.sel.window.WindowData;
import com.renxl.rotter.sel.window.WindowSeqGenerator;
import com.renxl.rotter.sel.window.WindowType;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;
import com.renxl.rotter.task.HeartbeatScheduler;
import com.renxl.rotter.task.TaskServiceListener;
import com.renxl.rotter.zookeeper.ZKclient;
import lombok.Data;

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
     * 接收manager的 同步调度任务 负责manager和node之间的任务拓扑建立核心类
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


    /**
     * 滑动窗口【window】尾推进
     */
    private WindowManager windowManager;

    /**
     * 滑动窗口滑动头推进
     */
    private WindowManagerWatcher windowManagerWatcher;

    /**
     * 滑动窗口序列号生成器
     */
    private WindowSeqGenerator windowSeqGenerator;


    private NodeRegister nodeRegister;

    /**
     * 数据传输管道
     * zk只负责滑动窗口协议 监听
     * 不做具体的数据传递
     */
    private Pipe pipe;

    private int poolSize = EXCTRACT_DEFAULT_POOL_SIZE;

    private int acceptCount = EXCTRACT_DEFAULT_ACCEPT_COUNT;
    private String name = "rotter-extract";


    private CompomentManager() {

    }

    public static CompomentManager getInstance() {

        if (INSTANCE == null) {
            synchronized (CompomentManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = CompomentBuilder.bulid(HeartBeatConfig.dubboPort);
                }
            }
        }
        return INSTANCE;
    }


    protected static CompomentManager newInstance() {

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
        windowSeqGenerator.init();
        nodeRegister.init();

    }

    @Override
    public void stop() {
        communicationClient.destory();
        dubboCommunicationEndpoint.destory();
        metaManager.destory();
        metaManagerWatcher.destory();
        windowManagerWatcher.destory();
        windowSeqGenerator.destory();
        nodeRegister.destory();
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
        String[] ipAndPort = managerAddress.split(Constants.IP_PORT_SPLIT);
        managerInfo.setManagerAddress(ipAndPort[0]);
        managerInfo.setPort(Integer.valueOf(ipAndPort[1]));
        metaManager.setManager(managerInfo);

    }

    /**
     * 通知manager进行load授权许可
     *
     * @param pipelineId
     */
    public void callLoadPermit(Integer pipelineId) {
        ManagerInfo manager = metaManager.getManager();

        communicationClient.call(manager.getManagerAddress(), manager.getPort(), new LoadReadingEvent(pipelineId, CompomentManager.getInstance().getMetaManager().getNodeIp()));

    }

    /**
     * 通知manager进行授权许可
     *
     * @param pipelineId
     */
    public void callSelectPermit(Integer pipelineId) {
        ManagerInfo manager = metaManager.getManager();
        communicationClient.call(manager.getManagerAddress(), manager.getPort(), new SelectReadingEvent(pipelineId, CompomentManager.getInstance().getMetaManager().getNodeIp()));

    }

    /**
     * 获取当前同步任务的同步进度相关信息
     *
     * @param pipelineId
     * @return
     */
    public RelpInfoResponse callSyncInfo(Integer pipelineId) {
        ManagerInfo manager = metaManager.getManager();
        RelpInfoResponse relpInfoResponse = (RelpInfoResponse) communicationClient.call(manager.getManagerAddress(), manager.getPort(), new RelpInfoEvent(pipelineId));
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
        // 滑动窗口序列号 有seletorTask端自增 初始值0
        long batchId = windowData.getBatchId();
        if (!metaManager.getNodeIp().equals(ip)) {
            return;
        }
        if (windowType == WindowType.s) {
            WindowBuffer selectBuffer = windowManager.getSelectBuffer(pipeLineId);
            batchId = getWindowSeqGenerator().gene(pipeLineId);
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
