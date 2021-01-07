package com.renxl.rotter.sel;

import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.rpcclient.events.GetExtractBatchEvents;

/**
 * 通过管道拉取 sedg
 *
 * @description:
 * @author: renxl
 * @create: 2021-01-07 19:05
 */
public class PipeImpl implements Pipe {

    @Override
    public SelectorBatchEvent getSelectBatchEvent(Integer pipelineId, long seqNumber) {
        SelectorBatchEvent selectorBatchEvent;// 获取 extractIp[== selectip] 数据 rpc 获取 memory
        String selecterIp = CompomentManager.getInstance().getMetaManager().getPipelineTaskIps().get(pipelineId).getSelecterIp();
        if (CompomentManager.getInstance().getMetaManager().getNodeIp().equals(selecterIp)) {
            // 内存调用
            selectorBatchEvent = CompomentManager.getInstance().getMetaManager().takeExtractEvent(pipelineId, seqNumber);
        } else {
            // rpc调用
            selectorBatchEvent = (SelectorBatchEvent) CompomentManager.getInstance().getCommunicationClient().call(selecterIp, new GetExtractBatchEvents(pipelineId, seqNumber));
        }

        return selectorBatchEvent;
    }


}
