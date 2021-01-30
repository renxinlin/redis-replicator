package com.renxl.rotter.manager;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.ParseException;
import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.rpcclient.CommunicationRegistry;
import com.renxl.rotter.rpcclient.events.SelectTaskEvent;
import com.renxl.rotter.rpcclient.events.TaskEventType;
import com.renxl.rotter.rpcclient.events.WindowEvent;
import com.renxl.rotter.sel.window.WindowData;
import com.renxl.rotter.sel.window.WindowType;
import com.renxl.rotter.zookeeper.ZKclient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.renxl.rotter.zookeeper.ZookeeperConfig.*;

/**
 * 监控zk上的manager交互
 */
@Slf4j
public class WindowManagerWatcher {

    public WindowManagerWatcher(){
        CommunicationRegistry.regist(TaskEventType.window,this);
    }

    /**
     * 滑动窗口变更监听
     * @param windowEvent
     */
    public void onWindow(WindowEvent windowEvent) {
        WindowData windowData = new WindowData(windowEvent.getPipeLineId(),windowEvent.getWindowType(),windowEvent.getIp(),windowEvent.getBatchId());
        CompomentManager.getInstance().onUpdateWindow(windowData);
    }


    public void init() {


    }

    /**
     * select存在并行度
     * load不存在
     *
     * @param pipelineId
     * @param parallel
     */
    public void initPipelined(Integer pipelineId, Integer parallel) {
        // select 初始化
        if (parallel != null) {
            try {
                int i = 0;
                // 创建滑动窗口大小
                while (i < parallel) {
                    i++;
                    // 滑动窗口递增值
                    long batchId = -1L;
                    WindowData windowData = new WindowData(pipelineId, WindowType.s, CompomentManager.getInstance().getMetaManager().getNodeIp(), batchId);
                    CompomentManager.getInstance().onUpdateWindow(windowData);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


    public void destory() {


    }


}
