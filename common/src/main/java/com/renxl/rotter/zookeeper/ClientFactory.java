package com.renxl.rotter.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 *
 * 采用Curator代替原生api需要反复注册等问题
 * renxl
 */
public class ClientFactory {

    /**
     * @param connectionString zk的连接地址
     * @return CuratorFramework 实例
     */
    public static CuratorFramework createSimple(String connectionString) {
        // 重试策略:第一次重试等待1s，第二次重试等待2s，第三次重试等待4s
        // 第一个参数：等待时间的基础单位，单位为毫秒
        // 第二个参数：最大重试次数
        ExponentialBackoffRetry retryPolicy =
                new ExponentialBackoffRetry(1000, 3);
        // 默认的会话超时时间 节点下线的感知太慢  TODO  生产发布把休眠时间拉到会话超时之后 会话超时60s  【连接超时15s】

        //  DEFAULT_SESSION_TIMEOUT_MS, DEFAULT_CONNECTION_TIMEOUT_MS 60 15
        //  连接断开超过15秒 认为连接真的断开
        // 会话断开则会删除临时节点
        return CuratorFrameworkFactory.newClient(connectionString,retryPolicy);
    }


}

/**
 *
 *
 * 关于connection timeout 的重试策略
 * ExponentialBackoffRetry	重试策略，在重试间隔增加睡眠时间的情况下重试一定次数
 * RetryNTimes	重试指定次数
 * RetryOneTime	仅仅重试一次
 * RetryUntilElapsed	一种重试策略，在给定的时间段内重试
 *
 *
 *
 *
 *
 *
 *
 */
