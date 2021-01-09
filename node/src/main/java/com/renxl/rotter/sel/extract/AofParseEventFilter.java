package com.renxl.rotter.sel.extract;

import com.renxl.rotter.sel.SelectorBatchEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-05 14:28
 */
public  class AofParseEventFilter extends Filter {

    /**
     * 默认主从时 从库是没有开启写功能的
     * 参数  slave-read-only no
     */
    private JedisPool jedisPool;
    AofParseEventFilter (String ip,int port){
        // 我们期望这里连接的是复制的机器
        jedisPool = new JedisPool();
    }

    /**
     *
     * 需要知道本地的redis信息
     *
     * 还要知道数据回环和 数据删除保护的hash算法 采用md5 假定md5是hash优秀安全
     *
     * 在本地进行删除
     *
     * 删除成功说明是回环数据  则不再向下游传递
     * @param selectorBatchEvent
     */
    @Override
    protected void executeFilterJob(SelectorBatchEvent selectorBatchEvent) {
        Jedis client = jedisPool.getResource();
        client.get("");

    }
}
