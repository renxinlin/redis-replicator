package com.renxl.rotter.pipeline.framework;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 15:47
 */
public class Asserts {


    public static void check(boolean success, RotterResponse.BizCodeAndMsg bizCodeAndMsg) {
        if (!success) {
            throw new RotterException("check error", bizCodeAndMsg);
        }
    }
}
