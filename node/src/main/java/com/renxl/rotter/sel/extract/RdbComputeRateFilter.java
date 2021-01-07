package com.renxl.rotter.sel.extract;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.moilioncircle.redis.replicator.cmd.impl.*;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import com.moilioncircle.redis.replicator.util.Strings;
import com.renxl.rotter.sel.SelectorBatchEvent;
import com.renxl.rotter.sel.SelectorEvent;
import org.testng.Assert;
import redis.clients.jedis.Protocol;

import java.util.List;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public class RdbComputeRateFilter extends Filter {
    @Override
    protected void executeFilterJob(SelectorBatchEvent selectorBatchEvent) {
        List<SelectorEvent> selectorEvents = selectorBatchEvent.getSelectorEvent();
        if (CollectionUtils.isEmpty(selectorEvents)) {
            return;
        }

        selectorEvents.forEach(selectorEvent -> {
            AbstractCommand abstractCommand = selectorEvent.getAbstartCommand();
            if (abstractCommand != null) {
                if(abstractCommand instanceof ExpireAtCommand){
                    long ex = ((ExpireAtCommand) abstractCommand).getEx();
                    
                }

                if(abstractCommand instanceof ExpireCommand){
                    int ex = ((ExpireCommand) abstractCommand).getEx();
                }


            }

            KeyValuePair keyValuePair = selectorEvent.getKeyValuePair();
            if (keyValuePair != null) {
                DB db = keyValuePair.getDb();

            }


        });


    }
}

/**
 if (event instanceof DumpKeyValuePair) {
 DumpKeyValuePair dkv = (DumpKeyValuePair) event;
 // Step1: select db
 DB db = dkv.getDb();
 int index;
 if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {
 target.send(Protocol.Command.SELECT, Protocol.toByteArray(index));
 dbnum.set(index);
 System.out.println("SELECT:" + index);
 }

 // Step2: restore dump data
 if (dkv.getExpiredMs() == null) {
 Object r = target.restore(dkv.getKey(), 0L, dkv.getValue(), true);
 System.out.println(r);
 } else {
 long ms = dkv.getExpiredMs() - System.currentTimeMillis();
 if (ms <= 0) return;
 Object r = target.restore(dkv.getKey(), ms, dkv.getValue(), true);
 System.out.println(r);
 }
 }

 if (event instanceof DefaultCommand) {
 // Step3: sync aof command
 DefaultCommand dc = (DefaultCommand) event;
 Object r = target.send(dc.getCommand(), dc.getArgs());
 System.out.println(r);
 }*/