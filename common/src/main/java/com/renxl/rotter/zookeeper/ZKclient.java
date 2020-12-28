package com.renxl.rotter.zookeeper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.StringUtils;

/**
 *
 **/
@Slf4j
@Data
public class ZKclient {


    public static ZKclient instance = null;

    static {
        instance = new ZKclient();
        instance.init();
    }

    private CuratorFramework client;

    private ZKclient() {

    }

    public void init() {

        if (null != client) {
            return;
        }
        //创建客户端
        client = ClientFactory.createSimple(ZookeeperConfig.getInstance().getAddress());

        //启动客户端实例,连接服务器
        client.start();
    }

    public void destroy() {
        CloseableUtils.closeQuietly(client);
    }


    /**
     * 创建节点
     */
    public void createNode(String zkPath, String data) {
        byte[] payload = null;
        try {
            // 创建一个 ZNode 节点
            // 节点的数据为 payload
            if (!StringUtils.isEmpty(data)) {

                payload = data.getBytes("UTF-8");
            }

            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(zkPath, payload);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 检查节点
     */
    public boolean isNodeExist(String zkPath) {
        try {

            Stat stat = client.checkExists().forPath(zkPath);
            if (null == stat) {
                log.info("节点不存在:{}", zkPath);
                return false;
            } else {

                log.info("节点存在 stat is:{}", stat.toString());
                return true;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public String createEphemeralSeqNode(String srcpath, byte[] data) {
        try {
            // 创建一个 ZNode 节点
            String path = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(srcpath, data);

            return path;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
