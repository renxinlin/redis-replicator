package com.renxl.rotter.config;

import com.renxl.rotter.monitor.RotterNodeWorkMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 15:26
 */
@Configuration
public class MonitorConfig {

    /**
     * 监听 node  select load 准备就绪 事件
     *
     * @return
     */
    @Bean(initMethod = "init", destroyMethod = "destory")
    public RotterNodeWorkMonitor rotterNodeWorkMonitor() {
        RotterNodeWorkMonitor rotterNodeWorkMonitor = new RotterNodeWorkMonitor();
        return rotterNodeWorkMonitor;
    }
}
