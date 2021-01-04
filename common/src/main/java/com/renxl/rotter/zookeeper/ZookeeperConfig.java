package com.renxl.rotter.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@NoArgsConstructor
@AllArgsConstructor
public class ZookeeperConfig  {//implements ApplicationContextAware {



    @Value("${zk.register.address}")
    private String address;
    /**
     * select准备就绪事件
     * ${0} pipelineId
     * /rotter/reading/select/${0}
     */
    private String selectNodeReadingPath = "/rotter/reading/select";
    /**
     * load准备就绪事件
     * ${0} pipelineId
     */
    private String loadNodeReadingPath =  "/rotter/reading/load";


    /**
     * manager master占用路径
     */
    public static final String managerMaster = "/rotter/manager";

    public static final String managerMasterParent = "/rotter";

    /**
     * 滑动窗口占用路径
     */
    public static final String pipelineWindowParent = "/rotter";


    public static final String pipelineWindow = "/rotter/window";
    // pipelineid
    public static final String pipelineWindowId = "/rotter/window/{0}";
    // pipelineWindowSize
    public static final String pipelineWindowTemp = "/rotter/window/{0}/";

    private static ZookeeperConfig zookeeperConfig  ;

    public static ZookeeperConfig getInstance() {
        return zookeeperConfig;
    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        zookeeperConfig = applicationContext.getBean(ZookeeperConfig.class);
//
//    }

}
