package com.next.controller;

import com.next.common.JsonData;
import com.next.model.TrainCity;
import com.next.param.TrainCityParam;
import com.next.service.TrainCityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @ClassName : TrainCityController
 * @Description : 城市信息维护
 * @Author : NathenYang
 * @Date: 2020-05-24 21:07
 */

@Controller
@RequestMapping("/admin/train/city")
public class TrainCityController {

    @Autowired
    private TrainCityService trainCityService;


    @RequestMapping("list.page")
    public ModelAndView page() {
        return new ModelAndView("trainCity");
    }


    @RequestMapping("/list.json")
    @ResponseBody
    public JsonData list() {
        return JsonData.success(trainCityService.getAll());
    }


    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData save(TrainCityParam param) {
        trainCityService.save(param);
        return JsonData.success();
    }

    @RequestMapping("update.json")
    @ResponseBody
    public JsonData update(TrainCityParam param) {
        trainCityService.save(param);
        return JsonData.success();
    }


}
