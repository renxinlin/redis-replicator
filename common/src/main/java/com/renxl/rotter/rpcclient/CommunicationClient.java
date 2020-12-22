/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
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

package com.renxl.rotter.rpcclient;

/**
 * @link{https://github.com/alibaba/otter} 通信完全copy otter 保持可靠性 同时降低开发成本
 * 读者可以自行参阅otter
 * 通讯服务
 */
public interface CommunicationClient {

    /**
     * 初始
     */
    public void initial();

    /**
     * 销毁
     */
    public void destory();

    /**
     *  单地址调用
     * @param addr
     * @param event
     * @return
     */
    public Object call(final String addr, final Event event);

    /**
     * 单地址调用携带回调函数
     * @param addr
     * @param event
     * @param callback
     */
    public void call(final String addr, Event event, final Callback callback);

    /**
     * 多地址 调用
     * @param addrs
     * @param event
     * @return
     */
    public Object call(final String[] addrs, final Event event);

    /**
     * 多地址调用携带回调函数
     * @param serveraddrs
     * @param event
     * @param callback
     */
    public void call(final String[] serveraddrs, final Event event, final Callback callback);

}
