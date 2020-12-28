package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 23:03
 */
@Data
public class SelectTaskEvent extends Event {

    private Integer pipelineId;
    private String sourceRedises;





    private Integer parallelism;
    public SelectTaskEvent(){
        super(TaskEventType.startTask);
    }

    public SelectTaskEvent(Integer pipelineId,String sourceRedises, Integer parallelism) {
        super(TaskEventType.startTask);
        this.pipelineId = pipelineId;
        this.parallelism = parallelism;
        this.sourceRedises = sourceRedises;

    }
}
