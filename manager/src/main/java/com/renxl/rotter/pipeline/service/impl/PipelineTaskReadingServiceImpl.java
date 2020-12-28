package com.renxl.rotter.pipeline.service.impl;

import com.renxl.rotter.pipeline.domain.PipelineTaskReading;
import com.renxl.rotter.pipeline.mapper.PipelineTaskReadingMapper;
import com.renxl.rotter.pipeline.service.IPipelineTaskReadingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * HA 优先跨机房选择
 服务实现类
 * </p>
 *
 * @author renxl
 * @since 2020-12-28
 */
@Service
public class PipelineTaskReadingServiceImpl extends ServiceImpl<PipelineTaskReadingMapper, PipelineTaskReading> implements IPipelineTaskReadingService {

}
