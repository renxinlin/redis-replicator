package com.renxl.rotter.pipeline.controller;


import com.renxl.rotter.pipeline.domain.PipelineConfig;
import com.renxl.rotter.pipeline.framework.RotterResponse;
import com.renxl.rotter.pipeline.service.IPipelineConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * redis
 * node
 * 都不做控制
 * <p>
 * <p>
 * 配置上去就走起
 * canal instance需要单独被使用  这里不需要 控制
 *
 * @author renxl
 * @since 2020-12-25
 */
@RestController
@RequestMapping("/pipelineConfig")
public class PipelineConfigController {
    @Autowired
    private IPipelineConfigService pipelineConfigService;

    /**
     * 获取redis同步配置信息
     *
     * @param id
     * @return
     */
    @GetMapping("get")
    @ResponseBody
    public RotterResponse<PipelineConfig> get(Integer id) {
        PipelineConfig pipelineConfig = pipelineConfigService.getById(id);
        return RotterResponse.success(pipelineConfig);
    }


    /**
     * 添加redis 同步配置信息
     *
     * @param
     * @return
     */
    @GetMapping("add")
    @ResponseBody
    public RotterResponse<Void> add(PipelineConfig pipelineConfig) {
        RotterResponse<Void> response = pipelineConfigService.addPipelineConfig(pipelineConfig);
        return response;
    }




    /**
     * 删除redis同步配置信息
     *
     * @param id
     * @return
     */
    @GetMapping("delete")
    public RotterResponse<Void> delete(Integer id) {
        pipelineConfigService.deleteById(id);
        return RotterResponse.success();
    }





    /**
     * 启动redis同步配置信息
     *
     * @param id
     * @return
     */
    @GetMapping("start")
    public RotterResponse<Void> start(Integer id) {
        pipelineConfigService.start(id);
        return RotterResponse.success();
    }


    /**
     * 停止redis同步配置信息
     *
     * @param id
     * @return
     */
    @GetMapping("stop")
    public RotterResponse<Void> stop(Integer id) {
        pipelineConfigService.stop(id);
        return RotterResponse.success();
    }

}
