package com.renxl.rotter.manager;

import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.config.CompomentManager;
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
    private ConcurrentMap<Integer, ArrayBlockingQueue<SelectorBatchEvent>> batchBuffer = new ConcurrentHashMap<>();


    public void init() {

        String adress = CompomentManager.getInstance().callInitManagerAdress();
        if (StringUtils.isEmpty(adress)) {
            return;
        }
        manager = new ManagerInfo();
        manager.setManagerAddress(adress);

    }

    public void addEvent(Integer pipelineId, SelectorBatchEvent task) {
        ArrayBlockingQueue arrayBlockingQueue = batchBuffer.getOrDefault(pipelineId,new ArrayBlockingQueue<SelectorBatchEvent>(1024*1024));
        arrayBlockingQueue.add(task);
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

    }

    public Boolean isPermit(Integer pipelineId) {
        SelectTask selectTask = pipelineSelectTasks.get(pipelineId);
        return selectTask.getPermit();

    }
}
