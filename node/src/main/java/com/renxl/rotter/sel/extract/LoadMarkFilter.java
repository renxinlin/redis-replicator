package com.renxl.rotter.sel.extract;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.moilioncircle.redis.replicator.cmd.CommandParsers;
import com.moilioncircle.redis.replicator.cmd.impl.AbstractCommand;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.cmd.impl.SelectCommand;
import com.renxl.rotter.common.Md5Util;
import com.renxl.rotter.constants.Constants;
import com.renxl.rotter.sel.SelectorEvent;

import java.util.ArrayList;
import java.util.List;

import static redis.clients.jedis.Protocol.toByteArray;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public class LoadMarkFilter {
    /**
     * select command 不管
     * <p>
     * <p>
     * del 命令加上 过期保护 回环标志
     * <p>
     * 其他命令加上 回环标志
     *
     * @param selectorEvents
     * @return
     */

    public List<SelectorEvent> mark(List<SelectorEvent> selectorEvents) {
        if(CollectionUtils.isEmpty(selectorEvents)){
            return null;
        }

        List<SelectorEvent> newSelectorEvents = new ArrayList<>();
        selectorEvents.forEach(selectorEvent -> {

            if (selectorEvent.getKeyValuePair() != null) {
                newSelectorEvents.add(new SelectorEvent(null, selectorEvent.getKeyValuePair()));
            } else if (selectorEvent.getAbstartCommand() != null) {

                AbstractCommand abstartCommand = selectorEvent.getAbstartCommand();

                newSelectorEvents.add(new SelectorEvent(selectorEvent.getAbstartCommand(), null));
                // selectCommand不做任何处理
                if (!(abstartCommand instanceof SelectCommand)) {
                    String commandStr = CommandParsers.toRune(((DefaultCommand) abstartCommand).getCommand()).toUpperCase();
                    if ("DEL".equals(commandStr)) {
                        buildMark(selectorEvent, newSelectorEvents, Constants.DELETE_PROTECTED);
                        buildMark(selectorEvent, newSelectorEvents, Constants.DATA_CYCLE);
                    } else {
                        buildMark(selectorEvent, newSelectorEvents, Constants.DATA_CYCLE);
                    }
                }

            }


        });
        return newSelectorEvents;
    }

    /**
     * @param selectorEvent
     * @param newSelectorEvents
     * @param keyPrefix         回环和删除保护
     */
    private void buildMark(SelectorEvent selectorEvent, List<SelectorEvent> newSelectorEvents, String keyPrefix) {

        DefaultCommand defaultCommand = (DefaultCommand) selectorEvent.getAbstartCommand();
        String hashKey = Md5Util.md5(defaultCommand.toString());


        String cyclekey = keyPrefix + hashKey;
        int seconds = 120;
        String value = "1";

        byte[][] cycleargs = new byte[3][];
        cycleargs[0] = cyclekey.getBytes();
        cycleargs[1] = toByteArray(seconds);
        cycleargs[2] = value.getBytes();

        DefaultCommand cycleMark = new DefaultCommand();
        cycleMark.setCommand("SETEX".getBytes());
        cycleMark.setArgs(cycleargs);


        newSelectorEvents.add(new SelectorEvent(cycleMark, null));
    }

}
