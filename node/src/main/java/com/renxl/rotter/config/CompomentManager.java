package com.renxl.rotter.config;

import com.renxl.rotter.LifeCycle;
import com.renxl.rotter.manager.MetaManager;
import com.renxl.rotter.manager.MetaManagerWatcher;
import com.renxl.rotter.rpcclient.CommunicationClient;
import com.renxl.rotter.rpcclient.impl.CommunicationConnectionFactory;
import com.renxl.rotter.rpcclient.impl.dubbo.DubboCommunicationEndpoint;
import com.renxl.rotter.task.HeartbeatScheduler;
import lombok.Data;
import sun.plugin2.message.HeartbeatMessage;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 20:06
 */
@Data
public class CompomentManager implements LifeCycle {


    private static volatile CompomentManager INSTANCE;
    private CommunicationConnectionFactory dubboCommunicationConnectionFactory;
    private CommunicationClient communicationClient;

    private DubboCommunicationEndpoint dubboCommunicationEndpoint;



    private HeartbeatScheduler hearbeatScheduler;

    private MetaManager metaManager;

    private MetaManagerWatcher metaManagerWatcher;


    private CompomentManager() {

    }

    public static CompomentManager getInstance() {

        if (INSTANCE == null) {
            synchronized (CompomentManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CompomentManager();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public void start() {
         communicationClient.initial();
        dubboCommunicationEndpoint.initial();
        metaManager.init();
        metaManagerWatcher.initial();

    }

    @Override
    public void stop() {
        dubboCommunicationEndpoint.destory();
        communicationClient.destory();
    }
}
