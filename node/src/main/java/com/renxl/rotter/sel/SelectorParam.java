package com.renxl.rotter.sel;

import com.renxl.rotter.rpcclient.events.RelpInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 20:39
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SelectorParam {

    private Integer pipelineId;

    private String sourceRedises;
    private Integer parallelism;
    private RelpInfoResponse relpInfoResponse;
}
