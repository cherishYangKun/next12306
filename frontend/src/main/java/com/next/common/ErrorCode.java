package com.next.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName : ErrorCode
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-05 22:45
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SYSTEM_ERROR(1, "系统异常"),
    USER_NOT_LOGIN(2, "用户未登录");


    int code;

    String desc;
}
