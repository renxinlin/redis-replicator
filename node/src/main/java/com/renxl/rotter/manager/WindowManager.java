package com.renxl.rotter.manager;

import com.renxl.rotter.sel.window.buffer.SelectWindowBuffer;
import com.renxl.rotter.sel.window.buffer.WindowBuffer;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:04
 */
@Data
public class WindowManager {

    public Map<Integer, WindowBuffer> sWindowBuffers = new HashMap();
    public Map<Integer, WindowBuffer> eWindowBuffers = new HashMap();
    public Map<Integer, WindowBuffer> lWindowBuffers = new HashMap();


    public WindowBuffer getSelectBuffer(Integer pipelineId) {
        WindowBuffer windowBuffer = sWindowBuffers.get(pipelineId);
        return getWindowBuffer(pipelineId, windowBuffer, sWindowBuffers);
    }



    public WindowBuffer getExtractBuffer(Integer pipelineId) {
        WindowBuffer windowBuffer = eWindowBuffers.get(pipelineId);
        return getWindowBuffer(pipelineId, windowBuffer, eWindowBuffers);
    }

    public WindowBuffer getLoadBuffer(Integer pipelineId) {
        WindowBuffer windowBuffer = lWindowBuffers.get(pipelineId);
        return getWindowBuffer(pipelineId, windowBuffer, lWindowBuffers);
    }


    private WindowBuffer getWindowBuffer(Integer pipelineId, WindowBuffer windowBuffer, Map<Integer, WindowBuffer> sWindowBuffers) {
        if (windowBuffer == null) {
            synchronized (sWindowBuffers) {
                if (windowBuffer == null) {
                    windowBuffer = new SelectWindowBuffer();
                    sWindowBuffers.put(pipelineId, windowBuffer);
                }
            }
        }
        return windowBuffer;
    }


}
