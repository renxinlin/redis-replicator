package com.renxl.rotter.config;

import com.renxl.rotter.manager.MetaManager;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.impl.CommunicationConnectionFactory;
import com.renxl.rotter.rpcclient.impl.DefaultCommunicationClientImpl;
import com.renxl.rotter.rpcclient.impl.dubbo.DubboCommunicationConnectionFactory;
import com.renxl.rotter.rpcclient.impl.dubbo.DubboCommunicationEndpoint;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:06
 */
public class CompomentBuilder {


    public static  CompomentManager bulid(){
        CompomentManager instance = CompomentManager.getInstance();
        // 构建rpc client
        CommunicationConnectionFactory communicationConnectionFactory = new DubboCommunicationConnectionFactory();
        CommunicationClient communicationClient = new DefaultCommunicationClientImpl(communicationConnectionFactory);
        // 暴露dubbo服务
        DubboCommunicationEndpoint dubboCommunicationEndpoint = new DubboCommunicationEndpoint();

        // 元信息管理器 todo 获取zk上的manager信息 start的get
        MetaManager manager = new MetaManager();

        instance.setCommunicationClient(communicationClient);
        instance.setDubboCommunicationConnectionFactory(communicationConnectionFactory);


        instance.setDubboCommunicationEndpoint(dubboCommunicationEndpoint);
        instance.setMetaManager(manager);
        return instance;
    }
}
