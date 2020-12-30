package com.renxl.rotter.config;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-28 21:59
 */
public class HeartBeatConfig {
    /**
     * 心跳上报时间
     */
    public static Long heartBeatTime = 15 * 1000L;

    /**
     * 心跳超时时间
     */
    public static Long heartBeatTimeoutTime = 60 * 1000L;

    /**
     * 心跳超时检测时间 30s
     */
    @Deprecated
    public static Long heartBeatCheckTime = 30 * 1000L;


    public static int EXCTRACT_DEFAULT_POOL_SIZE = 50;

    public static int EXCTRACT_DEFAULT_ACCEPT_COUNT = 100;
}
