package com.next.controller;

import com.next.common.JsonData;
import com.next.common.RequestHolder;
import com.next.dto.TrainNumberLeftDto;
import com.next.model.TrainUser;
import com.next.param.CancelOrderParam;
import com.next.param.GrabTicketParam;
import com.next.param.PayOrderParam;
import com.next.param.SearchLeftCountParam;
import com.next.service.TrainOrderService;
import com.next.service.TrainSeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : FrontController
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-08-30 21:46
 */
@Controller
@RequestMapping("/front")
@Slf4j
public class FrontController {


    @Resource
    public TrainSeatService trainSeatService;

    @Resource
    public TrainOrderService trainOrderService;

    public static final int USER_NOT_LOGIN = 2;


    @PostMapping("/searchLeftCount.json")
    @ResponseBody
    public JsonData searchLeftCount(SearchLeftCountParam param) {
        try {
            List<TrainNumberLeftDto> dtoList = trainSeatService.searchLeftCount(param);
            return JsonData.success(dtoList);
        } catch (Exception e) {
            log.error("searchLeftCount is exception param={}", param);
            return JsonData.fail("查询异常,请重试");
        }
    }


    @RequestMapping("/grab.json")
    @ResponseBody
    public JsonData grabTicket(GrabTicketParam param) {
        TrainUser user = RequestHolder.getCurrentUser();
        return JsonData.success(trainSeatService.grabTicket(param, user));
    }

    @RequestMapping("/mockPay.json")
    @ResponseBody
    public JsonData payOrder(PayOrderParam param) {
        TrainUser user = RequestHolder.getCurrentUser();
        trainOrderService.payOrder(param, user.getId());
        return JsonData.success();
    }

    @RequestMapping("/mockCancel.json")
    @ResponseBody
    public JsonData cancelOrder(CancelOrderParam param) {
        TrainUser user = RequestHolder.getCurrentUser();
        trainOrderService.cancelOrder(param, user.getId());
        return JsonData.success();
    }

}
