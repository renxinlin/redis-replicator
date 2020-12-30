package com.renxl.rotter.sel;

import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 19:12
 */
public class DefaultSelector extends Selector {
    @Override
    public void aof(DefaultCommand event) {

    }

    @Override
    public void rdb(DumpKeyValuePair event) {

    }
}
