package com.renxl.rotter.sel.extract;

import com.moilioncircle.redis.replicator.cmd.impl.*;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.util.Strings;
import com.renxl.rotter.sel.SelectorBatchEvent;
import com.renxl.rotter.sel.SelectorEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * flushDb 等禁止命令过滤器
 *
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public class AofCommandFilter extends Filter {

    @Override
    protected void executeFilterJob(SelectorBatchEvent selectorBatchEvent) {
        List<SelectorEvent> selectorEvents = selectorBatchEvent.getSelectorEvent();
        List<SelectorEvent> newSelectorEvents = new ArrayList<>(selectorBatchEvent.getSelectorEvent().size());
        selectorEvents.stream().forEach(selectorEvent -> {
            AbstractCommand abstartCommand = selectorEvent.getAbstartCommand();
            KeyValuePair keyValuePair = selectorEvent.getKeyValuePair();
            // rdb阶段
            if (keyValuePair != null) {
                newSelectorEvents.add(new SelectorEvent(null, keyValuePair));
            }

            // aof 过滤特殊key
            if (null != abstartCommand) {
                boolean tonext = true;
                if (abstartCommand instanceof DefaultCommand) {
                    if (Strings.toString(((DefaultCommand) abstartCommand).getCommand()).equals("FLUSHALL")) {
                        // instanceof FlushAllCommand
                        tonext = false;
                    } else if (Strings.toString(((DefaultCommand) abstartCommand).getCommand()).equals("FLUSHDB")) {
                        // instanceof SwapDBCommand
                        // 交换视图 这种操作太骚不需要
                        tonext = false;
                    } else if (Strings.toString(((DefaultCommand) abstartCommand).getCommand()).equals("SWAPDB")) {
                        // instanceof SwapDBCommand
                        // 交换视图 这种操作太骚不需要
                        tonext = false;
                    } else {
                        tonext = true;
                    }
                } else if (abstartCommand instanceof PingCommand) {
                    // ping 命令 PING-PONG 测试一个连接是否还是可用
                    tonext = false;
                } else if (abstartCommand instanceof ReplConfCommand) {
                    //在命令传播阶段，从服务器默认会以每秒一次的频率，向主服务器发送命令 REPLCONF ACK <replication_offset>
                    // 在一般情况下，lag的值应该在0秒或者1秒之间跳动，如果超过1秒的话，那么说明主从 服务器之间的连接出现了故障

                    // Redis的min-slaves-to-write和min-slaves-max-lag两个选项可以防止主服务器在不安全的情况下执行写命令
                    //
                    // min-slaves-to-write 3
                    // min-slaves-max-lag 10
                    // 那么在从服务器的数量少于3个，或者三个从服务器的延迟（lag）值都大于或等于10秒时，主服务器将拒绝执行写命令，这里的延迟值就是上面提到的INFO replication命令的lag 值
                    tonext = false;
                } else if (abstartCommand instanceof MultiCommand || abstartCommand instanceof ExecCommand) {
                    // DISCARD UnWatch watch
                    // 不支持redis 事务 但是事务内部的命令还是同步过去
                    tonext = false;
                } else if (abstartCommand instanceof SwapDBCommand) {
                    // 不支持redis 事务
                    tonext = false;
                } else if (abstartCommand instanceof FlushAllCommand) {
                    tonext = false;
                } else if (abstartCommand instanceof FlushDBCommand) {
                    tonext = false;
                } else {
                    tonext = true;
                }
                if (tonext) {
                    newSelectorEvents.add(new SelectorEvent(abstartCommand, null));
                }

            }

        });
        selectorBatchEvent.setSelectorEvent(newSelectorEvents);

    }
}
