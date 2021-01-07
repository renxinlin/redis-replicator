package com.renxl.rotter.manager;

import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.domain.SelectAndLoadIp;
import com.renxl.rotter.rpcclient.events.SelectAndLoadIpEvent;
import com.renxl.rotter.sel.*;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:04
 */
@Data
public class MetaManager {

    /**
     * manager master节点信息
     */
    private ManagerInfo manager;
    private String nodeIp = AddressUtils.getHostAddress().getHostAddress();

    /**
     * pipelineid task
     */
    private Map<Integer, SelectTask> pipelineSelectTasks = new HashMap<>();
    private Map<Integer, ExtractTask> pipelineExctractTasks = new HashMap<>();
    private Map<Integer, LoadTask> pipelineLoadTasks = new HashMap<>();


    /**
     * pipeLine select and load ip Info
     */
    private Map<Integer, SelectAndLoadIp> pipelineTaskIps ;

    /**
     *
     *
     *
     *                      zkclient--[add batchId]
     *                       |
     *                  ------------
     *                 |            |
     *                 |            batchId [remove batchId]
     *                 |            |
     *             batchId          --> ExtractTask    --->
     * selectTask --> batchBuffer   --> ExtractTask    ----> ---->
     *                               --> ExtractTask   ---->
     *
     * @param Integer pipelineId
     * @param ArrayBlockingQueue rdb aof
     */
    private ConcurrentMap<Integer, Map<Long,SelectorBatchEvent>> batchBuffer = new ConcurrentHashMap<>();

    /**
     * 原本的设计是buffer 但是为了便于滑动窗口算法按顺序读取
     *
     * 这里采用了map
     * 算是偷懒
     *
     * 同时由于滑动窗口的保障 这里的Map<Long,SelectorBatchEvent> 不会增的超过并行量 也算是起到大小固定的能力
     *
     *
     * TODO 改造成阻塞式排序队列
     */
    private ConcurrentMap<Integer, Map<Long,SelectorBatchEvent>> batchExtractBuffer = new ConcurrentHashMap<>();


    public void init() {

        String adress = CompomentManager.getInstance().callInitManagerAdress();
        if (StringUtils.isEmpty(adress)) {
            return;
        }
        manager = new ManagerInfo();
        manager.setManagerAddress(adress);
        pipelineTaskIps = new ConcurrentHashMap<>();

    }

    public void addEvent(Integer pipelineId, SelectorBatchEvent task) {
        Map<Long, SelectorBatchEvent> seqNumberAndEventBuffer = batchBuffer.getOrDefault(pipelineId, new ConcurrentHashMap<>());
        seqNumberAndEventBuffer.put(task.getBatchId(),task);
    }


    public SelectorBatchEvent takeEvent(Integer pipelineId, Long seqNumber) {
        Map<Long,SelectorBatchEvent> seqNumberAndEventBuffer = batchBuffer.get(pipelineId);
        return seqNumberAndEventBuffer.remove(seqNumber);
    }


    public void addExtractEvent(Integer pipelineId, SelectorBatchEvent task) {
        Map<Long, SelectorBatchEvent> seqNumberAndEventBuffer = batchExtractBuffer.getOrDefault(pipelineId, new ConcurrentHashMap<>());
        seqNumberAndEventBuffer.put(task.getBatchId(),task);
    }
    public SelectorBatchEvent takeExtractEvent(Integer pipelineId, Long seqNumber) {
        Map<Long, SelectorBatchEvent> seqNumberAndEventBuffer = batchExtractBuffer.get(pipelineId);
        return seqNumberAndEventBuffer.remove(seqNumber);
    }



    public void addTask(Task task) {
        if (task instanceof SelectTask) {
            pipelineSelectTasks.put(task.getPipelineId(), (SelectTask) task);
        }
        if (task instanceof ExtractTask) {
            pipelineExctractTasks.put(task.getPipelineId(), (ExtractTask) task);
        }

        if (task instanceof LoadTask) {
            pipelineLoadTasks.put(task.getPipelineId(), (LoadTask) task);
        }


    }


    public void destory() {
        pipelineSelectTasks.clear();
        pipelineExctractTasks.clear();
        pipelineLoadTasks.clear();
        manager = null;
        batchBuffer.clear();
        pipelineTaskIps.clear();
    }

    public Boolean isPermit(Integer pipelineId) {
        SelectTask selectTask = pipelineSelectTasks.get(pipelineId);
        return selectTask.getPermit();

    }

    public void addIps(SelectAndLoadIpEvent selectAndLoadIpEvent) {
        pipelineTaskIps.put(selectAndLoadIpEvent.getPipelineId(),new SelectAndLoadIp(selectAndLoadIpEvent.getSelectorIp(),selectAndLoadIpEvent.getLoadIp()));


    }
}
