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

import com.moilioncircle.examples.migration.MigrationExample;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.RedisURI;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.cmd.impl.*;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.EventListener;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.io.RawByteListener;
import com.moilioncircle.redis.replicator.rdb.dump.datatype.DumpKeyValuePair;
import com.moilioncircle.redis.replicator.util.Strings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.moilioncircle.examples.migration.MigrationExample.dress;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
@SuppressWarnings("resource")
public class CommandBackupExample {
    public  static LinkedList<DumpKeyValuePair> dumpKeyValuePairs = new LinkedList<>();
    public static void main(String[] args) throws IOException, URISyntaxException {



        Replicator replicator = new RedisReplicator("redis://127.0.0.1:6378");
        replicator = dress(replicator);

        replicator.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                if (event instanceof DumpKeyValuePair) {
                    System.out.println("DumpKeyValuePair" + event);

                }
                if (event instanceof PostRdbSyncEvent) {
                    System.out.println("PostRdbSyncEvent" + event);

                }
                if (event instanceof EvalCommand) {
                    return;
                }
                if (event instanceof Command) {
                    try {
                        byte[] command = ((DefaultCommand) event).getCommand();

                        System.out.println("Command" +Strings.toString(command));
                    } catch (Exception e) {
                    }
                }

                if (event instanceof SetCommand) {
                    try {
                        System.out.println("==1==" + event.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (event instanceof GenericKeyCommand) {
                    byte[] keyByte = ((GenericKeyCommand) event).getKey();
                    String keyStr = Strings.toString(keyByte);
                    System.out.println("gen==>" + keyStr);

                }

                if (event instanceof SelectCommand) {
                    int index = ((SelectCommand) event).getIndex();
                    System.out.println("index" + index);

                }
            }
        });

        replicator.open();


    }
}
