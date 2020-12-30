package com.renxl.rotter.register;

import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.zookeeper.ZKclient;
import com.renxl.rotter.zookeeper.ZookeeperConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.renxl.rotter.zookeeper.ZookeeperConfig.managerMasterParent;

/**
 * 基于zk的master选举
 */
@Slf4j
public class RegisterrWatcher {


    PathChildrenCache managerCache;

    public void initial() {
        final String hostAddress = AddressUtils.getHostAddress().getHostAddress();
        log.info("start register manager master [{}]", hostAddress);
        try {
            if (!ZKclient.instance.isNodeExist(managerMasterParent)) {
                ZKclient.instance.createNode(managerMasterParent, null);
            }
            ZKclient.instance.createEphemeral(ZookeeperConfig.managerMaster, hostAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CuratorFramework client = ZKclient.instance.getClient();

        try {
            // 创建了一个单线程池

            managerCache = new PathChildrenCache(client, managerMasterParent, true);
            PathChildrenCacheListener childrenCacheListener =
                    // 单线程！！！ 这里采用单线程来简化这个服务发现 注册的复杂性 从而保障正确性
                    new PathChildrenCacheListener() {
                        @Override
                        public void childEvent(CuratorFramework client,
                                               PathChildrenCacheEvent event) {
                            ChildData data = event.getData();
                            String managerAddress = new String(data.getData());
                            switch (event.getType()) {
                                case CHILD_REMOVED:
                                    try {
                                        ZKclient.instance.createEphemeral(ZookeeperConfig.managerMaster, hostAddress);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                default:
                                    break;
                            }


                        }
                    };
            managerCache.getListenable().addListener(childrenCacheListener);
            managerCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            log.error(" get manager error ", e);
        }


    }


    public void destory() {
        try {
            // 不在监听节点的变化
            managerCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 节点第一步不接受请求
        try {
            ZKclient.instance.getClient().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
