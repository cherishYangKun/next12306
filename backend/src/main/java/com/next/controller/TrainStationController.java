package com.next.controller;

import com.next.common.JsonData;
import com.next.dto.TrainStationDto;
import com.next.model.TrainCity;
import com.next.model.TrainStation;
import com.next.param.TrainStationParam;
import com.next.service.TrainCityService;
import com.next.service.TrainStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName : TrainStationController
 * @Description : 车站信息维护
 * @Author : NathenYang
 * @Date: 2020-05-24 21:08
 */

@Controller
@RequestMapping("/admin/train/station")
public class TrainStationController {

    @Autowired
    private TrainStationService trainStationService;

    @Autowired
    private TrainCityService trainCityService;

    @RequestMapping("list.page")
    public ModelAndView page() {
        return new ModelAndView("trainStation");
    }


    @RequestMapping("/list.json")
    @ResponseBody
    public JsonData list() {
        List<TrainStation> stationList = trainStationService.getAll();
        List<TrainCity> cityList = trainCityService.getAll();
        Map<Integer, String> mapCity = cityList.stream().collect(Collectors.toMap(TrainCity::getId, TrainCity::getName));
        //注：如果使用stream编程容易占内存  性能内存不足的情况下 尽量不要使用  不然会出现不停的GC FGC
        List<TrainStationDto> result = stationList.stream().map(trainStation -> {
            TrainStationDto dto = new TrainStationDto();
            dto.setId(trainStation.getId());
            dto.setName(trainStation.getName());
            dto.setCityId(trainStation.getCityId());
            dto.setCityName(mapCity.get(trainStation.getCityId()));
            return dto;
        }).collect(Collectors.toList());
        return JsonData.success(result);
    }


    @RequestMapping(value = "/save.json")
    @ResponseBody
    public JsonData save(TrainStationParam param) {
        trainStationService.save(param);
        return JsonData.success();
    }

    @RequestMapping(value = "update.json")
    @ResponseBody
    public JsonData update(TrainStationParam param) {
        trainStationService.update(param);
        return JsonData.success();
    }

}
