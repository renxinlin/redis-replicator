package com.renxl.rotter.sel.extract;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.moilioncircle.redis.replicator.cmd.impl.AbstractCommand;
import com.moilioncircle.redis.replicator.cmd.impl.GenericKeyCommand;
import com.moilioncircle.redis.replicator.util.Strings;
import com.renxl.rotter.sel.SelectorBatchEvent;
import com.renxl.rotter.sel.SelectorEvent;
import sun.security.provider.MD5;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * load阶段使用
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public  class AofCircleSyncFilter extends Filter {
    @Override
    protected void executeFilterJob(SelectorBatchEvent selectorBatchEvent) {

        // 删除
        List<SelectorEvent> selectorEvents = selectorBatchEvent.getSelectorEvent();

        // 同步回环标存在则过滤指定数据
        if(CollectionUtils.isEmpty(selectorEvents)){
            return;
        }

        List<SelectorEvent> aofCommands = selectorEvents.stream().filter(selectorEvent -> null != selectorEvent.getAbstartCommand()).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(aofCommands)){
            return;
        }
        List<SelectorEvent> arrayList = new ArrayList();
        aofCommands.stream().forEach(aofCommand->{
            AbstractCommand abstartCommand = aofCommand.getAbstartCommand();
            if(abstartCommand instanceof GenericKeyCommand){
                byte[] keyByte = ((GenericKeyCommand) abstartCommand).getKey();
                String keyStr = Strings.toString(keyByte);
                // 需要对key value整体进行加密
                // todo 删除成功表示回环标存在
            }

            // 其他类型命令处理
        });


//        selectorBatchEvent.setSelectorEvent()


    }
}
