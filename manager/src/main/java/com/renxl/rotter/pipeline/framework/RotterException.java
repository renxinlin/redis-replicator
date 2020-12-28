package com.renxl.rotter.pipeline.framework;

/**
 * @description:
 * @author: renxl
 * @create: 2020-12-25 15:48
 */
public class RotterException extends RuntimeException {

    RotterResponse.BizCodeAndMsg bizCodeAndMsg;

    public RotterException() {
        super();
    }

    public RotterException(String message) {
        super(message);
    }

    public RotterException(String message, Throwable cause) {
        super(message, cause);
    }

    public RotterException(Throwable cause) {
        super(cause);
    }


    public RotterException(RotterResponse.BizCodeAndMsg bizCodeAndMsg) {
        super();
    }

    public RotterException(String message, RotterResponse.BizCodeAndMsg bizCodeAndMsg) {
        super(message);
    }

    public RotterException(String message, Throwable cause, RotterResponse.BizCodeAndMsg bizCodeAndMsg) {
        super(message, cause);
    }

    public RotterException(Throwable cause, RotterResponse.BizCodeAndMsg bizCodeAndMsg) {
        super(cause);
    }
}
