package com.renxl.rotter.sel.extract;

import com.moilioncircle.redis.replicator.cmd.CommandParsers;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.renxl.rotter.sel.SelectorBatchEvent;
import com.renxl.rotter.sel.SelectorEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 过滤回环数据 和删除保护标记
 *
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public class AofCircleFlagFilter extends Filter {
    private static final String DATA_CYCLE = "!@#$renxlrotter:cycle:";
    private static final String DELETE_PROTECTED = "!@#$renxlrotter:d:";

    @Override
    protected void executeFilterJob(SelectorBatchEvent selectorBatchEvent) {
        List<SelectorEvent> selectorEvents = selectorBatchEvent.getSelectorEvent();
        List<SelectorEvent> newSelectorEvents = new ArrayList<>();
        for (SelectorEvent selectorEvent : selectorEvents) {
            if (selectorEvent.getAbstartCommand() != null
                    // 数据回环和删除保护标记 通过set del命令 实现
                    && (CommandParsers.toRune(((DefaultCommand) (selectorEvent.getAbstartCommand())).getCommand()).equals("SET")
                    || CommandParsers.toRune(((DefaultCommand) (selectorEvent.getAbstartCommand())).getCommand()).equals("DEL")
            )
                    // 不管
                    && (CommandParsers.toRune(((DefaultCommand) selectorEvent.getAbstartCommand()).getArgs()[0]).startsWith(DATA_CYCLE)
                    || CommandParsers.toRune(((DefaultCommand) selectorEvent.getAbstartCommand()).getArgs()[0]).startsWith(DELETE_PROTECTED)
            )) {
                // 说明是数据回环或者删除保护标记  不进行回流
                continue;
            } else if (selectorEvent.getKeyValuePair() != null
                    && (
                    CommandParsers.toRune(selectorEvent.getKeyValuePair().getKey()).startsWith(DATA_CYCLE)
                            || CommandParsers.toRune(selectorEvent.getKeyValuePair().getKey()).startsWith(DELETE_PROTECTED))) {
                // 说明是数据回环或者删除保护标记 dump 不进行同步任务
                continue;
            } else {
                newSelectorEvents.add(selectorEvent);
            }


        }
        selectorBatchEvent.setSelectorEvent(newSelectorEvents);


    }
}
