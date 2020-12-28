package com.renxl.rotter.pipeline.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * HA 优先跨机房选择
 *
 * </p>
 *
 * @author renxl
 * @since 2020-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PipelineTaskReading implements Serializable {
    public static final Integer reading = 1;
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    private Long pipelineId;


    /**
     * 0未准备1 准备就绪
     */
    private Integer selectReading;

    /**
     * 0未准备1 准备就绪
     */
    private Integer loadReading;

    private LocalDateTime gmtCreated;

    private LocalDateTime gmtModified;

    /**
     * 0未删除1删除
     */
    private Integer deleteMark = 0;

    private Long deleteTimestamp = 0L ;


    public boolean isLoadReading() {
        return reading.equals(loadReading);
    }

    public boolean isselectReading() {
        return reading.equals(selectReading);
    }

    public void initSelectReading(Integer pipelineId) {
        selectReading = reading;
        this.pipelineId = pipelineId.longValue();
    }



    public void initLoadReading(Integer pipelineId) {
        loadReading = reading;
        this.pipelineId = pipelineId.longValue();

    }
}
