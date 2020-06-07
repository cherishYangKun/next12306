package com.next.controller;

import com.next.common.JsonData;
import com.next.param.GenerateParam;
import com.next.service.TrainSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @ClassName : TrainSeatController
 * @Description : 座位
 * @Author : NathenYang
 * @Date: 2020-05-31 17:16
 */
@Controller
@RequestMapping("/admin/train/seat")
public class TrainSeatController {


    @Autowired
    private TrainSeatService trainSeatService;

    @RequestMapping("list.page")
    public ModelAndView list() {
        return new ModelAndView("trainSeat");
    }

    @RequestMapping("generate.json")
    @ResponseBody
    public JsonData generate(GenerateParam param) {
        trainSeatService.generate(param);
        return JsonData.success();
    }
}
