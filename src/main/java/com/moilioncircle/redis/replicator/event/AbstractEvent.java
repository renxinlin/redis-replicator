/*
 * Copyright 2016-2017 Leon Chen
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

package com.moilioncircle.redis.replicator.event;

import com.moilioncircle.redis.replicator.util.type.Tuple2;

/**
 * @author Leon Chen
 * @since 3.3.0
 */
public abstract class AbstractEvent implements Event {
    protected Context context = new ContextImpl();

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private static class ContextImpl implements Context {

        private Tuple2<Long, Long> offsets;

        @Override
        public Tuple2<Long, Long> getOffsets() {
            return offsets;
        }

        @Override
        public void setOffsets(Tuple2<Long, Long> offset) {
            this.offsets = offset;
        }
    }
}