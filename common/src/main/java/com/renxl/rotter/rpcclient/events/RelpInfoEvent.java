package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 13:49
 */
@Data
public class RelpInfoEvent extends Event {

    private Integer pipelineId;

    public RelpInfoEvent(Integer pipelineId) {
        super(TaskEventType.relpInfo);
        this.pipelineId = pipelineId;
    }

}
