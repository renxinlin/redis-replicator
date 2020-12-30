package com.renxl.rotter.pipeline.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * HA记录
 * </p>
 *
 * @author renxl
 * @since 2020-12-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PipelineSyncInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    private LocalDateTime gmtCreated;

    private LocalDateTime gmtModified;

    private Long deleteTimestamp;

    /**
     * 0未删除1删除
     */
    private Integer deleteMark;

    /**
     * 同步信息
     */
    private String replJson;


}
