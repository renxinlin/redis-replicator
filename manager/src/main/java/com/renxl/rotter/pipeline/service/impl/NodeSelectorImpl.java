package com.renxl.rotter.pipeline.service.impl;

import com.renxl.rotter.pipeline.service.INodeSelector;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.events.PingEvent;
import com.renxl.rotter.rpcclient.events.PongResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 13:48
 */
@Slf4j
@Service
public class NodeSelectorImpl implements INodeSelector {
    @Autowired
    CommunicationClient communicationClient;

    @Override
    public String getRuningNode(List<String> nodes, String lastRuningNode) {
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        if(StringUtils.isEmpty(lastRuningNode)){
            nodes.remove(lastRuningNode);
        }
        // 使得所有的节点随机的进行工作
        Collections.shuffle(nodes);
        lastRuningNode = nodes.get(0);
        try {
            PongResponse pongResponse = (PongResponse) communicationClient.call(lastRuningNode, new PingEvent());
            return lastRuningNode;
        } catch (Exception e) {
            log.error("send ping to ["+lastRuningNode+"] no response [pong]",e);
           return getRuningNode(nodes, lastRuningNode);
        }

    }
}
