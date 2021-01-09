package com.renxl.rotter.sel.extract;

import com.moilioncircle.redis.replicator.cmd.CommandParsers;
import com.moilioncircle.redis.replicator.cmd.impl.AbstractCommand;
import com.moilioncircle.redis.replicator.cmd.impl.GenericKeyCommand;
import com.moilioncircle.redis.replicator.rdb.datatype.ContextKeyValuePair;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.renxl.rotter.config.CompomentManager;
import com.renxl.rotter.sel.SelectorBatchEvent;
import com.renxl.rotter.sel.SelectorEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
@Slf4j
public class AofKeyFilter extends Filter {


    @Override
    protected void executeFilterJob(SelectorBatchEvent selectorBatchEvent) {

        List<SelectorEvent> selectorEvents = selectorBatchEvent.getSelectorEvent();
        // 将不需要同步的数据库过滤
        List<SelectorEvent> newSelectorEvents = selectorEvents.stream().filter(selectorEvent -> {
            AbstractCommand abstartCommand = selectorEvent.getAbstartCommand();
            KeyValuePair keyValuePair = selectorEvent.getKeyValuePair();
            if (keyValuePair != null) {
                if (keyValuePair instanceof ContextKeyValuePair) {
                    // 不进行目标传递 也不会存在这里
                    log.error("ContextKeyValuePair exist...");
                    return false;

                }
                if (keyValuePair instanceof KeyValuePair) {
                    String dumpKey = CommandParsers.toRune(keyValuePair.getKey());
                    // 匹配上则不加入extract
                    return !CompomentManager.getInstance().getMetaManager().matchFilterKeys(getPipeLineId(), dumpKey);
                }

            }


            if (abstartCommand != null) {
                if (abstartCommand instanceof GenericKeyCommand) {
                    byte[] key = ((GenericKeyCommand) abstartCommand).getKey();
                    String commandKey = CommandParsers.toRune(key);
                    // 匹配上则不加入extract
                    return !CompomentManager.getInstance().getMetaManager().matchFilterKeys(getPipeLineId(), commandKey);
                }

                // TODO 对mset这些批量操作不做处理 ; 将来扩展个性化能力 以及eval这些特殊的将来提供定制化能力
            }
            return false;
        }).collect(Collectors.toList());
        selectorBatchEvent.setSelectorEvent(newSelectorEvents);

    }
}
