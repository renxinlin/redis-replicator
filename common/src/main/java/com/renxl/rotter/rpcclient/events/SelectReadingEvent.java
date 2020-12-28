package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 15:51
 */
@Data
public class SelectReadingEvent extends Event {
    private  Integer pipelineId;
    private Integer nodeId;
    private String nodeIp;

}
