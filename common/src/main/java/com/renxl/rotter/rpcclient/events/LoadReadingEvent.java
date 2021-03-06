package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 15:51
 */
@Data
public class LoadReadingEvent extends Event {
    private Integer pipelineId;
    private String nodeIp;



    public LoadReadingEvent(Integer pipelineId,String nodeIp) {
        super(TaskEventType.loadReading);
        this.pipelineId = pipelineId;
        this.nodeIp = nodeIp;
    }
}
