package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-07 18:59
 */
@Data
public class GetExtractBatchEvent extends Event {

    private Integer pipelineId;

    private Long seqNumber;

    public GetExtractBatchEvent(Integer pipelineId, Long seqNumber ) {
        super(TaskEventType.getExtractBatch);
        this.pipelineId = pipelineId;
        this.seqNumber = seqNumber;
    }
}
