package com.renxl.rotter.sel.extract;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.renxl.rotter.sel.SelectorBatchEvent;
import com.renxl.rotter.sel.SelectorEvent;

import java.util.List;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public abstract class Filter {

    private Integer pipeLineId ;
    private Filter next;


    public void filter(SelectorBatchEvent selectorBatchEvent) {


        executeFilterJob0(selectorBatchEvent);

        if (next != null) {
            next.filter(selectorBatchEvent);
        }

    }


       public void executeFilterJob0(SelectorBatchEvent selectorBatchEvent){
           // 删除
           List<SelectorEvent> selectorEvents = selectorBatchEvent.getSelectorEvent();

           // 同步回环标存在则过滤指定数据
           if(CollectionUtils.isEmpty(selectorEvents)){
               return;
           }

           executeFilterJob(selectorBatchEvent);



       }

    protected abstract void executeFilterJob(SelectorBatchEvent selectorBatchEvent);


    public void setNext(Filter next) {
        this.next = next;
    }
    public Filter getNext() {
        return  next;
    }


    public Integer getPipeLineId() {
        return pipeLineId;
    }
}
