package com.renxl.rotter.sel.window.buffer;

import com.renxl.rotter.sel.window.Window;
import com.renxl.rotter.sel.window.WindowType;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 19:23
 */
public class SelectWindowBuffer extends WindowBuffer {
    /**
     * 滑动窗口的大小
     */
    private ArrayBlockingQueue<Long> arrayBlockingQueue  = new ArrayBlockingQueue(1024);


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
        try {

            arrayBlockingQueue.put(batchId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return batchId;
    }
}
