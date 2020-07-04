package com.next.controller;

import com.next.beans.PageQuery;
import com.next.beans.PageResult;
import com.next.common.JsonData;
import com.next.dto.TrainSeatDto;
import com.next.model.TrainSeat;
import com.next.model.TrainStation;
import com.next.param.GenerateParam;
import com.next.param.PublishTicketParam;
import com.next.param.TrainSeatSearchParam;
import com.next.service.TrainSeatService;
import com.next.service.TrainStationService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private TrainStationService trainStationService;

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

    @RequestMapping("search.json")
    @ResponseBody
    public JsonData search(TrainSeatSearchParam param, PageQuery pageQuery) {
        Integer total = trainSeatService.countList(param);
        if (total == 0) {
            return JsonData.success(PageResult.<TrainSeatDto>builder().total(0).build());
        }
        List<TrainSeat> trainSeats = trainSeatService.searchList(param, pageQuery);
        if (CollectionUtils.isEmpty(trainSeats)) {
            return JsonData.success(PageResult.<TrainSeatDto>builder().total(0).build());
        }
        List<TrainStation> stationList = trainStationService.getAll();
        Map<Integer, String> stationMap = stationList.stream().collect(Collectors.toMap(TrainStation::getId, TrainStation::getName));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ZoneId zoneId = ZoneId.systemDefault();
        List<TrainSeatDto> dtoList = trainSeats.stream().map(trainSeat -> {
            TrainSeatDto trainSeatDto = new TrainSeatDto();
            trainSeatDto.setId(trainSeat.getId());
            trainSeatDto.setTrainNumber(param.getTrainNumber());
            trainSeatDto.setCarriageNumber(trainSeat.getCarriageNumber());
            trainSeatDto.setRowNumber(trainSeat.getRowNumber());
            trainSeatDto.setSeatNumber(trainSeat.getSeatNumber());
            trainSeatDto.setFromStation(stationMap.get(trainSeat.getFromStationId()));
            trainSeatDto.setToStation(stationMap.get(trainSeat.getToStationId()));
            trainSeatDto.setSeatLevel(trainSeat.getSeatLevel());
            trainSeatDto.setShowStart(LocalDateTime.ofInstant(trainSeat.getTrainStart().toInstant(), zoneId).format(formatter));
            trainSeatDto.setShowEnd(LocalDateTime.ofInstant(trainSeat.getTrainEnd().toInstant(), zoneId).format(formatter));
            trainSeatDto.setFromStationId(trainSeat.getFromStationId());
            trainSeatDto.setToStationId(trainSeat.getToStationId());
            trainSeatDto.setMoney(trainSeat.getMoney());
            trainSeatDto.setStatus(trainSeat.getStatus());
            trainSeatDto.setTrainNumberId(trainSeat.getTrainNumberId());
            return trainSeatDto;
        }).collect(Collectors.toList());
        return JsonData.success(PageResult.<TrainSeatDto>builder().total(total).data(dtoList).build());
    }




    @RequestMapping("publish.json")
    @ResponseBody
    public JsonData publish(PublishTicketParam param) {
        trainSeatService.publish(param);
        return JsonData.success();
    }
}
