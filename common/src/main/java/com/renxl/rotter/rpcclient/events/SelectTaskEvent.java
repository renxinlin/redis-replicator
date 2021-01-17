package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 23:03
 */
@Data
public class SelectTaskEvent extends Event {

    private Integer pipelineId;


    private Integer isMaster;
    private String sourceRedises;





    private Integer parallelism;
    public SelectTaskEvent(){
        super(TaskEventType.selectTask);
    }

    public SelectTaskEvent(Integer pipelineId,Integer isMaster,String sourceRedises, Integer parallelism) {
        super(TaskEventType.selectTask);
        this.pipelineId = pipelineId;
        this.parallelism = parallelism;
        this.sourceRedises = sourceRedises;
        this.isMaster = isMaster;

    }
}
