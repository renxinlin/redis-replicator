package com.renxl.rotter.sel.window.buffer;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 19:23
 */
public class SelectWindowBuffer extends WindowBuffer {
    /**
     * 滑动窗口的大小
     */
    private PriorityBlockingQueue<Long> arrayBlockingQueue = new PriorityBlockingQueue(1024);


    @Override
    public long get() {
        while (true) {
            try {
                Long poll = arrayBlockingQueue.take();
                return poll;
            } catch (InterruptedException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public long put(long batchId) {
        arrayBlockingQueue.add(batchId);
        return batchId;
    }
}
