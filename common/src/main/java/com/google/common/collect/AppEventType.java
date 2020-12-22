package com.google.common.collect;

import com.renxl.rotter.rpcclient.EventType;

public enum AppEventType implements EventType {
    create, update, delete, find;
}
