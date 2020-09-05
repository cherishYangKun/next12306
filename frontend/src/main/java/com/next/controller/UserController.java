package com.next.controller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.next.common.JsonData;
import com.next.common.RequestHolder;
import com.next.dto.TrainOrderExtDto;
import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import com.next.model.TrainTraveller;
import com.next.model.TrainUser;
import com.next.service.TrainOrderService;
import com.next.service.TrainStationService;
import com.next.service.TrainTravellerService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName : UserController
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-08-31 21:38
 */
@Controller
@RequestMapping("/user")
public class UserController {


    @Resource
    public TrainTravellerService trainTravellerService;

    @Resource
    public TrainOrderService trainOrderService;

    @Resource
    public TrainStationService trainStationService;


    @RequestMapping("getTravellers.json")
    @ResponseBody
    public JsonData getTravellers(HttpServletRequest request) {
        TrainUser user = RequestHolder.getCurrentUser();
        List<TrainTraveller> data =
                trainTravellerService.queryByUserId(user.getId());
        return JsonData.success(data);
    }


    @RequestMapping("/getOrderList.json")
    @ResponseBody
    public JsonData getOrderList(HttpServletRequest request) {
        TrainUser user = RequestHolder.getCurrentUser();
        List<TrainOrder> orderList = trainOrderService.getOrderList(user.getId());
        if (CollectionUtils.isEmpty(orderList)) {
            return JsonData.success();
        }

        List<TrainTraveller> trainTravellerList = trainTravellerService.queryByUserId(user.getId());
        Map<Long, String> travellerNameMap = Maps.newHashMap();
        trainTravellerList.parallelStream().forEach(trainTraveller ->
                travellerNameMap.put(trainTraveller.getId(), trainTraveller.getName()));

        List<String> orderIdList = orderList.stream().map(order -> order.getOrderId()).collect(Collectors.toList());
        List<TrainOrderDetail> orderDetailList = trainOrderService.getOrderDetailList(orderIdList);
        // orderId -> Collection<TrainOrderDetail>
        Multimap<String, TrainOrderDetail> orderDetailMultimap = HashMultimap.create();
        orderDetailList.parallelStream().forEach(trainOrderDetail ->
                orderDetailMultimap.put(trainOrderDetail.getParentOrderId(), trainOrderDetail));

        DateTimeFormatter df = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        ZoneId zoneId = ZoneId.systemDefault();

        List<TrainOrderExtDto> dtoList = orderList.stream().map(order -> {
            TrainOrderExtDto dto = new TrainOrderExtDto();
            dto.setTrainOrder(order);
            dto.setFromStationName(trainStationService.getStationNameById(order.getFromStationId()));
            dto.setToStationName(trainStationService.getStationNameById(order.getToStationId()));
            dto.setShowPay(order.getStatus() == 10);
            dto.setShowCancel(order.getStatus() == 20);

            LocalDateTime startTime = order.getTrainStart().toInstant().atZone(zoneId).toLocalDateTime();
            LocalDateTime endTime = order.getTrainEnd().toInstant().atZone(zoneId).toLocalDateTime();
            Collection<TrainOrderDetail> tmpOrderDetailList = orderDetailMultimap.get(order.getOrderId());
            dto.setSeatInfo(startTime.format(df) + "~" + endTime.format(df) + " " +
                    generateSeatInfo(tmpOrderDetailList, travellerNameMap) + " " +
                    "(金额：" + order.getTotalMoney() + "元）");
            return dto;
        }).collect(Collectors.toList());

        return JsonData.success(dtoList);
    }

    private String generateSeatInfo(Collection<TrainOrderDetail> tmpOrderDetailList, Map<Long, String> travellerNameMap) {
        if (CollectionUtils.isEmpty(tmpOrderDetailList)) {
            return "";
        }
        int index = 0;
        StringBuilder stringBuilder = new StringBuilder(tmpOrderDetailList.size() * 20);
        for (TrainOrderDetail trainOrderDetail : tmpOrderDetailList) {
            if (trainOrderDetail.getTravellerId() == 0 || !travellerNameMap.containsKey(trainOrderDetail.getTravellerId())) {
                // 异常数据
                continue;
            }
            if (index > 0) {
                stringBuilder.append("; ");
            }
            stringBuilder.append(travellerNameMap.get(trainOrderDetail.getTravellerId())).append(" ")
                    .append(trainOrderDetail.getCarriageNumber()).append("车")
                    .append(trainOrderDetail.getRowNumber()).append("排")
                    .append(trainOrderDetail.getSeatNumber()).append("座");
            index++;
        }
        return stringBuilder.toString();
    }
}
