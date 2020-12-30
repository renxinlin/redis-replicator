package com.renxl.rotter.sel;

import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.RedisURI;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.event.*;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.manager.WindowManagerWatcher;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-30 19:12
 */
@Slf4j
public class DefaultSelector extends Selector {

    SelectorParam param;


    public DefaultSelector(SelectorParam param){
        this.param = param ;
        // 根据并行数创建滑动窗口大小
        CompomentManager.getInstance().getWindowManagerWatcher().initPipelined(param.getPipelineId(),param.getParallelism());
    }
    public void sync() throws IOException, URISyntaxException {

        // todo 获取当前主master信息

        String sourceUri = param.getSourceRedises();
        RedisURI suri = new RedisURI(sourceUri);
        Replicator r = dress(new RedisReplicator(suri));

        r.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {

                if (event instanceof PreRdbSyncEvent) {
                    log.info("start rdb==>");
                }
                if (event instanceof DumpKeyValuePair) {
                    DumpKeyValuePair dkv = (DumpKeyValuePair) event;
                    rdb((DumpKeyValuePair) event);
                }
                if (event instanceof PostRdbSyncEvent) {
                    log.info("end rdb==>");
                }
                if (event instanceof PreCommandSyncEvent) {
                    log.info("start aof==>");

                }
                if (event instanceof DefaultCommand) {
                    aof((DefaultCommand) event);
                }

                if (event instanceof PostCommandSyncEvent) {
                    log.info("end aof==>");

                }
            }
        });


        r.open();
    }

    @Override
    public void aof(DefaultCommand event) {

    }

    @Override
    public void rdb(DumpKeyValuePair event) {

    }
}
