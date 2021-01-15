package com.renxl.rotter.sel.extract;

import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.renxl.rotter.common.Md5Util;
import com.renxl.rotter.constants.Constants;
import com.renxl.rotter.domain.RedisMasterInfo;
import com.renxl.rotter.sel.SelectorBatchEvent;
import com.renxl.rotter.sel.SelectorEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 *
 *
 * 判断一个业务key是不是回环key
 * 业务系统在A机房redis-A写入指令 set a 1
 * A机房replicator-A作为redis-A的从节点接收到指令set a 1
 * rotter-A MD5(set a 1) 得到circle-key-md5，拼装成指令setex circle-key-md5 120 1
 * rotter-A将指令setex circle-key-md5 120 1和指令set a 1一起写入B机房redis-B
 * B机房replicator-B作为redis-B的从节点接收到指令setex circle-key-md5 120 1 和set a 1
 * rotter-B直接忽略circle-key指令
 * rotter-B在本机房执行del circle-key-md5，如果成功说明是回环KEY，不需要同步至A机房
 *
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public class AofParseEventFilter extends Filter {

    /**
     * 默认主从时 从库是没有开启写功能的
     * 参数  slave-read-only no
     */
    private JedisPool jedisPool;


    /**
     * 在数据到达之前就会被初始化
     *
     * @param redisMasterInfo
     */
    public void initRedis(RedisMasterInfo redisMasterInfo) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(8);
        config.setMinIdle(2);
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config, redisMasterInfo.getIp(), Integer.valueOf(redisMasterInfo.getPort()), 10000, redisMasterInfo.getAuth());

    }

    /**
     * 需要知道本地的redis信息
     * <p>
     * 还要知道数据回环和 数据删除保护的hash算法 采用md5 假定md5是hash优秀安全
     * <p>
     * 在本地进行删除
     * <p>
     * 删除成功说明是回环数据  则不再向下游传递
     *
     * @param selectorBatchEvent
     */
    @Override
    protected void executeFilterJob(SelectorBatchEvent selectorBatchEvent) {
        // todo 阻塞
        Jedis client = jedisPool.getResource();
        List<SelectorEvent> selectorEvents = selectorBatchEvent.getSelectorEvent();
        List<SelectorEvent> newSelectorEvents = new ArrayList<>();
        selectorEvents.forEach(selectorEvent -> {
            if (selectorEvent.getKeyValuePair() != null) {
                // 对于rdb 双向则必须存在主机房 dump时候主机房为准
                newSelectorEvents.add(selectorEvent);
            } else if (selectorEvent.getAbstartCommand() != null) {
                //
                if (selectorEvent.getAbstartCommand() instanceof DefaultCommand) {
                    String commandStr = selectorEvent.getAbstartCommand().toString();
                    String hashKey = Md5Util.md5(commandStr);
                    // 没有数据回环和删除保护标记
                    Long dataCycle = client.del(Constants.DATA_CYCLE + hashKey);
                    Boolean exists = client.exists(Constants.DELETE_PROTECTED + hashKey);
                    if ((dataCycle == null || dataCycle == 0) && !exists) {
                        // 不会产生新的aof
                        newSelectorEvents.add(selectorEvent);
                    }
                }

            } else {
                newSelectorEvents.add(selectorEvent);
            }
        });

        selectorBatchEvent.setSelectorEvent(newSelectorEvents);
        client.close();

    }


}
