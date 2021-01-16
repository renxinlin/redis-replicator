package com.renxl.rotter.manager;

import com.alibaba.dubbo.common.json.JSON;
import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.domain.SelectAndLoadIp;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.Event;
import com.renxl.rotter.rpcclient.events.WindowEvent;
import com.renxl.rotter.sel.window.WindowData;
import com.renxl.rotter.sel.window.WindowType;
import com.renxl.rotter.sel.window.buffer.SelectWindowBuffer;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;
import com.renxl.rotter.zookeeper.ZKclient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.renxl.rotter.zookeeper.ZookeeperConfig.pipelineWindowTemp;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:04
 */
@Slf4j
public class WindowManager {
    /**
     * todo 优化线程安全
     */
    private volatile Map<Integer, WindowBuffer> sWindowBuffers = new HashMap();
    private volatile Map<Integer, WindowBuffer> eWindowBuffers = new HashMap();
    private volatile Map<Integer, WindowBuffer> lWindowBuffers = new HashMap();


    private volatile Map<Integer, AtomicBoolean> selectInit = new HashMap();
    private volatile Map<Integer, AtomicBoolean> extractInit = new HashMap();
    private volatile Map<Integer, AtomicBoolean> loadInit = new HashMap();


    public WindowBuffer getSelectBuffer(Integer pipelineId) {
        WindowBuffer windowBuffer = sWindowBuffers.get(pipelineId);
        return getWindowBuffer(pipelineId, windowBuffer, sWindowBuffers);
    }


    public WindowBuffer getExtractBuffer(Integer pipelineId) {
        WindowBuffer windowBuffer = eWindowBuffers.get(pipelineId);
        return getWindowBuffer(pipelineId, windowBuffer, eWindowBuffers);
    }

    public WindowBuffer getLoadBuffer(Integer pipelineId) {
        WindowBuffer windowBuffer = lWindowBuffers.get(pipelineId);
        return getWindowBuffer(pipelineId, windowBuffer, lWindowBuffers);
    }


    private WindowBuffer getWindowBuffer(Integer pipelineId, WindowBuffer windowBuffer, Map<Integer, WindowBuffer> sWindowBuffers) {
        if (windowBuffer == null) {
            synchronized (sWindowBuffers) {
                if (windowBuffer == null) {
                    windowBuffer = new SelectWindowBuffer();
                    sWindowBuffers.put(pipelineId, windowBuffer);
                }
            }
        }
        return windowBuffer;
    }

    /**
     * 滑动窗口序列号
     *
     * @param pipelineId
     * @param syncNumber
     */
    public void singleExtract(Integer pipelineId, long syncNumber) {
        WindowData windowData = new WindowData(pipelineId, WindowType.e, AddressUtils.getHostAddress().getHostAddress(), syncNumber);
        CommunicationClient communicationClient = CompomentManager.getInstance().getCommunicationClient();
        SelectAndLoadIp selectAndLoadIp = CompomentManager.getInstance().getMetaManager().getPipelineTaskIps().get(pipelineId);
        String selecterIp = selectAndLoadIp.getSelecterIp();
        String selecterport = selectAndLoadIp.getSelecterport();
        communicationClient.call(selecterIp, Integer.valueOf(selecterport),  new WindowEvent(windowData.getPipeLineId(),windowData.getWindowType(),windowData.getIp(),windowData.getBatchId()));
    }





    /**
     * load 节点调用这个
     * <p>
     * rpc通着select节点进行
     *
     * @param pipelineId
     * @param ip
     * @throws IOException
     */
    public void singleSelect(Integer pipelineId, String ip)   {
        WindowData windowData = new WindowData(pipelineId, WindowType.s, ip, -1L);
        CommunicationClient communicationClient = CompomentManager.getInstance().getCommunicationClient();
        SelectAndLoadIp selectAndLoadIp = CompomentManager.getInstance().getMetaManager().getPipelineTaskIps().get(pipelineId);
        String selecterIp = selectAndLoadIp.getSelecterIp();
        String selecterport = selectAndLoadIp.getSelecterport();
        communicationClient.call(selecterIp, Integer.valueOf(selecterport),  new WindowEvent(windowData.getPipeLineId(),windowData.getWindowType(),windowData.getIp(),windowData.getBatchId()));

    }


    public void singleLoad(Integer pipelineId, String ip, long syncNumber) {

        WindowData windowData = new WindowData(pipelineId, WindowType.l, ip, syncNumber);
        CommunicationClient communicationClient = CompomentManager.getInstance().getCommunicationClient();
        SelectAndLoadIp selectAndLoadIp = CompomentManager.getInstance().getMetaManager().getPipelineTaskIps().get(pipelineId);
        String loadIp = selectAndLoadIp.getLoadIp();
        String loadPort = selectAndLoadIp.getLoadPort();
        communicationClient.call(loadIp, Integer.valueOf(loadPort),  new WindowEvent(windowData.getPipeLineId(),windowData.getWindowType(),windowData.getIp(),windowData.getBatchId()));
    }


}
