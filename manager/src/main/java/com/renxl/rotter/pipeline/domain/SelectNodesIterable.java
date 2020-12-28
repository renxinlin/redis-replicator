package com.renxl.rotter.pipeline.domain;

import lombok.AllArgsConstructor;

import java.util.Iterator;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 11:37
 */
public class SelectNodesIterable implements Iterable<String> {
    private String[] nodes;
    private int size;

    public SelectNodesIterable(String[] nodes){
        this.nodes = nodes;
        this.size = nodes.length;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {

            private int curror = -1;

            public boolean hasNext() {
                return curror + 1 < size;
            }

            public String next() {
                curror++;
                return nodes[curror];
            }
        };

    }
}
