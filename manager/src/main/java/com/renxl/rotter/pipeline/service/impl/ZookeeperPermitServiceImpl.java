package com.renxl.rotter.pipeline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.renxl.rotter.pipeline.domain.PipelineTaskReading;
import com.renxl.rotter.pipeline.mapper.PipelineTaskReadingMapper;
import com.renxl.rotter.pipeline.service.IPermitService;
import com.renxl.rotter.zookeeper.ZKclient;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

/**
 * <p>
 * HA 优先跨机房选择
 * 服务实现类
 * </p>
 *
 * @author renxl
 * @since 2020-12-28
 */
@Service
public class ZookeeperPermitServiceImpl extends ServiceImpl<PipelineTaskReadingMapper, PipelineTaskReading> implements IPermitService {

    /**
     * 尾缀为pipelineId
     */
    private String permitSelectNodes = "/rotter/permit/select/{0}";

    /**
     * 尾缀为pipelineId
     */
    private String permitLoadNodes = "/rotter/permit/load/{0}";;

    @Override
    public void permit(Integer pipelineId) {
        String permitSelectInfo = MessageFormat.format(permitSelectNodes, String.valueOf(pipelineId));
        String permitLoadInfo = MessageFormat.format(permitLoadNodes, String.valueOf(pipelineId));
        // select 允许
        ZKclient.instance.createNode(permitSelectInfo,null);
        // load 允许
        ZKclient.instance.createNode(permitLoadInfo,null);

    }
}
