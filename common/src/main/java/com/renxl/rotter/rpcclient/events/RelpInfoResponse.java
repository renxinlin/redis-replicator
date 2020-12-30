package com.renxl.rotter.rpcclient.events;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 15:08
 */
@Data
public class RelpInfoResponse  implements Serializable {

    private static final long serialVersionUID = 208038167977229245L;



    /**
     * 管道id
     */
    private Long pipelineId;

    /**
     * 最后一次负责的复制id
     */
    private String replid;

    /**
     * 复制偏移量
     */
    private String offset;

    /**
     * 同步信息
     */
    private String replJson;

}
