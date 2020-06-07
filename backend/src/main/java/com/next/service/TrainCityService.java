package com.next.service;

import com.next.dao.TrainCityMapper;
import com.next.exception.BusinessException;
import com.next.model.TrainCity;
import com.next.param.TrainCityParam;
import com.next.utils.BeanValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : TrainService
 * @Description : 车站
 * @Author : NathenYang
 * @Date: 2020-05-24 21:16
 */

@Service
@Slf4j
public class TrainCityService {

    @Resource
    public TrainCityMapper trainCityMapper;


    public List<TrainCity> getAll() {
        return trainCityMapper.getAll();
    }


    public void save(TrainCityParam param) {
        log.info("save param={}", param);
        BeanValidator.check(param);
        if (checkExist(param.getCityName(), param.getId())) {
            throw new BusinessException("存在相同的城市名称");
        }
        TrainCity trainCity = TrainCity.builder().name(param.getCityName()).build();
        trainCityMapper.insertSelective(trainCity);
    }


    public void update(TrainCityParam param) {
        log.info("update param={}", param);
        BeanValidator.check(param);
        if (checkExist(param.getCityName(), param.getId())) {
            throw new BusinessException("存在相同的城市名称");
        }
        TrainCity before = trainCityMapper.selectByPrimaryKey(param.getId());
        if (before == null) {
            throw new BusinessException("待更新的城市不存在");
        }
    }


    private boolean checkExist(String cityName, Integer trainCityId) {
        return trainCityMapper.countByNameAndId(cityName, trainCityId) > 0 ? true : false;
    }
}
