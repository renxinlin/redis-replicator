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

package com.moilioncircle.redis.replicator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.moilioncircle.redis.replicator.cmd.impl.AggregateType;
import com.moilioncircle.redis.replicator.cmd.impl.ExistType;
import com.moilioncircle.redis.replicator.cmd.impl.SetCommand;
import com.moilioncircle.redis.replicator.cmd.impl.ZAddCommand;
import com.moilioncircle.redis.replicator.cmd.impl.ZInterStoreCommand;
import com.moilioncircle.redis.replicator.cmd.impl.ZUnionStoreCommand;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.util.Strings;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.params.ZAddParams;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
@SuppressWarnings("resource")
public class RedisSocketReplicatorTest {

    @Test
    public void testSet() throws Exception {
        final AtomicReference<String> ref = new AtomicReference<>(null);
        Replicator replicator = new RedisReplicator("localhost", 6379, Configuration.defaultSetting().setRetries(0));
        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof PostRdbSyncEvent) {
                    Jedis jedis = new Jedis("localhost", 6379);
                    jedis.del("abc");
                    jedis.set("abc", "bcd");
                    jedis.close();
                }
                if (event instanceof SetCommand) {
                    SetCommand setCommand = (SetCommand) event;
                    Assert.assertEquals("abc", Strings.toString(setCommand.getKey()));
                    Assert.assertEquals("bcd", Strings.toString(setCommand.getValue()));
                    ref.compareAndSet(null, "ok");
                    try {
                        replicator.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        replicator.open();
        Assert.assertEquals("ok", ref.get());
    }

    @Test
    public void testZInterStore() throws Exception {
        final AtomicReference<String> ref = new AtomicReference<>(null);
        final Replicator replicator = new RedisReplicator("localhost", 6379, Configuration.defaultSetting().setRetries(0));
        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof PostRdbSyncEvent) {
                    Jedis jedis = new Jedis("localhost", 6379);
                    jedis.del("zset1");
                    jedis.del("zset2");
                    jedis.del("out");
                    jedis.zadd("zset1", 1, "one");
                    jedis.zadd("zset1", 2, "two");
                    jedis.zadd("zset2", 1, "one");
                    jedis.zadd("zset2", 2, "two");
                    jedis.zadd("zset2", 3, "three");
                    //ZINTERSTORE out 2 zset1 zset2 WEIGHTS 2 3
                    ZParams zParams = new ZParams();
                    zParams.weights(2, 3);
                    zParams.aggregate(ZParams.Aggregate.MIN);
                    jedis.zinterstore("out", zParams, "zset1", "zset2");
                    jedis.close();
                }
                if (event instanceof ZInterStoreCommand) {
                    ZInterStoreCommand zInterStoreCommand = (ZInterStoreCommand) event;
                    Assert.assertEquals("out", Strings.toString(zInterStoreCommand.getDestination()));
                    Assert.assertEquals(2, zInterStoreCommand.getNumkeys());
                    Assert.assertEquals("zset1", Strings.toString(zInterStoreCommand.getKeys()[0]));
                    Assert.assertEquals("zset2", Strings.toString(zInterStoreCommand.getKeys()[1]));
                    Assert.assertEquals(2.0, zInterStoreCommand.getWeights()[0], 0.0001);
                    Assert.assertEquals(3.0, zInterStoreCommand.getWeights()[1], 0.0001);
                    Assert.assertEquals(AggregateType.MIN, zInterStoreCommand.getAggregateType());
                    ref.compareAndSet(null, "ok");
                    try {
                        replicator.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        replicator.open();
        Assert.assertEquals("ok", ref.get());
    }

    @Test
    public void testZUnionStore() throws Exception {
        final AtomicReference<String> ref = new AtomicReference<>(null);
        final Replicator replicator = new RedisReplicator("localhost", 6379, Configuration.defaultSetting().setRetries(0));
        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof PostRdbSyncEvent) {
                    Jedis jedis = new Jedis("localhost", 6379);
                    jedis.del("zset3");
                    jedis.del("zset4");
                    jedis.del("out1");
                    jedis.zadd("zset3", 1, "one");
                    jedis.zadd("zset3", 2, "two");
                    jedis.zadd("zset4", 1, "one");
                    jedis.zadd("zset4", 2, "two");
                    jedis.zadd("zset4", 3, "three");
                    //ZINTERSTORE out 2 zset1 zset2 WEIGHTS 2 3
                    ZParams zParams = new ZParams();
                    zParams.weights(2, 3);
                    zParams.aggregate(ZParams.Aggregate.SUM);
                    jedis.zunionstore("out1", zParams, "zset3", "zset4");
                    jedis.close();
                }
                if (event instanceof ZUnionStoreCommand) {
                    ZUnionStoreCommand zInterStoreCommand = (ZUnionStoreCommand) event;
                    Assert.assertEquals("out1", Strings.toString(zInterStoreCommand.getDestination()));
                    Assert.assertEquals(2, zInterStoreCommand.getNumkeys());
                    Assert.assertEquals("zset3", Strings.toString(zInterStoreCommand.getKeys()[0]));
                    Assert.assertEquals("zset4", Strings.toString(zInterStoreCommand.getKeys()[1]));
                    Assert.assertEquals(2.0, zInterStoreCommand.getWeights()[0], 0.0001);
                    Assert.assertEquals(3.0, zInterStoreCommand.getWeights()[1], 0.0001);
                    Assert.assertEquals(AggregateType.SUM, zInterStoreCommand.getAggregateType());
                    ref.compareAndSet(null, "ok");
                    try {
                        replicator.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        replicator.open();
        Assert.assertEquals("ok", ref.get());
    }

    @Test
    public void testCloseListener() {
        final AtomicInteger acc = new AtomicInteger(0);
        Replicator replicator = new RedisReplicator("127.0.0.1", 6666, Configuration.defaultSetting().setUseDefaultExceptionListener(false));
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                acc.incrementAndGet();
            }
        });
        try {
            replicator.open();
            Assert.fail();
        } catch (IOException e) {
        }

        Assert.assertEquals(1, acc.get());
    }

    @Test
    public void testZAdd() throws Exception {
        final AtomicReference<String> ref = new AtomicReference<>(null);
        final Replicator replicator = new RedisReplicator("localhost", 6379, Configuration.defaultSetting().setRetries(0));
        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof PostRdbSyncEvent) {
                    Jedis jedis = new Jedis("localhost", 6379);
                    jedis.del("abc");
                    jedis.zrem("zzlist", "member");
                    jedis.set("abc", "bcd");
                    jedis.zadd("zzlist", 1.5, "member", ZAddParams.zAddParams().nx());
                    jedis.close();
                }
                if (event instanceof SetCommand) {
                    SetCommand setCommand = (SetCommand) event;
                    Assert.assertEquals("abc", Strings.toString(setCommand.getKey()));
                    Assert.assertEquals("bcd", Strings.toString(setCommand.getValue()));
                    ref.compareAndSet(null, "1");
                } else if (event instanceof ZAddCommand) {
                    ZAddCommand zaddCommand = (ZAddCommand) event;
                    Assert.assertEquals("zzlist", Strings.toString(zaddCommand.getKey()));
                    assertEquals(1.5, zaddCommand.getZSetEntries()[0].getScore(), 0.0001);
                    Assert.assertEquals("member", Strings.toString(zaddCommand.getZSetEntries()[0].getElement()));
                    assertEquals(ExistType.NX, zaddCommand.getExistType());
                    ref.compareAndSet("1", "2");
                    try {
                        replicator.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        replicator.open();
        Assert.assertEquals("2", ref.get());
    }

    @Test
    public void testV7() throws Exception {
        final AtomicReference<String> ref = new AtomicReference<>(null);
        final Replicator replicator = new RedisReplicator("localhost", 6380, Configuration.defaultSetting().setAuthPassword("test").setRetries(0));

        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof PostRdbSyncEvent) {
                    Jedis jedis = new Jedis("localhost", 6380);
                    jedis.auth("test");
                    jedis.del("abc");
                    jedis.set("abc", "bcd");
                    jedis.close();
                }
                if (event instanceof SetCommand) {
                    SetCommand setCommand = (SetCommand) event;
                    Assert.assertEquals("abc", Strings.toString(setCommand.getKey()));
                    Assert.assertEquals("bcd", Strings.toString(setCommand.getValue()));
                    ref.compareAndSet(null, "ok");
                    try {
                        replicator.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        replicator.open();
        Assert.assertEquals("ok", ref.get());
    }

    @Test
    public void testExpireV6() throws Exception {
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.del("abc");
        jedis.del("bbb");
        jedis.set("abc", "bcd");
        jedis.expire("abc", 500);
        jedis.set("bbb", "bcd");
        jedis.expireAt("bbb", System.currentTimeMillis() + 1000000);
        jedis.close();

        Replicator replicator = new RedisReplicator("localhost", 6379, Configuration.defaultSetting().setRetries(0));
        final List<KeyValuePair<?, ?>> list = new ArrayList<>();
        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof KeyValuePair) {
                    list.add((KeyValuePair<?, ?>) event);
                }
                if (event instanceof PostRdbSyncEvent) {
                    try {
                        replicator.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        replicator.open();
        for (KeyValuePair<?, ?> kv : list) {
            if (Strings.toString(kv.getKey()).equals("abc")) {
                Assert.assertNotNull(kv.getExpiredMs());
            } else if (Strings.toString(kv.getKey()).equals("bbb")) {
                Assert.assertNotNull(kv.getExpiredMs());
            }
        }
    }

    @Test
    public void testCount() throws IOException {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        for (int i = 0; i < 8000; i++) {
            jedis.del("test_" + i);
            jedis.set("test_" + i, "value_" + i);
        }
        jedis.close();

        Replicator redisReplicator = new RedisReplicator("127.0.0.1", 6379, Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        redisReplicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof KeyValuePair) {
                    KeyValuePair<?, ?> kv = (KeyValuePair<?, ?>) event;
                    if (Strings.toString(kv.getKey()).startsWith("test_")) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                        }
                        acc.incrementAndGet();
                    }
                }
                if (event instanceof PostRdbSyncEvent) {
                    try {
                        replicator.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        redisReplicator.open();
        Assert.assertEquals(8000, acc.get());
    }
}
