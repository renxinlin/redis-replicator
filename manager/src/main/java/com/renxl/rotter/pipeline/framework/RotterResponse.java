package com.renxl.rotter.pipeline.framework;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 15:20
 */
@Data
public class RotterResponse<T> {

    private  static final int SUCCESS = 200;
    private static final int UN_SUCCESS = 500;

    /**
     * 200 500
     */
    private int code;
    /**
     * 10000
     * 1XXXX
     * 2XXXX
     * 3XXXX
     * 4XXXX
     */
    private int bizCode;

    private String msg;


    private T data;


    public static <V> RotterResponse<V> success(V data) {
        RotterResponse response = new RotterResponse();
        response.setData(data);
        response.setBizCode(BizCodeAndMsg.SUCCESS.bizCode);
        response.setMsg(BizCodeAndMsg.SUCCESS.msg);
        response.setCode(SUCCESS);
        return response;
    }




    public static <V> RotterResponse<V> success() {
        RotterResponse response = new RotterResponse();
        response.setBizCode(BizCodeAndMsg.SUCCESS.bizCode);
        response.setMsg(BizCodeAndMsg.SUCCESS.msg);
        response.setCode(SUCCESS);
        return response;
    }




    public static  <V> RotterResponse<V> success(BizCodeAndMsg bizCodeAndMsg,V data) {
        RotterResponse response = new RotterResponse();
        response.setBizCode(bizCodeAndMsg.getBizCode());
        response.setMsg(bizCodeAndMsg.getMsg());
        response.setCode(SUCCESS);
        response.setData(data);
        return response;
    }




    public static RotterResponse success(BizCodeAndMsg bizCodeAndMsg) {
        RotterResponse response = new RotterResponse();
        response.setBizCode(bizCodeAndMsg.getBizCode());
        response.setMsg(bizCodeAndMsg.getMsg());
        response.setCode(SUCCESS);
        return response;
    }










    // error

    public static <V> RotterResponse<V> fail(V data) {
        RotterResponse response = new RotterResponse();
        response.setData(data);
        response.setBizCode(BizCodeAndMsg.ERROR.bizCode);
        response.setMsg(BizCodeAndMsg.ERROR.msg);
        response.setCode(UN_SUCCESS);
        return response;
    }




    public static <V> RotterResponse<V> fail() {
        RotterResponse response = new RotterResponse();
        response.setBizCode(BizCodeAndMsg.ERROR.bizCode);
        response.setMsg(BizCodeAndMsg.ERROR.msg);
        response.setCode(UN_SUCCESS);
        return response;
    }




    public static <V> RotterResponse<V> fail(BizCodeAndMsg bizCodeAndMsg,V data) {
        RotterResponse response = new RotterResponse();
        response.setBizCode(bizCodeAndMsg.getBizCode());
        response.setMsg(bizCodeAndMsg.getMsg());
        response.setCode(UN_SUCCESS);
        response.setData(data);
        return response;
    }




    public static RotterResponse fail(BizCodeAndMsg bizCodeAndMsg) {
        RotterResponse response = new RotterResponse();
        response.setBizCode(bizCodeAndMsg.getBizCode());
        response.setMsg(bizCodeAndMsg.getMsg());
        response.setCode(UN_SUCCESS);
        return response;
    }








    @AllArgsConstructor
    @Getter
    public enum BizCodeAndMsg {

        /**
         * success 1XXXX
         */
        SUCCESS(10000, "success"),

        /**
         * fail 5XXXX
         */
        ERROR(50000, "error"),

        ADD_PIPELINE_CONFIG_PARAM_ERROR(50001,"同步任务配置参数校验不通过"),
        PIPLINED_STARTED(50002,"同步任务已经处于启动状态"),
        TARGET_NUM_NO_MORE_THAN_ONE(50003, "目标节点只能配置一个node"),
        PING_ERRPR(50004,"node 节点ping-pong失败")
        ;
        private int bizCode;
        private String msg;


    }
}
