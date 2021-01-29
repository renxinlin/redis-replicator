package com.renxl.rotter.sel.extract;

import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.sel.SelectorBatchEvent;
import com.renxl.rotter.sel.SelectorEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public  class RdbDumpFilter extends Filter {
    @Override
    protected void executeFilterJob(SelectorBatchEvent selectorBatchEvent) {
        //  主机房db  非主机房直接过滤
        boolean master = CompomentManager.getInstance().getMetaManager().isMaster(getPipeLineId());
        List<SelectorEvent> selectorEvents = selectorBatchEvent.getSelectorEvent();
        List<SelectorEvent> newSelect = selectorEvents.stream().filter(selectorEvent -> master || selectorEvent.getAbstartCommand() != null).collect(Collectors.toList());
        selectorBatchEvent.setSelectorEvent(newSelect);


    }
}
