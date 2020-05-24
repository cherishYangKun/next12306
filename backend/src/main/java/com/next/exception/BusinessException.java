package com.next.exception;

/**
 * @ClassName : BusinessException
 * @Description : 业务异常抛出
 * @Author : NathenYang
 * @Date: 2020-05-24 11:11
 */

public class BusinessException extends RuntimeException {
    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    protected BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
