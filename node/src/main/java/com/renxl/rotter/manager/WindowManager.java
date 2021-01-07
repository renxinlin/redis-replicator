package com.renxl.rotter.manager;

import com.alibaba.dubbo.common.json.JSON;
import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.config.CompomentManager;
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
        String pipelineWindowTempFormat = MessageFormat.format(pipelineWindowTemp, String.valueOf(pipelineId));
        // 只起到唤醒作用 不做全局滑动窗口序列号
        String windowData = null;
        try {
            windowData = JSON.json(new WindowData(pipelineId, WindowType.e, AddressUtils.getHostAddress().getHostAddress(), syncNumber));
        } catch (IOException e) {
            log.info("json format error");
        }
        ZKclient.instance.createNodeSel(pipelineWindowTempFormat, windowData);
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
    public void singleSelect(Integer pipelineId, String ip) throws IOException {
        // load 保障了syncNumber 的消费顺序 select不再增加重复确定
        String pipelineWindowTempFormat = MessageFormat.format(pipelineWindowTemp, String.valueOf(pipelineId));
        // 只起到唤醒作用 不做全局滑动窗口序列号
        String windowData = null;
        long syncNumber = CompomentManager.getInstance().getWindowSeqGenerator().gene(pipelineId);
        try {
            windowData = JSON.json(new WindowData(pipelineId, WindowType.l, ip, syncNumber));
        } catch (IOException e) {
            log.info("json format error");
        }
        ZKclient.instance.createNodeSel(pipelineWindowTempFormat, windowData);

    }


    public void singleLoad(Integer pipelineId, String ip, long syncNumber) {
        String pipelineWindowTempFormat = MessageFormat.format(pipelineWindowTemp, String.valueOf(pipelineId));
        // 只起到唤醒作用 不做全局滑动窗口序列号
        String windowData = null;
        try {
            windowData = JSON.json(new WindowData(pipelineId, WindowType.l, ip, syncNumber));
        } catch (IOException e) {
            log.info("json format error");
        }
        ZKclient.instance.createNodeSel(pipelineWindowTempFormat, windowData);
    }


}
