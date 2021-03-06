/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.renxl.rotter.rpcclient;

import lombok.Data;

/**
 * 心跳检查事件
 * 
 * @author jianghang
 */
@Data
public class NodeHeartEvent extends Event {

    private static final long serialVersionUID = 8690886624112649424L;


    private String nodeIp;

    // type
    public NodeHeartEvent(String ip){
        super(HeartEventType.nodeHeartBeat);
        nodeIp = ip;
    }

    private Byte heart = 1;

    public static enum HeartEventType implements EventType {
        nodeHeartBeat;
    }

    public Byte getHeart() {
        return heart;
    }

    public void setHeart(Byte heart) {
        this.heart = heart;
    }

}
