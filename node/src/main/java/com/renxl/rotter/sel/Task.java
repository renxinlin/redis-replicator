package com.renxl.rotter.sel;

import com.renxl.rotter.domain.RedisMasterInfo;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 04:12
 */
@Data
public abstract class Task extends Thread{
    private Integer pipelineId;

    /**
     * 由manager控制许可 只有true 才可以继续处理同步任务 如果false超过一定时间则需要关闭任务
     */
    protected boolean permit;

    public void permit() {
        permit = true;
    }

    public  void unPermit() {
        permit = false;
    }

    abstract boolean getPermit();


}
