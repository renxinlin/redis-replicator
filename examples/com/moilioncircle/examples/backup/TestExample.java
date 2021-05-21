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

package com.moilioncircle.examples.backup;

import com.alibaba.fastjson.JSONObject;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueString;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
@SuppressWarnings("resource")
public class TestExample {
    static HashMap<String, Long> clazzs = new HashMap<>();

    public static void main(String[] args) throws IOException, URISyntaxException {

        //save 1000 records commands
        Replicator replicator = new RedisReplicator("redis://daily.redis.mockuai.com:6379");
        final AtomicInteger acc = new AtomicInteger(0);
        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                // 1 没有事务的补充 无法组装原子性 日志
                // 2 按照状态量解决多机房问题
                if(event instanceof  DumpKeyValuePair){
                    System.out.println( "DumpKeyValuePair:"+ JSONObject.toJSONString(event));

                }

                if(event instanceof KeyStringValueString){
                    System.out.println( JSONObject.toJSONString(event));                }

                Class<? extends Event> aClass = event.getClass();
                String simpleName = aClass.getSimpleName();

                Long aLong = clazzs.get(simpleName);
                if (aLong == null) {
                    aLong = 0L;
                } else {
                    aLong = aLong + 1;
                }
                clazzs.put(simpleName, aLong);

                clazzs.entrySet().forEach(e -> {
                    System.out.println(e.getKey() + "=======>" + e.getValue());
                });

            }
        });

        replicator.open();

    }
}
