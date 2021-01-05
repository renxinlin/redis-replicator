package com.renxl.rotter.manager;

import com.alibaba.dubbo.common.json.JSON;
import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.sel.window.WindowData;
import com.renxl.rotter.sel.window.WindowType;
import com.renxl.rotter.sel.window.buffer.SelectWindowBuffer;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;
import com.renxl.rotter.zookeeper.ZKclient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static com.renxl.rotter.zookeeper.ZookeeperConfig.pipelineWindowId;
import static com.renxl.rotter.zookeeper.ZookeeperConfig.pipelineWindowTemp;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:04
 */
@Data
@Slf4j
public class WindowManager {

    private Map<Integer, WindowBuffer> sWindowBuffers = new HashMap();
    private Map<Integer, WindowBuffer> eWindowBuffers = new HashMap();
    private Map<Integer, WindowBuffer> lWindowBuffers = new HashMap();


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


    public static void singleExtract(Integer pipelineId)   {
        String pipelineWindowIdFormat = MessageFormat.format(pipelineWindowId, String.valueOf(pipelineId));
        String pipelineWindowTempFormat = MessageFormat.format(pipelineWindowTemp, String.valueOf(pipelineId));
        // 只起到唤醒作用 不做全局滑动窗口序列号
        long batchId = CompomentManager.getInstance().getIdWorker().nextId();
        String windowData = null;
        try {
            windowData = JSON.json(new WindowData(pipelineId, WindowType.e, AddressUtils.getHostAddress().getHostAddress(), batchId));
        } catch (IOException e) {
            log.info("json format error");
        }
        ZKclient.instance.createNodeSel(pipelineWindowTempFormat, windowData);
    }


    public static void singleSelect(Integer pipelineId, String ip) throws IOException {
        String pipelineWindowIdFormat = MessageFormat.format(pipelineWindowId, String.valueOf(pipelineId));
        String pipelineWindowTempFormat = MessageFormat.format(pipelineWindowTemp, String.valueOf(pipelineId));
        // 既起到唤醒作用 也做全局滑动窗口序列号控制顺序
        long batchId = CompomentManager.getInstance().getIdWorker().nextId();
        String windowData = JSON.json(new WindowData(pipelineId, WindowType.s, ip, batchId));
        ZKclient.instance.createNodeSel(pipelineWindowTempFormat, windowData);
    }


    public static void singleLoad(Integer pipelineId, String ip)     {
        String pipelineWindowIdFormat = MessageFormat.format(pipelineWindowId, String.valueOf(pipelineId));
        String pipelineWindowTempFormat = MessageFormat.format(pipelineWindowTemp, String.valueOf(pipelineId));
        // 只起到唤醒作用 不做全局滑动窗口序列号
        long batchId = CompomentManager.getInstance().getIdWorker().nextId();
        String windowData = null;
        try {
            windowData = JSON.json(new WindowData(pipelineId, WindowType.l, ip, batchId));
        } catch (IOException e) {
            log.info("json format error");
        }
        ZKclient.instance.createNodeSel(pipelineWindowTempFormat, windowData);
    }


}
