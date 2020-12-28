package com.renxl.rotter.pipeline.framework;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 16:12
 */
public class Matcher {

    public static boolean isIP(String ip) {
        if (ip == null || "".equals(ip)) {
            return false;
        }
        String regex = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}"
                + "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";
        return ip.matches(regex);
    }

    public static boolean isPort(String s) {
        return s.matches("^[1-9]$|(^[1-9][0-9]$)|(^[1-9][0-9][0-9]$)|(^[1-9][0-9][0-9][0-9]$)|(^[1-6][0-5][0-5][0-3][0-5]$)");
    }

}
