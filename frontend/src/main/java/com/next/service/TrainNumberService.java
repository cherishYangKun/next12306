package com.next.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.next.dao.TrainNumberMapper;
import com.next.model.TrainNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : TrainNumberService
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-08-30 22:08
 */
@Service
@Slf4j
public class TrainNumberService {


    @Resource
    public TrainNumberMapper trainNumberMapper;

    public static Cache<String, TrainNumber> trainNumberCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();


    public TrainNumber findByNameFromCache(String name) {
        TrainNumber trainNumber = trainNumberCache.getIfPresent(name);
        if (trainNumber != null) {
            return trainNumber;

        }
        trainNumber = trainNumberMapper.findByName(name);
        if (trainNumber != null) {
            trainNumberCache.put(name, trainNumber);
        }
        return trainNumber;
    }
}
