package com.renxl.rotter.config;

import com.renxl.rotter.register.RegisterrWatcher;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.impl.CommunicationConnectionFactory;
import com.renxl.rotter.rpcclient.impl.DefaultCommunicationClientImpl;
import com.renxl.rotter.rpcclient.impl.dubbo.DubboCommunicationConnectionFactory;
import com.renxl.rotter.rpcclient.impl.dubbo.DubboCommunicationEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 17:44
 */
@Configuration
public class ManagerConfiguration {



    /**
     * rpc 通信工具 采用注册通信
     *
     * @return
     */
    @Bean(initMethod = "initial", destroyMethod = "destory")
    public CommunicationClient communicationClient(CommunicationConnectionFactory communicationConnectionFactory) {
        CommunicationClient communicationClient = new DefaultCommunicationClientImpl(communicationConnectionFactory);
        return communicationClient;
    }

    @Bean
    public CommunicationConnectionFactory dubboCommunicationConnectionFactory() {
        CommunicationConnectionFactory communicationConnectionFactory = new DubboCommunicationConnectionFactory();
        return communicationConnectionFactory;
    }


    @Bean(initMethod = "initial", destroyMethod = "destory")
    public DubboCommunicationEndpoint dubboCommunicationEndpoint() {
        DubboCommunicationEndpoint dubboCommunicationEndpoint = new DubboCommunicationEndpoint(Configs.dubboManangerPort);
        return dubboCommunicationEndpoint;
    }

    /**
     * manager 选举 选举后的master作为与node节点交互的manger
     * 其他左右follower节点等待当前master manager掉线
     *
     * @return
     */
    @Bean(initMethod = "initial", destroyMethod = "destory")
    public RegisterrWatcher registerrWatcher() {
        RegisterrWatcher registerrWatcher = new RegisterrWatcher();
        return registerrWatcher;
    }


}
// todo 暴露rpc端口  注册manager自己到zk  event注册