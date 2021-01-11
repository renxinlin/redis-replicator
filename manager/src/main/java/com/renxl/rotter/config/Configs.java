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
public class Configs {


    public   static  int dubboManangerPort = 6666;
    public   static  int dubboNodePort = 6666;



}
