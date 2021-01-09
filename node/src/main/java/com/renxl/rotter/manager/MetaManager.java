package com.renxl.rotter.manager;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.renxl.rotter.common.AddressUtils;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.domain.SelectAndLoadIp;
import com.renxl.rotter.rpcclient.events.SelectAndLoadIpEvent;
import com.renxl.rotter.sel.*;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * pipelineid 任务当前的select db  用于dbfilter 表示白名单配置
     */
    private Map<Integer, List<Integer>> pipelineCurrentDb ;


    /**
     * pipelineId 对应的不需要同步的key 表示黑名单配置
     */
    private Map<Integer, List<String>> pipelineKeyFilter;


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
    private ConcurrentMap<Integer, Map<Long,SelectorBatchEvent>> batchBuffer;

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
    private ConcurrentMap<Integer, Map<Long,SelectorBatchEvent>> batchExtractBuffer;


    public void init() {

        String adress = CompomentManager.getInstance().callInitManagerAdress();
        if (StringUtils.isEmpty(adress)) {
            return;
        }
        manager = new ManagerInfo();
        manager.setManagerAddress(adress);
        pipelineTaskIps = new ConcurrentHashMap<>();
        pipelineCurrentDb = new HashMap<>();
        batchBuffer = new ConcurrentHashMap<>();
        batchExtractBuffer = new ConcurrentHashMap<>();
        pipelineKeyFilter = new HashMap<>();

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
        pipelineCurrentDb.clear();
        batchBuffer.clear();
        batchExtractBuffer .clear();
        pipelineKeyFilter.clear();
    }

    public Boolean isPermit(Integer pipelineId) {
        SelectTask selectTask = pipelineSelectTasks.get(pipelineId);
        return selectTask.getPermit();

    }

    public void addIps(SelectAndLoadIpEvent selectAndLoadIpEvent) {
        pipelineTaskIps.put(selectAndLoadIpEvent.getPipelineId(),new SelectAndLoadIp(selectAndLoadIpEvent.getSelectorIp(),selectAndLoadIpEvent.getLoadIp()));


    }

    public List<Integer> getSelectDb(Integer pipeLineId) {
        // 默认选择redis默认配置的16个数据库
        ArrayList<Integer> defaultSelect = new ArrayList<>();
        defaultSelect.add(0);
        defaultSelect.add(1);
        defaultSelect.add(2);
        defaultSelect.add(3);
        defaultSelect.add(4);
        defaultSelect.add(5);
        defaultSelect.add(6);
        defaultSelect.add(7);
        defaultSelect.add(8);
        defaultSelect.add(9);
        defaultSelect.add(10);
        defaultSelect.add(11);
        defaultSelect.add(12);
        defaultSelect.add(13);
        defaultSelect.add(14);
        defaultSelect.add(15);

        List<Integer> selectDbs = pipelineCurrentDb.getOrDefault(pipeLineId, defaultSelect);
        return selectDbs;
    }


    public boolean matchFilterKeys(Integer pipelineId,String...  filterkeys) {
        List<String> filters = pipelineKeyFilter.get(pipelineId);
        if(CollectionUtils.isEmpty(filters)){
            return false;
        }else {

            for (int i = 0;i<filterkeys.length;i++){
                for (int j = 0;j< filters.size();j++){
                    boolean match = match(filters.get(j), filterkeys[i]);
                    if(match){
                        return true;
                    }
                }
            }


        }
        return false;
    }

    private boolean match(String filterRule, String filterkey) {
        // todo 目前只支持前缀匹配
        // 后期会扩展 左匹配  右匹配 模糊匹配  正则匹配等规则化对象
        return filterRule.startsWith(filterkey);
    }
}
