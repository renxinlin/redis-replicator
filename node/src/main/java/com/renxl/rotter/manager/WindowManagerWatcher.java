package com.renxl.rotter.manager;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.ParseException;
import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.common.IdWorker;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.sel.window.WindowData;
import com.renxl.rotter.sel.window.WindowType;
import com.renxl.rotter.zookeeper.ZKclient;
import com.renxl.rotter.zookeeper.ZookeeperConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.data.Id;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static com.renxl.rotter.zookeeper.ZookeeperConfig.*;

/**
 * 监控zk上的manager交互
 */
@Slf4j
public class WindowManagerWatcher {


    public Map<Integer, PathChildrenCache> pipelinedWatcher = new HashMap<>();


    public void init(){
        try {
            if (!ZKclient.instance.isNodeExist(pipelineWindowParent)) {
                ZKclient.instance.createNode(pipelineWindowParent, null);
            }

            if (!ZKclient.instance.isNodeExist(pipelineWindow)) {
                ZKclient.instance.createNode(pipelineWindow, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * select存在并行度
     * load不存在
     * @param pipelineId
     * @param parallel
     */
    public void initPipelined(Integer pipelineId,Integer parallel) {
        if(pipelinedWatcher.get(pipelineId)!=null){
            return;
        }

        // select 初始化
        if(parallel!=null){
            String pipelineWindowIdFormat = MessageFormat.format(pipelineWindowId, String.valueOf(pipelineId));
            String pipelineWindowTempFormat = MessageFormat.format(pipelineWindowTemp, String.valueOf(pipelineId));
            try {
                if (!ZKclient.instance.isNodeExist(pipelineWindowId)) {
                    ZKclient.instance.createNode(pipelineWindowId, null);
                }
                // TODO 测试
                ZKclient.instance.deleteChild(pipelineWindowIdFormat);
                int i = 0;
                // 创建滑动窗口大小
                while (i<parallel) {
                    i++;
                    // 滑动窗口递增值
                    long batchId = CompomentManager.getInstance().getIdWorker().nextId();

                    String windowData = JSON.json(new WindowData(pipelineId, WindowType.s, AddressUtils.getHostAddress().getHostAddress(),batchId));

                    ZKclient.instance.createNodeSel(pipelineWindowTempFormat, windowData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        PathChildrenCache windowWatcher;
        CuratorFramework client = ZKclient.instance.getClient();

        try {
            // 创建了一个单线程池

            windowWatcher = new PathChildrenCache(client, managerMasterParent, true);
            PathChildrenCacheListener childrenCacheListener =
                    // 单线程！！！ 这里采用单线程来简化这个服务发现 注册的复杂性 从而保障正确性
                    new PathChildrenCacheListener() {
                        @Override
                        public void childEvent(CuratorFramework client,
                                               PathChildrenCacheEvent event) {
                            ChildData data = event.getData();

                            String windowDataStr = new String(data.getData());
                            WindowData windowData = null;
                            try {
                                windowData = JSON.parse(windowDataStr,WindowData.class);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            switch (event.getType()) {
                                case INITIALIZED:

                                    break;

                                case CHILD_ADDED:
                                    CompomentManager.getInstance().onUpdateWindow(windowData);
                                    break;
                                case CHILD_UPDATED:
                                    CompomentManager.getInstance().onUpdateWindow(windowData);
                                    break;

                                case CHILD_REMOVED:
                                    break;

                                default:
                                    break;
                            }


                        }
                    };
            windowWatcher.getListenable().addListener(childrenCacheListener);
            windowWatcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            pipelinedWatcher.put(pipelineId,windowWatcher);
        } catch (Exception e) {
            log.error(" get manager error ", e);
        }


    }


    public void destory() {

        for (PathChildrenCache integerPathChildrenCacheEntry : pipelinedWatcher.values()) {
            try {
                integerPathChildrenCacheEntry.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        // 节点第一步不接受请求
        try {
            ZKclient.instance.getClient().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
