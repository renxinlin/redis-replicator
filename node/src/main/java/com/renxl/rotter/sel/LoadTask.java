package com.renxl.rotter.sel;

import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:02
 */
public class LoadTask extends Task {

    private String  targetRedis ;

    /**
     * 按照滑动窗口顺序阻塞等待 先发出后到达的 aof rdb
     */
    private ArrayList<Long> currentWaitSeqNum ;

    /**
     * 初始的滑动窗口序列号
     */
    private AtomicLong currentSeqNum = new AtomicLong(0L);

    public LoadTask(Integer pipelineId, String targetRedis, int parallelism) {
        currentWaitSeqNum = new ArrayList(parallelism);
        this.setPipelineId(pipelineId);
        this.targetRedis = targetRedis;

    }
    @Override
    boolean getPermit() {
        return permit;
    }


     public void run() {
        // load 阶段单线程处理 pipeline发送 增加速度
        while (true){
            WindowBuffer loadBuffer = CompomentManager.getInstance().getWindowManager().getLoadBuffer(getPipelineId());
            long seqNumber = loadBuffer.get();
            // 获取 extract 数据

            // 添加删除保护指令 数据回环指令


            // 发送到目标redis




        }

    }

}
