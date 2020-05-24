package com.next.common;

import com.next.exception.BusinessException;
import com.next.exception.ParamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName : GlobalExceptionHandler
 * @Description : 全局异常处理
 * @Author : NathenYang
 * @Date: 2020-05-24 11:12
 */

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public JsonData exceptionHandler(RuntimeException ex) {
        log.error("unknow exception", ex);
        if (ex instanceof ParamException || ex instanceof BusinessException) {
            return JsonData.fail(ex.getMessage());
        }

        return JsonData.fail("系统异常 请稍后再试");
    }


    @ExceptionHandler(Error.class)
    @ResponseBody
    public JsonData errorHandler(Error ex) {
        log.error("unknow error", ex);
        return JsonData.fail("系统异常,请联系管理员");

    }
}
