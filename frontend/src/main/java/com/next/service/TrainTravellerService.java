package com.next.service;

import com.google.common.collect.Lists;
import com.next.dao.TrainTravellerMapper;
import com.next.dao.TrainUserTravellerMapper;
import com.next.model.TrainTraveller;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName : TrainTravellerService
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-08-31 21:38
 */
@Service
public class TrainTravellerService {


    @Resource
    public TrainTravellerMapper trainTravellerMapper;

    @Resource
    public TrainUserTravellerMapper trainUserTravellerMapper;


    public List<TrainTraveller> queryByUserId(long userId) {
        List<Long> userIds = trainUserTravellerMapper.getByUserId(userId);
        if (CollectionUtils.isEmpty(userIds)) {
            return Lists.newArrayList();
        }
        List<TrainTraveller> travellers = trainTravellerMapper.getByIdList(userIds);
        List<TrainTraveller> result = travellers.stream().map(trainTraveller ->
                TrainTraveller.builder().id(trainTraveller.getId())
                        .name(trainTraveller.getName())
                        .adultFlag(trainTraveller.getAdultFlag())
                        .idNumber(hideSensitiveMsg(trainTraveller.getIdNumber()))
                        .build()
        ).collect(Collectors.toList());
        return result;
    }

    private String hideSensitiveMsg(String msg) {
        if (StringUtils.isBlank(msg) || msg.length() < 7) {
            return msg;
        }
        return msg.substring(0, 3) + "******" + msg.substring(msg.length() - 3);
    }
}
