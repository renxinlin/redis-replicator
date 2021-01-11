package com.renxl.rotter.domain;


import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.renxl.rotter.constants.Constants;
import com.renxl.rotter.zookeeper.ZKclient;
import lombok.Data;
import sun.jvm.hotspot.opto.Node;

import java.util.List;
import java.util.stream.Collectors;

import static com.renxl.rotter.zookeeper.ZookeeperConfig.nodeAddress;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-11 16:07
 */
@Data
public class NodeInfo {
    private String ip;
    private int port;


    public NodeInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;


    }

    public static List<NodeInfo> getNodes() {

        List<String> nodes = ZKclient.instance.getChild(nodeAddress);

        if(CollectionUtils.isEmpty(nodes)){
            return null;
        }
        List<NodeInfo> collect = nodes.stream().map(node -> {
            String[] ipAndPort = node.split(Constants.IP_PORT_SPLIT);
            return new NodeInfo(ipAndPort[0], Integer.valueOf(ipAndPort[1]));
        }).collect(Collectors.toList());
        return collect;
    }
}
