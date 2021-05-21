/*
 * Copyright 2016-2018 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moilioncircle.examples.migration;

import com.alibaba.fastjson.JSON;
import com.moilioncircle.redis.replicator.CloseListener;
import com.moilioncircle.redis.replicator.Configuration;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.RedisURI;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.CommandName;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import com.moilioncircle.redis.replicator.cmd.parser.DefaultCommandParser;
import com.moilioncircle.redis.replicator.cmd.parser.PingParser;
import com.moilioncircle.redis.replicator.cmd.parser.ReplConfParser;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.rdb.datatype.DB;
import com.moilioncircle.redis.replicator.rdb.dump.DumpRdbVisitor;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import com.moilioncircle.redis.replicator.util.Strings;
import redis.clients.jedis.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static redis.clients.jedis.Protocol.Command.AUTH;
import static redis.clients.jedis.Protocol.Command.RESTORE;
import static redis.clients.jedis.Protocol.Command.SELECT;
import static redis.clients.jedis.Protocol.toByteArray;

/**
 * spring dubbo
 *
 * @author Leon Chen
 * @since 2.5.0
 */
public class MigrationExample {

    public static void main(String[] args) throws IOException, URISyntaxException {
//        sync("redis://daily.redis.mockuai.com:6379", "redis://127.0.0.1:6380");

        // 连接池就采用默认大小
        JedisPool jedisPool = new JedisPool("daily.redis.mockuai.com",6379);
        Jedis resource = jedisPool.getResource();
        resource.select(1);
        String set = resource.set("asd", "ds");
        resource.set("aasd", "ds");




//        resource.select(1);
//        Pipeline pipelined = resource.pipelined();
//        pipelined.set("12","12");
//        pipelined.set("13","12");
//        pipelined.set("#$%^&*(13","12");
//        // 【load返回后 不在处理 NOT OK 的数据】 二是记录日志
//        List<Object> objects1 = pipelined.syncAndReturnAll();
//        System.out.println(JSON.toJSONString(objects1));
//        resource.close();
//        pipelined.close();
//        pipelined.set("132","12");
//        objects1 = pipelined.syncAndReturnAll();
//
//        System.out.println(objects1);

    }

    /*
     * Precondition:
     * 1. Make sure the two redis version is same.
     * 2. Make sure the single key-value is not very big.(highly recommend less then 1 MB)
     *
     * We running following steps to sync two redis.
     * 1. Get rdb stream from source redis.
     * 2. Convert source rdb stream to redis dump format.
     * 3. Use Jedis RESTORE command to restore that dump format to target redis.
     * 4. Get aof stream from source redis and sync to target redis.
     */
    public static void sync(String sourceUri, String targetUri) throws IOException, URISyntaxException {
        RedisURI suri = new RedisURI(sourceUri);
        RedisURI turi = new RedisURI(targetUri);
        final ExampleClient target = new ExampleClient(turi.getHost(), turi.getPort());
        Configuration tconfig = Configuration.valueOf(turi);
        if (tconfig.getAuthPassword() != null) {
            Object auth = target.send(AUTH, tconfig.getAuthPassword().getBytes());
            System.out.println("AUTH:" + auth);
        }
        final AtomicInteger dbnum = new AtomicInteger(-1);
        Replicator r = dress(new RedisReplicator(suri));

        r.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof DumpKeyValuePair) {
                    DumpKeyValuePair dkv = (DumpKeyValuePair) event;
                    // Step1: select db
                    DB db = dkv.getDb();
                    int index;
                    if (db != null && (index = (int) db.getDbNumber()) != dbnum.get()) {
                        target.send(SELECT, toByteArray(index));
                        dbnum.set(index);
                        System.out.println("SELECT:" + index);
                    }

                    // Step2: restore dump data
                    if (dkv.getExpiredMs() == null) {
                        Object r = target.restore(dkv.getKey(), 0L, dkv.getValue(), true);
                        System.out.println(r);
                    } else {
                        long ms = dkv.getExpiredMs() - System.currentTimeMillis();
                        if (ms <= 0) return;
                        Object r = target.restore(dkv.getKey(), ms, dkv.getValue(), true);
                        System.out.println(r);

                    }
                }

                if (event instanceof DefaultCommand) {
                    // Step3: sync aof command
                    DefaultCommand dc = (DefaultCommand) event;
                    Object r = target.send(dc.getCommand(), dc.getArgs());
                    System.out.println(r);
                }

                // 管道加速性能



            }
        });

        r.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                target.close();
            }
        });
        r.open();
    }

    public static Replicator dress(Replicator r) {
        r.setRdbVisitor(new DumpRdbVisitor(r));
        // ignore PING REPLCONF GETACK
        r.addCommandParser(CommandName.name("PING"), new PingParser());
        r.addCommandParser(CommandName.name("REPLCONF"), new ReplConfParser());
        //
        r.addCommandParser(CommandName.name("APPEND"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SETEX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("MSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("DEL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SADD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HMSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EXPIRE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EXPIREAT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("GETSET"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HSETNX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("MSETNX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PSETEX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SETNX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SETRANGE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HDEL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LPOP"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LPUSH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LPUSHX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LRem"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RPOP"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RPUSH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RPUSHX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZREM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RENAME"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("INCR"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("DECR"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("INCRBY"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("DECRBY"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PERSIST"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SELECT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("FLUSHALL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("FLUSHDB"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("HINCRBY"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZINCRBY"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("MOVE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SMOVE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PFADD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PFCOUNT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PFMERGE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SDIFFSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SINTERSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SUNIONSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZADD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZINTERSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZUNIONSTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("BRPOPLPUSH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LINSERT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RENAMENX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RESTORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PEXPIRE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PEXPIREAT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("GEOADD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EVAL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EVALSHA"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SCRIPT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("PUBLISH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("BITOP"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("BITFIELD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SETBIT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SREM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("UNLINK"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SWAPDB"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("MULTI"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("EXEC"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZREMRANGEBYSCORE"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZREMRANGEBYRANK"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZREMRANGEBYLEX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("LTRIM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("SORT"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("RPOPLPUSH"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZPOPMIN"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("ZPOPMAX"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XACK"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XADD"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XCLAIM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XDEL"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XGROUP"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XTRIM"), new DefaultCommandParser());
        r.addCommandParser(CommandName.name("XSETID"), new DefaultCommandParser());
        return r;
    }

    /*
     * Jedis is not a reliable redis client.
     * For simplicity we use Jedis to show this example.
     * In production you need to replace following code to yours.
     */
    public static class ExampleClient extends Client {

        public ExampleClient(final String host, final int port) {
            super(host, port);
            setConnectionTimeout(10000);
            setSoTimeout(10000);
        }

        public Object send(Protocol.Command cmd, final byte[]... args) {
            sendCommand(cmd, args);
            Object r = getOne();
            if (r instanceof byte[]) {
                return Strings.toString(r);
            } else {
                return r;
            }
        }

        public Object send(final byte[] cmd, final byte[]... args) {
            return send(Protocol.Command.valueOf(Strings.toString(cmd).toUpperCase()), args);
        }

        public Object restore(byte[] key, long expired, byte[] dumped, boolean replace) {
            if (!replace) {
                return send(RESTORE, key, toByteArray(expired), dumped);
            } else {
                return send(RESTORE, key, toByteArray(expired), dumped, "REPLACE".getBytes());
            }
        }
    }
}
