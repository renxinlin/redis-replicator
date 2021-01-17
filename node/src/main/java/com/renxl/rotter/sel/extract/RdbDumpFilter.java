package com.renxl.rotter.sel.extract;

import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.sel.SelectorBatchEvent;

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
        if(!master){
            selectorBatchEvent.setSelectorEvent(null);
        }

    }
}
