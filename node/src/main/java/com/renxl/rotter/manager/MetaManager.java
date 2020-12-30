package com.renxl.rotter.manager;

import com.renxl.rotter.LoadTask;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.sel.SelectTask;
import com.renxl.rotter.sel.Task;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * pipelineid task
     */
    private Map<Integer, SelectTask> pipelineSelectTasks = new HashMap<>();
    private Map<Integer, SelectTask> pipelineExctractTasks = new HashMap<>();
    private Map<Integer, LoadTask> pipelineLoadTasks = new HashMap<>();


    public void init() {

        String adress = CompomentManager.getInstance().callInitManagerAdress();
        if (StringUtils.isEmpty(adress)) {
            return;
        }
        manager = new ManagerInfo();
        manager.setManagerAddress(adress);

    }

    public void onAddSelectTask(String pipelineId, SelectTask task) {

    }

    public void removeTask() {

    }

    public void addTask(Task task) {
        if (task instanceof SelectTask) {
            pipelineSelectTasks.put(task.getPipelineId(), (SelectTask) task);
        }
        if (task instanceof LoadTask) {
            pipelineLoadTasks.put(task.getPipelineId(), (LoadTask) task);
        }

        if (task instanceof LoadTask) {
            pipelineLoadTasks.put(task.getPipelineId(), (LoadTask) task);
        }


    }
}
