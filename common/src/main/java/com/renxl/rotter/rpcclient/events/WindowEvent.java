package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 23:03
 */
@Data
public class WindowEvent extends Event {

    private Integer pipeLineId;
    private short windowType;
    private String ip ;

    private long batchId ;






    public WindowEvent(Integer pipelineId,short windowType ,String ip, long batchId) {
        super(TaskEventType.window);
        this.pipeLineId = pipelineId;
        this.windowType = windowType;
        this.ip = ip;
        this.batchId = batchId;

    }


}
