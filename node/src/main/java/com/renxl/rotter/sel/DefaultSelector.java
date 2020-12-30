package com.renxl.rotter.sel;

import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.manager.WindowManagerWatcher;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 19:12
 */
public class DefaultSelector extends Selector {

    SelectorParam param;


    public DefaultSelector(SelectorParam param){
        this.param = param ;
        CompomentManager.getInstance().getWindowManagerWatcher().init(param.getPipelineId());
    }


    @Override
    public void aof(DefaultCommand event) {

    }

    @Override
    public void rdb(DumpKeyValuePair event) {

    }
}
