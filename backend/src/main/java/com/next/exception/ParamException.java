package com.next.exception;

/**
 * @ClassName : ParamException
 * @Description : 参数异常
 * @Author : NathenYang
 * @Date: 2020-05-24 11:06
 */

public class ParamException extends RuntimeException {


    public ParamException() {
        super();
    }

    public ParamException(String message) {
        super(message);
    }

    public ParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParamException(Throwable cause) {
        super(cause);
    }

    protected ParamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
