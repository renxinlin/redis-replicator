package com.renxl.rotter.pipeline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.renxl.rotter.pipeline.domain.PipelineConfig;
import com.renxl.rotter.pipeline.framework.RotterResponse;

/**
 * <p>
 * HA 优先跨机房选择
 * 服务类
 * </p>
 *
 * @author renxl
 * @since 2020-12-25
 */
public interface IPipelineConfigService extends IService<PipelineConfig> {
    /**
     * 添加redis同步配置
     *
     * @param pipelineConfig
     * @return
     */
    RotterResponse<Void> addPipelineConfig(PipelineConfig pipelineConfig);

    /**
     * 删除redis同步配置
     * todo 检查同步配置是否可以被删除
     *
     * @param id
     */
    void deleteById(Integer id);

    /**
     * 启动pipeline
     *
     * @param id
     */
    void start(Integer id);

    /**
     * 更新同步任务配置
     * 由于pipeline 启动后会存在同步进度相关元信息 所以只支持增删 不支持修改
     *
     * @param pipelineConfig
     * @return
     */
    @Deprecated
    RotterResponse<Void> updatePipelineConfig(PipelineConfig pipelineConfig);

    /**
     * 停止同步任务
     * @param id
     */
    void stop(Integer id);
}
