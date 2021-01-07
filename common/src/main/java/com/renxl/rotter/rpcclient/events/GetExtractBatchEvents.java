package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-07 18:59
 */
@Data
public class GetExtractBatchEvents extends Event {

    private Integer pipelineId;

    private Long seqNumber;

    public GetExtractBatchEvents(Integer pipelineId,Long seqNumber ) {
        super(TaskEventType.getExtractBatchEvents);
        this.pipelineId = pipelineId;
        this.seqNumber = seqNumber;
    }
}
