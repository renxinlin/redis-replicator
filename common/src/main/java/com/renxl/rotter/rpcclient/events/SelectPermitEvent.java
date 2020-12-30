package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 15:51
 */
@Data
public class SelectPermitEvent extends Event {
    private  Integer pipelineId;
    private String nodeIp;

    public SelectPermitEvent(Integer pipelineId,String nodeIp) {
        super(TaskEventType.selectPermit);
        this.pipelineId = pipelineId;
        this.nodeIp = nodeIp;
    }

}
