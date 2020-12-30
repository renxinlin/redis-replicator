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

package com.moilioncircle.examples.failover;

import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Leon Chen
 * @since 2.4.5
 */
public class FailoverExample {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException, URISyntaxException {
        // Execute the following command to create redis-4.0 cluster
        // #===========================command start===============================
        // $wget https://github.com/antirez/redis/archive/4.0.0.tar.gz
        // $tar -xvzf 4.0.0.tar.gz
        // $cd redis-4.0.0
        // $make MALLOC=libc
        // $cp src/redis-trib.rb /usr/local/bin/
        // $gem install redis
        // $cd utils/create-cluster
        // $./create-cluster start
        // $./create-cluster create
        // $cd ../../src
        // $./redis-cli -p 30002
        // $127.0.0.1:30002>cluster nodes
        // #============================command end  ==============================

        // 1227cc2c3101f13837cad4dc1c20ce41c1f49e55 127.0.0.1:30003@40003 master - 0 1509974842062 3 connected 10923-16383
        // bcb371fd4c9779a9963b5591f35504a7c7a56c54 127.0.0.1:30001@40001 master - 0 1509974842062 1 connected 0-5460
        // d6f409dacc394ed840c9905fd17fe3e110726244 127.0.0.1:30005@40005 slave cb0e0ae2d4d020d22e864b4d733a4aaf231c7a71 0 1509974842000 10 connected
        // 3b5809755396efa128065776073f8e3eab34cdfa 127.0.0.1:30006@40006 slave 1227cc2c3101f13837cad4dc1c20ce41c1f49e55 0 1509974842062 6 connected
        // ef5a11acf0bb0a8c043549113762328dabefdbf7 127.0.0.1:30004@40004 slave bcb371fd4c9779a9963b5591f35504a7c7a56c54 0 1509974842062 4 connected
        // cb0e0ae2d4d020d22e864b4d733a4aaf231c7a71 127.0.0.1:30002@40002 myself,master - 0 1509974841000 10 connected 5461-10922

        // You will see that 30002 is the master and 30005 is the slave of 30002.
        // Write some KV to 30002 then run the following code.
        // After code running then kill 30002. the slave 30005 will be the new master. you will see following code will do failover with partial synchronizations(PSYNC2) .

        /**
         *
         *
         *
         * psync2在宕机的时候会保存自己的复制信息到rdb中 这是优化与psync的地方
         * 虽然psync2是有两组replid和offset
         *
         * master_replid:117be6fda92f7909557d8096cc140ddf2ee40452
         * master_replid2:0000000000000000000000000000000000000000
         * master_repl_offset:39420283
         * second_repl_offset:-1
         *
         *
         *
         *
         *
         * 但是rotter只需要维护一份replid即可
         *
         *
         *
         * 但是otter需要知道整个集群的信息
         * 以及故障时候谁是master的信息
         * 对于redis发生故障
         * Rotterdam不会进行HA 而是等待redis新的选举完毕后重连
         *
         */
        Replicator r = new RedisReplicator("redis://127.0.0.1:30002?verbose=yes&retries=10");
        try {
            r.open();
        } catch (IOException e) {
            // failover
            String replId = r.getConfiguration().getReplId();
            long offset = r.getConfiguration().getReplOffset();
            r = new RedisReplicator("redis://127.0.0.1:30005?verbose=yes&retries=10&replId=" + replId + "&replOffset=" + offset);
            r.open();
        }
    }
}
