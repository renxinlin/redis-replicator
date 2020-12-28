package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 23:03
 */
@Data
public class LoadTaskEvent extends Event {
    private String targetRedis;

    public LoadTaskEvent(){
        super(TaskEventType.loadTask);
    }

    public LoadTaskEvent(String targetRedis) {
        super(TaskEventType.loadTask);

        this.targetRedis = targetRedis;
    }
}
