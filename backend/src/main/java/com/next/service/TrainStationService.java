package com.next.service;

import com.next.dao.TrainCityMapper;
import com.next.dao.TrainStationMapper;
import com.next.exception.BusinessException;
import com.next.model.TrainCity;
import com.next.model.TrainStation;
import com.next.param.TrainStationParam;
import com.next.utils.BeanValidator;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : TrainStationService
 * @Description : 车站信息
 * @Author : NathenYang
 * @Date: 2020-05-24 21:17
 */
@Service
public class TrainStationService {


    @Resource
    public TrainStationMapper trainStationMapper;

    @Resource
    private TrainCityMapper trainCityMapper;


    public List<TrainStation> getAll() {
        return trainStationMapper.getAll();
    }


    public void save(TrainStationParam param) {
        BeanValidator.check(param);
        TrainCity trainCity = trainCityMapper.selectByPrimaryKey(param.getCityId());
        if (trainCity == null) {
            throw new BusinessException("车站所属城市不存在");
        }
        if (checkExist(param.getName(), param.getCityId(), param.getId())) {
            throw new BusinessException("该城市下存在相同的车站名称");
        }
        TrainStation trainStation = TrainStation.builder().name(param.getName()).cityId(param.getCityId()).build();
        trainStationMapper.insertSelective(trainStation);
    }


    public void update(TrainStationParam param) {
        BeanValidator.check(param);
        TrainCity trainCity = trainCityMapper.selectByPrimaryKey(param.getCityId());
        if (trainCity == null) {
            throw new BusinessException("车站所属城市不存在");
        }
        if (checkExist(param.getName(), param.getCityId(), param.getId())) {
            throw new BusinessException("该城市下存在相同的车站名称");
        }
        TrainStation before = trainStationMapper.selectByPrimaryKey(param.getId());
        if (before == null) {
            throw new BusinessException("待更新的站点不存在");
        }
        TrainStation trainStation = TrainStation.builder().id(param.getId()).name(param.getName()).cityId(param.getCityId()).build();
        trainStationMapper.updateByPrimaryKeySelective(trainStation);

    }

    private boolean checkExist(String name, Integer id, Integer cityId) {
        return trainStationMapper.countByIdAndNameAndCityId(name, id, cityId) > 0 ? true : false;
    }


    public Integer getCityIdByStationId(Integer stationId) {
        TrainStation trainStation = trainStationMapper.selectByPrimaryKey(stationId);
        if (trainStation == null) {
            throw new BusinessException("该车站不存在");
        }
        return trainStation.getCityId();
    }
}
