package com.renxl.rotter.sel;

import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.fastjson.JSON;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.domain.RedisMasterInfo;
import com.renxl.rotter.domain.SelectAndLoadIp;
import com.renxl.rotter.sel.extract.*;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;

import java.util.concurrent.*;

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

    /**
     * 及时更换同步的redis源
     * @param newRedisMasterInfo
     */
    public   void onChangeSource(RedisMasterInfo newRedisMasterInfo){
        Filter findRedis = this.filter;
        while (findRedis!=null ){
            if(findRedis instanceof AofParseEventFilter){
                ((AofParseEventFilter) findRedis).initRedis(newRedisMasterInfo);
            }
            findRedis = findRedis.getNext();
        }
    }


    /**
     * todo 添加个headfilter
     * 在添加个tailfilter
     */
    private Filter filter ;


    public ExtractTask(Integer pipelineId, Integer parallelism) {
        this.setPipelineId(pipelineId);
        /**
         * node节点配置的extracttask线程池的大小
         *
         *  queues == 0 ? new SynchronousQueue<Runnable>() :
         *                         (queues < 0 ? new LinkedBlockingQueue<Runnable>()
         *                                 : new LinkedBlockingQueue<Runnable>(queues)),
         *
         */
        extractThreads = new ThreadPoolExecutor(parallelism, parallelism, 60, TimeUnit.SECONDS,
                new SynchronousQueue(), new NamedThreadFactory("extract-pipelineId-" + pipelineId),
                new ThreadPoolExecutor.CallerRunsPolicy());



        // 过滤回环key
        AofParseEventFilter aofParseEventFilter = new AofParseEventFilter();

        // 回环和数据删除保护标记过滤
        AofCircleFlagFilter aofCircleFlagFilter = new AofCircleFlagFilter();
        aofCircleFlagFilter.setNext(aofParseEventFilter);

        // 过滤配置的key
        KeyFilter keyFilter = new KeyFilter();
        keyFilter.setNext(aofCircleFlagFilter);

        // 过滤不在配置中的db
        DbFilter dbFilter = new DbFilter();
        dbFilter.setNext(keyFilter);

        // 过滤flushDb等风险性命令
        AofCommandFilter aofCommandFilter  = new AofCommandFilter();
        aofCommandFilter.setNext(dbFilter);

        // 过滤 todo master 传向 slave slave不传向
        filter = new RdbDumpFilter();
        filter.setNext(aofCommandFilter);


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
                // 开始过滤 aof rdb 的 核心
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
