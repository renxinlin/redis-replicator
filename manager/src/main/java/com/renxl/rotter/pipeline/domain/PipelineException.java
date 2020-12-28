package com.renxl.rotter.pipeline.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2020-12-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PipelineException implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 管道id
     */
    private Long pipelineId;

    /**
     * 最后一次执行同步任务的机器再次执行时候不会选
     */
    private String lastSelectNode;

    /**
     * 最后一次执行同步任务的机器，再次执行时候不会选择该机器
     */
    private Integer lastNodeNode;


}
