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

package com.moilioncircle.redis.replicator.util;

import org.junit.Test;
import org.testng.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Leon Chen
 * @since 2.2.0
 */
public class ByteArrayMapTest {
    
    @Test
    public void test() {
        Map<byte[], byte[]> m = new LinkedHashMap<>();
        m.put(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        m.put(null, new byte[]{4});
        m.put(new byte[]{4, 5, 6}, null);
        ByteArrayMap bytes = new ByteArrayMap(m);
        Assert.assertEquals(3, bytes.size());
        Assert.assertEquals(true, Arrays.equals(new byte[]{4, 5, 6}, bytes.get(new byte[]{1, 2, 3})));
        Assert.assertEquals(true, Arrays.equals(new byte[]{4}, bytes.get(null)));
        Assert.assertEquals(null, bytes.get(new byte[]{4, 5, 6}));
        Assert.assertEquals(false, bytes.isEmpty());
        Assert.assertEquals(true, bytes.containsKey(new byte[]{1, 2, 3}));
        Assert.assertEquals(true, bytes.containsKey(null));
        Assert.assertEquals(true, bytes.containsValue(new byte[]{4, 5, 6}));
        Assert.assertEquals(true, bytes.containsValue(null));

        Set<byte[]> s = bytes.keySet();

        Iterator<byte[]> it = s.iterator();
        while (it.hasNext()) {
            byte[] key = it.next();
            Assert.assertEquals(true, s.contains(key));
            Assert.assertEquals(true, bytes.containsKey(key));
        }

        for (byte[] b : bytes.keySet()) {
            Assert.assertEquals(true, bytes.containsKey(b));
        }

        for (byte[] b : bytes.values()) {
            Assert.assertEquals(true, bytes.containsValue(b));
        }

        for (Map.Entry<byte[], byte[]> entry : bytes.entrySet()) {
            Assert.assertEquals(true, bytes.containsKey(entry.getKey()));
            Assert.assertEquals(true, bytes.containsValue(entry.getValue()));
        }

        Set<Map.Entry<byte[], byte[]>> ss = bytes.entrySet();
        Iterator<Map.Entry<byte[], byte[]>> itr = ss.iterator();
        while (itr.hasNext()) {
            Map.Entry<byte[], byte[]> entry = itr.next();
            Assert.assertEquals(true, ss.contains(entry));
            Assert.assertEquals(true, ss.contains(new TestEntry(entry.getKey(), entry.getValue())));
            if (entry.getValue() != null) {
                Assert.assertEquals(true, ss.contains(new TestEntry(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length))));
            } else {
                Assert.assertEquals(true, ss.contains(new TestEntry(entry.getKey(), null)));
            }
            Assert.assertEquals(true, bytes.containsKey(entry.getKey()));
            Assert.assertEquals(true, bytes.containsValue(entry.getValue()));
        }
    
        bytes = new ByteArrayMap(null);
        Assert.assertEquals(0, bytes.size());
        Assert.assertEquals(true, bytes.isEmpty());
    
        bytes = new ByteArrayMap(new HashMap<byte[], byte[]>());
        Assert.assertEquals(0, bytes.size());
        Assert.assertEquals(true, bytes.isEmpty());
    
        bytes = new ByteArrayMap(m);
        bytes.put(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        bytes.put(null, new byte[]{4});
        bytes.put(new byte[]{4, 5, 6}, null);
        s = bytes.keySet();
        s.remove(new byte[]{1, 2, 3});
        s.remove(null);
        s.remove(new byte[]{4, 5, 6});
        Assert.assertEquals(0, s.size());
        Assert.assertEquals(0, bytes.size());
    
        bytes = new ByteArrayMap(m);
        bytes.put(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        bytes.put(null, new byte[]{4});
        bytes.put(new byte[]{4, 5, 6}, null);
        ss = bytes.entrySet();
        List<Map.Entry<byte[], byte[]>> list = new ArrayList<>();
        for (Map.Entry<byte[], byte[]> entry : ss) {
            list.add(new TestEntry(entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<byte[], byte[]> entry : list) {
            ss.remove(entry);
        }
        Assert.assertEquals(0, ss.size());
        Assert.assertEquals(0, bytes.size());
    
        bytes = new ByteArrayMap(m);
        bytes.put(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        bytes.put(null, new byte[]{4});
        bytes.put(new byte[]{4, 5, 6}, null);
        Iterator<byte[]> a = bytes.keySet().iterator();
        while (a.hasNext()) {
            a.next();
            a.remove();
        }
        Assert.assertEquals(0, bytes.size());
    
        bytes = new ByteArrayMap(m);
        bytes.put(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        bytes.put(null, new byte[]{4});
        bytes.put(new byte[]{4, 5, 6}, null);
        Iterator<Map.Entry<byte[], byte[]>> aa = bytes.entrySet().iterator();
        while (aa.hasNext()) {
            aa.next();
            aa.remove();
        }
        Assert.assertEquals(0, bytes.size());
    }
    
    @Test
    public void test1() {
        Map<byte[], byte[]> m = new LinkedHashMap<>();
        m.put(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        m.put(null, new byte[]{4});
        m.put(new byte[]{4, 5, 6}, null);
        ByteArrayMap bytes = new ByteArrayMap(m);
        Assert.assertEquals(3, bytes.size());
        Iterator<byte[]> it = bytes.values().iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        Assert.assertEquals(0, bytes.size());
    }
    
    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        Map<byte[], byte[]> m = new LinkedHashMap<>();
        m.put(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        m.put(null, new byte[]{4});
        m.put(new byte[]{4, 5, 6}, null);
        File file = new File("./test.txt");
        ByteArrayMap bytes = new ByteArrayMap(m);
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        out.writeObject(bytes);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        ByteArrayMap deseri = (ByteArrayMap) in.readObject();
        in.close();
        Assert.assertEquals(3, deseri.size());
        Assert.assertEquals(true, Arrays.equals(new byte[]{4, 5, 6}, deseri.get(new byte[]{1, 2, 3})));
        Assert.assertEquals(true, Arrays.equals(new byte[]{4}, deseri.get(null)));
        Assert.assertEquals(null, deseri.get(new byte[]{4, 5, 6}));
        Assert.assertEquals(false, deseri.isEmpty());
        Assert.assertEquals(true, deseri.containsKey(new byte[]{1, 2, 3}));
        Assert.assertEquals(true, deseri.containsKey(null));
        Assert.assertEquals(true, deseri.containsValue(new byte[]{4, 5, 6}));
        Assert.assertEquals(true, deseri.containsValue(null));
    }

    private final class TestEntry implements Map.Entry<byte[], byte[]> {

        private byte[] value;
        private final byte[] key;

        private TestEntry(byte[] key, byte[] value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public byte[] getKey() {
            return this.key;
        }

        @Override
        public byte[] getValue() {
            return this.value;
        }

        @Override
        public byte[] setValue(byte[] value) {
            byte[] oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

}