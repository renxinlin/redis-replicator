package com.renxl.rotter.monitor;

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
import java.io.UnsupportedEncodingException;

/**
 * manager调用node执行同步任务 如果node可以工作在此处监听
 *
 * @description:
 * @author: renxl
 * @create: 2020-12-26 22:45
 */

@Slf4j
public class RotterNodeWorkMonitor {

    PathChildrenCache selectCache;
    PathChildrenCache loadCache;

    public void init() {

        String selectNodeReadingPath = ZookeeperConfig.getInstance().getSelectNodeReadingPath();
        boolean isExist = ZKclient.instance.isNodeExist(selectNodeReadingPath);
        if (!isExist) {
            ZKclient.instance.createNode(selectNodeReadingPath, null);
        }


        String loadNodeReadingPath = ZookeeperConfig.getInstance().getLoadNodeReadingPath();
        isExist = ZKclient.instance.isNodeExist(loadNodeReadingPath);
        if (!isExist) {
            ZKclient.instance.createNode(loadNodeReadingPath, null);
        }
        CuratorFramework client = ZKclient.instance.getClient();

        try {
            // 创建了一个单线程池
            selectCache = new PathChildrenCache(client, selectNodeReadingPath, true);
            PathChildrenCacheListener childrenCacheListener =
                    // 单线程！！！ 这里采用单线程来简化这个服务发现 注册的复杂性 从而保障正确性
                    new PathChildrenCacheListener() {
                        @Override
                        public void childEvent(CuratorFramework client,
                                               PathChildrenCacheEvent event) {
                            try {
                                ChildData data = event.getData();
                                switch (event.getType()) {
                                    case INITIALIZED:

                                        log.info("select node , path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));
                                        break;

                                    case CHILD_ADDED:

                                        log.info("子节点增加, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));
                                        break;
                                    case CHILD_UPDATED:
                                        log.info("子节点更新, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));


                                        break;
                                    case CHILD_REMOVED:
                                        log.info("子节点删除, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));


                                        break;
                                    default:
                                        break;
                                }

                            } catch (
                                    UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    };
            selectCache.getListenable().addListener(childrenCacheListener);
            /*
             * StartMode：初始化方式
             * POST_INITIALIZED_EVENT：异步初始化。初始化后会触发事件
             * NORMAL：异步初始化
             * BUILD_INITIAL_CACHE：同步初始化
             * */

            selectCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            log.error("exception when monitor rotter select node ", e);
        }


        try {
            // 创建了一个单线程池
            loadCache = new PathChildrenCache(client, loadNodeReadingPath, true);
            PathChildrenCacheListener childrenCacheListener =
                    // 单线程！！！ 这里采用单线程来简化这个服务发现 注册的复杂性 从而保障正确性
                    new PathChildrenCacheListener() {
                        @Override
                        public void childEvent(CuratorFramework client,
                                               PathChildrenCacheEvent event) {
                            try {
                                ChildData data = event.getData();
                                switch (event.getType()) {
                                    case INITIALIZED:

                                        log.info("子节点初始化成功, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));
                                        break;

                                    case CHILD_ADDED:

                                        log.info("子节点增加, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));
                                        break;
                                    case CHILD_UPDATED:
                                        log.info("子节点更新, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));


                                        break;
                                    case CHILD_REMOVED:
                                        log.info("子节点删除, path={}, data={}",
                                                data.getPath(), new String(data.getData(), "UTF-8"));


                                        break;
                                    default:
                                        break;
                                }

                            } catch (
                                    UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    };
            loadCache.getListenable().addListener(childrenCacheListener);
            loadCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            log.error("exception when monitor rotter select node ", e);
        }
    }


    public void destory() {
        try {
            // 不在监听节点的变化
            selectCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // 不在监听节点的变化
            loadCache.close();
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
