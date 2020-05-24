package com.next.controller;

import com.next.common.JsonData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName : TestController
 * @Description : 项目环境搭建验证
 * @Author : NathenYang
 * @Date: 2020-05-24 11:20
 */

@Controller
public class TestController {


    @RequestMapping("/test1")
    @ResponseBody
    public JsonData test1() {
        return JsonData.success();
    }
}
