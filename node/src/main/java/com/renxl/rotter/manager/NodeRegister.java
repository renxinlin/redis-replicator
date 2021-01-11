package com.renxl.rotter.manager;

import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.zookeeper.ZKclient;
import com.renxl.rotter.zookeeper.ZookeeperConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.renxl.rotter.zookeeper.ZookeeperConfig.managerMasterParent;
import static com.renxl.rotter.zookeeper.ZookeeperConfig.nodeAddress;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:04
 */
@Slf4j
@Data
public class NodeRegister {

    public void init() {


        if (!ZKclient.instance.isNodeExist(managerMasterParent)) {
            ZKclient.instance.createNode(managerMasterParent, null);
        }

        if (!ZKclient.instance.isNodeExist(nodeAddress)) {
            ZKclient.instance.createNode(nodeAddress, null);
        }
        String nodeIp = CompomentManager.getInstance().getMetaManager().getNodeIp();
        int nodeDubboPort = CompomentManager.getInstance().getMetaManager().getNodeDubboPort();
        ZKclient.instance.createEphemeral(ZookeeperConfig.nodeAddress + "/" + nodeIp, nodeIp + ":" + nodeDubboPort);
    }



    public void destory() {

    }
}
