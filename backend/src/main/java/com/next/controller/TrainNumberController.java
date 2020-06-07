package com.next.controller;

import com.next.common.JsonData;
import com.next.dto.TrainNumberDto;
import com.next.model.TrainNumber;
import com.next.model.TrainStation;
import com.next.param.TrainNumberParam;
import com.next.service.TrainNumberService;
import com.next.service.TrainStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName : TrainStationController
 * @Description : 车次信息
 * @Author : NathenYang
 * @Date: 2020-05-24 21:08
 */

@Controller
@RequestMapping("/admin/train/number")
public class TrainNumberController {

    @Autowired
    private TrainNumberService trainNumberService;

    @Autowired
    private TrainStationService trainStationService;

    @RequestMapping("list.page")
    public ModelAndView page() {
        return new ModelAndView("trainNumber");
    }


    @RequestMapping("/list.json")
    @ResponseBody
    public JsonData list() {
        List<TrainNumber> trainNumberList = trainNumberService.getAll();
        List<TrainStation> stationList = trainStationService.getAll();
        Map<Integer, String> stationMap = stationList.stream().collect(Collectors.toMap(TrainStation::getId, TrainStation::getName));
        List<TrainNumberDto> result = trainNumberList.stream().map(trainNumber -> {
            TrainNumberDto dto = new TrainNumberDto();
            dto.setId(trainNumber.getId());
            dto.setFromStationId(trainNumber.getFromStationId());
            dto.setToStationId(trainNumber.getToStationId());
            dto.setFromCityId(trainNumber.getFromCityId());
            dto.setToCityId(trainNumber.getToCityId());
            dto.setName(trainNumber.getName());
            dto.setSeatNum(trainNumber.getSeatNum());
            dto.setTrainType(trainNumber.getTrainType());
            dto.setType(trainNumber.getType());
            dto.setSeatNum(trainNumber.getSeatNum());
            dto.setFromStation(stationMap.get(trainNumber.getFromStationId()));
            dto.setToStation(stationMap.get(trainNumber.getToStationId()));
            return dto;
        }).collect(Collectors.toList());
        return JsonData.success(result);
    }


    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData save(TrainNumberParam param) {
        trainNumberService.save(param);
        return JsonData.success();
    }

    @RequestMapping("update.json")
    @ResponseBody
    public JsonData update(TrainNumberParam param) {
        trainNumberService.update(param);
        return JsonData.success();
    }

}
