package com.renxl.rotter.sel.extract;

import com.renxl.rotter.sel.SelectorBatchEvent;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public abstract class Filter {


    private Filter next;


    public void filter(SelectorBatchEvent selectorBatchEvent) {

        executeFilterJob(selectorBatchEvent);

        if (next != null) {
            next.filter(selectorBatchEvent);
        }

    }

    protected abstract void executeFilterJob(SelectorBatchEvent selectorBatchEvent);


    public void setNext(Filter next) {
        this.next = next;
    }
}
