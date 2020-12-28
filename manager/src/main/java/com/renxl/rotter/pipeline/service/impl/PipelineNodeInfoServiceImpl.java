package com.renxl.rotter.pipeline.service.impl;

import com.renxl.rotter.pipeline.domain.PipelineNodeInfo;
import com.renxl.rotter.pipeline.mapper.PipelineNodeInfoMapper;
import com.renxl.rotter.pipeline.service.IPipelineNodeInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * HA记录 服务实现类
 * </p>
 *
 * @author renxl
 * @since 2020-12-25
 */
@Service
public class PipelineNodeInfoServiceImpl extends ServiceImpl<PipelineNodeInfoMapper, PipelineNodeInfo> implements IPipelineNodeInfoService {

}
