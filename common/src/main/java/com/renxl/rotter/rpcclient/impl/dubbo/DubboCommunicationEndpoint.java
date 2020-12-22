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

package com.renxl.rotter.rpcclient.impl.dubbo;

import com.AddressUtils;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import com.renxl.rotter.rpcclient.CommunicationEndpoint;
import com.renxl.rotter.rpcclient.impl.AbstractCommunicationEndpoint;

import java.net.InetAddress;
import java.text.MessageFormat;

/**
 * 暴露dubbo端口 没有使用注册中心
 * @author jianghang 2011-11-29 上午11:08:29
 * @version 4.0.0
 */
public class DubboCommunicationEndpoint extends AbstractCommunicationEndpoint {
    private static  InetAddress ip = null;

    static {
        // todo 网卡ip获取
        ip = AddressUtils.getHostAddress();
    }
    private static   String DUBBO_SERVICE_URL = "dubbo://"+ip.getHostAddress()+":{0}/endpoint?server=netty&codec=dubbo&serialization=java&heartbeat=5000&iothreads=4&threads=50&connections=30";
    private DubboProtocol protocol = DubboProtocol.getDubboProtocol();
    private ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getExtension("javassist");

    private Exporter<CommunicationEndpoint> exporter = null;
    private int port = 2088;

    public DubboCommunicationEndpoint() {

    }

    public DubboCommunicationEndpoint(int port) {
        this.port = port;
    }

    public void initial() {
        String url = MessageFormat.format(DUBBO_SERVICE_URL, String.valueOf(port));
        exporter = protocol.export(proxyFactory.getInvoker(this, CommunicationEndpoint.class, URL.valueOf(url)));
    }

    public void destory() {
        exporter.unexport();
    }

    // =============== setter / gettter ==================

    public void setPort(int port) {
        this.port = port;
    }

}
