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
@AllArgsConstructor
public class SelectPermitEvent extends Event {
    private  Integer pipelineId;
    private String nodeIp;

}
