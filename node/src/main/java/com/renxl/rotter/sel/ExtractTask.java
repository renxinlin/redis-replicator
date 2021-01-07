package com.renxl.rotter.sel;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.domain.SelectAndLoadIp;
import com.renxl.rotter.sel.extract.AofCircleSyncFilter;
import com.renxl.rotter.sel.extract.AofDeleteKeyFilter;
import com.renxl.rotter.sel.extract.Filter;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.renxl.rotter.config.CompomentManager.getInstance;

/**
 *
 *
 * 经过对redis aof和rdb研究
 * 决定对sel 进行进一步细化
 *
 * reason:
 *  redis作为非结构化数据
 *  并不按标准的etl进行操作
 *  所以弱化其extract的概念  而偏向 filter功能
 *
 *
 *  研究决定
 *
 *  extract主要有如下功能
 *  1: 过滤掉不需要同步的DB
 *  2: 过滤掉不需要同步的KEY
 *  3 过滤掉回环指令和删除保护指令[删除保护指令主要解决mysql回流污染]
 *  4 用户自定义filter
 *
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:02
 */
public class ExtractTask extends Task {

    private ExecutorService extractThreads;

    private Filter filter ;


    public ExtractTask(Integer pipelineId, Integer parallelism) {
        this.setPipelineId(pipelineId);
        /**
         * node节点配置的extracttask线程池的大小
         */
        extractThreads = new ThreadPoolExecutor(parallelism, parallelism, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue(0), new NamedThreadFactory("extract-pipelineId-" + pipelineId),
                new ThreadPoolExecutor.CallerRunsPolicy());


        /**
         * 构建extract的核心工作
         *
         * 过滤掉回环指令和删除保护指令
         * 过滤掉不需要同步的KEY
         * 过滤掉不需要同步的DB
         *
         * 过滤掉特殊指令
         *
         */

        filter = new AofCircleSyncFilter();

    }


    @Override
    boolean getPermit() {
        return true;
    }

    public void run() {
        // 这里不再向otter那样纯的pipe机制 直接获取ringbuffer
        WindowBuffer extractBuffer = CompomentManager.getInstance().getWindowManager().getExtractBuffer(getPipelineId());
        while (true) {

            // 这个阶段式不需要保障滑动窗口顺序的 在load阶段处理seq问题 todo 验证阻塞
            long seqNumber = extractBuffer.get();

            extractThreads.execute(()->{
                SelectorBatchEvent selectorBatchEvent = CompomentManager.getInstance().getMetaManager().takeEvent(getPipelineId(), seqNumber);
                // 开始过滤
                filter.filter(selectorBatchEvent);
                SelectAndLoadIp selectAndLoadIp = getInstance().getMetaManager().getPipelineTaskIps().get(getPipelineId());
                // 通知load 进行加载 添加到缓存区等待 load读取
                getInstance().getMetaManager().addExtractEvent(getPipelineId(),selectorBatchEvent);
                // 通知load 前来读取
                getInstance().getWindowManager().singleLoad(getPipelineId(),selectAndLoadIp.getLoadIp(),seqNumber);

            });
        }

    }

}
