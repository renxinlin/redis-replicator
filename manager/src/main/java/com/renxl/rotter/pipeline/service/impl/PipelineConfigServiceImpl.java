package com.renxl.rotter.pipeline.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.renxl.rotter.config.Configs;
import com.renxl.rotter.pipeline.domain.PipelineConfig;
import com.renxl.rotter.pipeline.domain.PipelineNodeInfo;
import com.renxl.rotter.pipeline.framework.Asserts;
import com.renxl.rotter.pipeline.framework.RotterException;
import com.renxl.rotter.pipeline.framework.RotterResponse;
import com.renxl.rotter.pipeline.mapper.PipelineConfigMapper;
import com.renxl.rotter.pipeline.service.INodeSelector;
import com.renxl.rotter.pipeline.service.IPipelineConfigService;
import com.renxl.rotter.pipeline.service.IPipelineNodeInfoService;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.events.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * HA 优先跨机房选择
 * 服务实现类
 * </p>
 *
 * @author renxl
 * @since 2020-12-25
 */
@Service
public class PipelineConfigServiceImpl extends ServiceImpl<PipelineConfigMapper, PipelineConfig> implements IPipelineConfigService {
    @Autowired
    INodeSelector iNodeSelector;


    @Autowired
    CommunicationClient communicationClient;


    @Autowired
    IPipelineNodeInfoService iPipelineNodeInfoService;


    @Override
    public RotterResponse<Void> addPipelineConfig(PipelineConfig pipelineConfig) {
        pipelineConfig.checkTarget();
        pipelineConfig.addPort();
        baseMapper.insert(pipelineConfig);
        return RotterResponse.success();
    }

    @Override
    public void deleteById(Integer id) {
        PipelineConfig pipelineConfig = baseMapper.selectById(id);
        Asserts.check(!pipelineConfig.isStart(), RotterResponse.BizCodeAndMsg.PIPLINED_STARTED);
        baseMapper.deleteById(id);
    }

    // 不同于otter抢占式工作设计 Rotter的工作调度采用分发确认机制,绝对服从manager
    // 整个通信还是基于SEDA 阶段式事件驱动 模型
    @Override
    public void start(Integer id) {
        PipelineConfig pipelineConfig = baseMapper.selectById(id);
        Asserts.check(!pipelineConfig.isStart(), RotterResponse.BizCodeAndMsg.PIPLINED_STARTED);


        PipelineNodeInfo one = iPipelineNodeInfoService.getOne(iPipelineNodeInfoService.lambdaQuery().eq(PipelineNodeInfo::getPipelineId, pipelineConfig.getId()).getWrapper());
        if(one!=null){
            iPipelineNodeInfoService.getBaseMapper().deleteById(one.getId());
        }
        // 获取能够正常启动的load todo 获取最后的节点 过滤 如果只有一个 就只能选定了
        String selectNode = iNodeSelector.getRuningNode(pipelineConfig.getSelectNodeList(), null);
        String loadNode = iNodeSelector.getRuningNode(pipelineConfig.getLoadNodeList(), null);
        Asserts.check(!StringUtils.isEmpty(selectNode), RotterResponse.BizCodeAndMsg.PING_ERRPR);
        Asserts.check(!StringUtils.isEmpty(loadNode), RotterResponse.BizCodeAndMsg.PING_ERRPR);

        // todo 存储最后执行的机器 如果最后有 则先删除后更新
        PipelineNodeInfo pipelineNodeInfo = new PipelineNodeInfo();
        pipelineNodeInfo.init(pipelineConfig.getId(),selectNode,loadNode);
        iPipelineNodeInfoService.save(pipelineNodeInfo);

        // 通知node节点启动准备资源
        communicationClient.call(selectNode, Configs.dubboNodePort, new SelectTaskEvent(pipelineConfig.getId().intValue(),pipelineConfig.getSourceRedises(),pipelineConfig.getParallelism()));
        communicationClient.call(loadNode, Configs.dubboNodePort, new LoadTaskEvent(pipelineConfig.getId().intValue(),pipelineConfig.getParallelism(),pipelineConfig.getTargetRedis()));

        // 此时 同步任务可能在准备中 也可能在执行中; 但是都不允许再次启动
        pipelineConfig.start();
        baseMapper.updateById(pipelineConfig);


    }

    @Override
    public RotterResponse<Void> updatePipelineConfig(PipelineConfig pipelineConfig) {
        throw new RotterException(" unsupported operation for update config ");
    }

    @Override
    public void stop(Integer id) {
        // todo 停止只需要停止许可即可; node会自动处理


        // 清除相关资源
    }
}
