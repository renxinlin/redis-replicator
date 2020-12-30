package com.renxl.rotter.pipeline.service.impl;

import com.renxl.rotter.pipeline.domain.PipelineSyncInfo;
import com.renxl.rotter.pipeline.mapper.PipelineSyncInfoMapper;
import com.renxl.rotter.pipeline.service.IPipelineSyncInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * HA记录 服务实现类
 * </p>
 *
 * @author renxl
 * @since 2020-12-30
 */
@Service
public class PipelineSyncInfoServiceImpl extends ServiceImpl<PipelineSyncInfoMapper, PipelineSyncInfo> implements IPipelineSyncInfoService {

}
