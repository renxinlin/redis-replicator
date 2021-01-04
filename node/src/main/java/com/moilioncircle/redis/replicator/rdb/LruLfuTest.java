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

package com.moilioncircle.redis.replicator.rdb;

import com.moilioncircle.redis.replicator.Configuration;
import com.moilioncircle.redis.replicator.FileType;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.rdb.datatype.EvictType;
import com.moilioncircle.redis.replicator.rdb.datatype.ExpiredType;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.util.Strings;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Leon Chen
 * @since 2.6.0
 */
public class LruLfuTest {

    @Test
    public void testLru() throws IOException {
        @SuppressWarnings("resource")
        Replicator replicator = new RedisReplicator(LruLfuTest.class.getClassLoader().getResourceAsStream("dump-lru.rdb"), FileType.RDB,
                Configuration.defaultSetting());
        final Map<String, KeyValuePair<byte[], ?>> map = new LinkedHashMap<>();
        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof KeyValuePair<?, ?>) {
                    @SuppressWarnings("unchecked")
                    KeyValuePair<byte[], ?> kv = (KeyValuePair<byte[], ?>) event;
                    map.put(Strings.toString(kv.getKey()), kv);
                }
            }
        });
        replicator.open();
        KeyValuePair<?, ?> kv1 = map.get("key");
        TestCase.assertEquals(ExpiredType.MS, kv1.getExpiredType());
        TestCase.assertEquals(1528592665231L, kv1.getExpiredMs().longValue());
        TestCase.assertEquals(EvictType.LRU, kv1.getEvictType());
        TestCase.assertEquals(4L, kv1.getEvictValue().longValue());
        KeyValuePair<?, ?> kv2 = map.get("key1");
        TestCase.assertEquals(EvictType.LRU, kv2.getEvictType());
        TestCase.assertEquals(1914611L, kv2.getEvictValue().longValue());
    }

    @Test
    public void testLfu() throws IOException {
        @SuppressWarnings("resource")
        Replicator replicator = new RedisReplicator(LruLfuTest.class.getClassLoader().getResourceAsStream("dump-lfu.rdb"), FileType.RDB,
                Configuration.defaultSetting());
        final Map<String, KeyValuePair<byte[], ?>> map = new LinkedHashMap<>();
        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof KeyValuePair<?, ?>) {
                    @SuppressWarnings("unchecked")
                    KeyValuePair<byte[], ?> kv = (KeyValuePair<byte[], ?>) event;
                    map.put(Strings.toString(kv.getKey()), kv);
                }
            }
        });
        replicator.open();
        KeyValuePair<?, ?> kv1 = map.get("key");
        TestCase.assertEquals(ExpiredType.MS, kv1.getExpiredType());
        TestCase.assertEquals(1528592896226L, kv1.getExpiredMs().longValue());
        TestCase.assertEquals(EvictType.LFU, kv1.getEvictType());
        TestCase.assertEquals(4L, kv1.getEvictValue().longValue());
        KeyValuePair<?, ?> kv2 = map.get("key1");
        TestCase.assertEquals(EvictType.LFU, kv2.getEvictType());
        TestCase.assertEquals(1L, kv2.getEvictValue().longValue());
    }
}
