package com.renxl.rotter.sel.window.buffer;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 20:07
 */
public abstract class WindowBuffer {


    public abstract long get();

    public abstract long put(long batchId);
}
