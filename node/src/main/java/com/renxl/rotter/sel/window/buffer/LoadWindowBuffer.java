package com.renxl.rotter.sel.window.buffer;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * extract的滑动窗口池
 *
 * @description:
 * @author: renxl
 * @create: 2020-12-30 19:23
 */
public class LoadWindowBuffer extends WindowBuffer {
    private ArrayBlockingQueue<Long> arrayBlockingQueue = new ArrayBlockingQueue(1024);

    @Override
    public long get() {
        Long poll = null;
        while (true) {
            try {
                poll = arrayBlockingQueue.take();
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
        try {

            arrayBlockingQueue.put(batchId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return batchId;
    }
}
