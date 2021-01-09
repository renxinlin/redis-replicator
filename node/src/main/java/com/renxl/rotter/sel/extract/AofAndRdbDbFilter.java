package com.renxl.rotter.sel.extract;

import com.moilioncircle.redis.replicator.cmd.impl.AbstractCommand;
import com.moilioncircle.redis.replicator.cmd.impl.SelectCommand;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.sel.SelectorBatchEvent;
import com.renxl.rotter.sel.SelectorEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public  class AofAndRdbDbFilter extends Filter {

    //



    @Override
    protected void executeFilterJob(SelectorBatchEvent selectorBatchEvent) {

        List<Integer> filterDbs = CompomentManager.getInstance().getMetaManager().getSelectDb(getPipeLineId());

        List<SelectorEvent> selectorEvents = selectorBatchEvent.getSelectorEvent();
        // 将不需要同步的数据库过滤
        List<SelectorEvent> newSelectorEvents = selectorEvents.stream().filter(selectorEvent -> {
            AbstractCommand abstartCommand = selectorEvent.getAbstartCommand();
            KeyValuePair keyValuePair = selectorEvent.getKeyValuePair();

            if(abstartCommand!=null){
                return abstartCommand instanceof SelectCommand  ||  filterDbs.contains(abstartCommand.getDbNumber());
            }
            if(keyValuePair!=null){
                DB db = keyValuePair.getDb();
                return  filterDbs.contains(db.getDbNumber());
            }
            return false;
        }).collect(Collectors.toList());
        selectorBatchEvent.setSelectorEvent(newSelectorEvents);

    }
}
