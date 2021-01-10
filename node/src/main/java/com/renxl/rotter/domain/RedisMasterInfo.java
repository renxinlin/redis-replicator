package com.renxl.rotter.domain;

import com.renxl.rotter.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-09 16:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisMasterInfo {
    private String ip;
    private String port;
    private String auth;

    public void parse(String targetRedis) {
        String[] ipAndPort = targetRedis.split(Constants.IP_PORT_SPLIT);
        this.ip = ipAndPort[0];
        this.port = ipAndPort.length == 2 ? ipAndPort[1] : 6379 + "";

    }
}
