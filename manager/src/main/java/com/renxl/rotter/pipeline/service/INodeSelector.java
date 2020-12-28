package com.renxl.rotter.pipeline.service;

import java.util.List;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 13:47
 */
public interface INodeSelector {
    /**
     * 迭代获取可以通信的节点
     * @param nodes
     * @param lastNode
     * @return
     */
    String getRuningNode(List<String> nodes,String lastNode);
}
