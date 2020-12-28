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
public class StartTaskEvent extends Event {

    private String sourceRedises;





    private Integer parallelism;
    public  StartTaskEvent(){
        super(TaskEventType.startTask);
    }

    public StartTaskEvent(String sourceRedises, Integer parallelism) {
        super(TaskEventType.startTask);
        this.parallelism = parallelism;
        this.sourceRedises = sourceRedises;

    }
}
