package com.renxl.rotter.domain;

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
}
