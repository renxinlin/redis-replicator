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

package com.moilioncircle.redis.replicator.cmd.parser;

import com.moilioncircle.redis.replicator.cmd.impl.BitFieldCommand;
import org.junit.Test;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class BitFieldParserTest extends AbstractParserTest {

    @Test
    public void testParse() {
        {
            BitFieldParser parser = new BitFieldParser();
            BitFieldCommand command = parser.parse(
                    toObjectArray(new Object[]{"bitfield", "mykey", "overflow", "sat"}));
            assertEquals("mykey", command.getKey());
            assertEquals(0, command.getStatements().size());
            assertEquals(1, command.getOverFlows().size());
        }

        {
            BitFieldParser parser = new BitFieldParser();
            BitFieldCommand command = parser.parse(
                    toObjectArray(new Object[]{"bitfield", "mykey", "incrby", "i5", "100", "1", "overflow", "sat"}));
            assertEquals("mykey", command.getKey());
            assertEquals(1, command.getStatements().size());
            assertEquals(1, command.getOverFlows().size());
        }

        {
            BitFieldParser parser = new BitFieldParser();
            BitFieldCommand command = parser.parse(
                    toObjectArray(new Object[]{"bitfield", "mykey", "incrby", "i5", "100", "1", "set", "i8", "#0", "100", "overflow", "sat"}));
            assertEquals("mykey", command.getKey());
            assertEquals(2, command.getStatements().size());
            assertEquals(1, command.getOverFlows().size());
        }

        {
            BitFieldParser parser = new BitFieldParser();
            BitFieldCommand command = parser.parse(
                    toObjectArray(new Object[]{"bitfield", "mykey", "incrby", "i5", "100", "1", "set", "i8", "#0", "100", "overflow", "fail"}));
            assertEquals("mykey", command.getKey());
            assertEquals(2, command.getStatements().size());
            assertEquals(1, command.getOverFlows().size());
        }

        {
            BitFieldParser parser = new BitFieldParser();
            BitFieldCommand command = parser.parse(
                    toObjectArray(new Object[]{"bitfield", "mykey", "incrby", "i5", "100", "1", "set", "i8", "#0", "100", "overflow", "wrap", "incrby", "i5", "100", "1", "set", "i8", "#0", "100", "overflow", "wrap", "incrby", "i5", "100", "1", "set", "i8", "#0", "100", "overflow", "fail"}));
            assertEquals("mykey", command.getKey());
            assertEquals(2, command.getStatements().size());
            assertEquals(3, command.getOverFlows().size());
            assertEquals(2, command.getOverFlows().get(0).getStatements().size());
        }

        {
            BitFieldParser parser = new BitFieldParser();
            BitFieldCommand command = parser.parse(
                    toObjectArray(new Object[]{"bitfield", "mykey", "incrby", "i5", "100", "1", "get", "i8", "10", "overflow", "wrap", "incrby", "i5", "100", "1", "set", "i8", "#0", "100", "overflow", "wrap", "incrby", "i5", "100", "1", "set", "i8", "#0", "100", "overflow", "fail"}));
            assertEquals("mykey", command.getKey());
            assertEquals(2, command.getStatements().size());
            assertEquals(3, command.getOverFlows().size());
            assertEquals(2, command.getOverFlows().get(0).getStatements().size());
        }

    }
}