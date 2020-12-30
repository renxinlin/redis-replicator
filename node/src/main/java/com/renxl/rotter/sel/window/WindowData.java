package com.renxl.rotter.sel.window;

import com.renxl.rotter.common.AddressUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 20:45
 */
@Data
@AllArgsConstructor
public class WindowData implements Serializable {
    private Integer pipeLineId;
    private short windowType;
    private String ip ;

    private long batchId ;
}
