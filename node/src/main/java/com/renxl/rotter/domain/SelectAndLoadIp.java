package com.renxl.rotter.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description:
 * @author: renxl
 * @create: 2021-01-07 16:22
 */
@AllArgsConstructor
@Data
public class SelectAndLoadIp {
    private String selecterIp;
    private String loadIp;
}
