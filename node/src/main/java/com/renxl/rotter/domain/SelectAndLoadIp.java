package com.renxl.rotter.domain;

import com.renxl.rotter.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-07 16:22
 */
@Data
public class SelectAndLoadIp {
    private String selecterIp;
    private String loadIp;

    private String selecterport;
    private String loadPort;

    public SelectAndLoadIp(String selecterIpAndPort,String loadIpAndPort){
        selecterIp = selecterIpAndPort.split(Constants.IP_PORT_SPLIT)[0];
        selecterport = selecterIpAndPort.split(Constants.IP_PORT_SPLIT)[1];
        loadIp = loadIpAndPort.split(Constants.IP_PORT_SPLIT)[0];
        loadPort = loadIpAndPort.split(Constants.IP_PORT_SPLIT)[1];

    }
}
