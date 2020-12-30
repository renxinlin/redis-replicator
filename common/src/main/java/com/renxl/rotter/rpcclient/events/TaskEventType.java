package com.renxl.rotter.rpcclient.events;

import com.renxl.rotter.rpcclient.EventType;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 23:04
 */
public enum TaskEventType implements EventType {
    /**
     * 通知node准备select
     */
    selectTask,
    /**
     * 通知node 准备load
     */
    loadTask,
    ping,
    selectReading,
    loadReading,

    permitEvent, selectPermit, loadPermit, relpInfo;
}
