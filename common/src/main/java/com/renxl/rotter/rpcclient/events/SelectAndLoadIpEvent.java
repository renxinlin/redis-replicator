package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.Event;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-07 16:19
 */
@Data
public class SelectAndLoadIpEvent extends Event {


    private Integer pipelineId;
    private String selectorIp;
    private String loadIp;
    public SelectAndLoadIpEvent(Integer pipelineId,
                                String selectorIp,
                                String loadIp) {
        super(TaskEventType.selectAndLoadIp);
        this.pipelineId = pipelineId;
        this.selectorIp = selectorIp;
        this.loadIp = loadIp;

    }
}
