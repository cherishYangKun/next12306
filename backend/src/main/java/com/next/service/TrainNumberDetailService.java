package com.next.service;

import com.next.dao.TrainNumberDetailMapper;
import com.next.dao.TrainNumberMapper;
import com.next.exception.BusinessException;
import com.next.model.TrainNumber;
import com.next.model.TrainNumberDetail;
import com.next.param.TrainNumberDetailParam;
import com.next.utils.BeanValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : TrainNumberDetailService
 * @Description : 车次详情
 * @Author : NathenYang
 * @Date: 2020-05-24 21:17
 */
@Service
public class TrainNumberDetailService {

    @Resource
    private TrainNumberDetailMapper trainNumberDetailMapper;

    @Resource
    private TrainNumberMapper trainNumberMapper;


    @Autowired
    private TrainStationService trainStationService;


    public List<TrainNumberDetail> getAll() {
        return trainNumberDetailMapper.getAll();
    }


    public void save(TrainNumberDetailParam param) {
        BeanValidator.check(param);
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(param.getTrainNumberId());
        if (trainNumber == null) {
            throw new BusinessException("该车次不存在");
        }
        //查询车次对应的车次详情列表
        List<TrainNumberDetail> detailList = trainNumberDetailMapper.getByTrainNumberId(param.getTrainNumberId());
        //组装数据
        TrainNumberDetail trainNumberDetail = TrainNumberDetail.builder()
                .fromStationId(param.getFromStationId())
                .toStationId(param.getToStationId())
                .relativeMinute(param.getRelativeMinute())
                .waitMinute(param.getWaitMinute())
                .money(param.getMoney())
                .fromCityId(trainStationService.getCityIdByStationId(param.getFromStationId()))
                .toCityId(trainStationService.getCityIdByStationId(param.getToStationId()))
                .stationIndex(detailList.size()).build();
        //保存db
        trainNumberDetailMapper.insertSelective(trainNumberDetail);
        //校验车次详情是否已添加完成
        if (param.getEnd() == 1) {
            detailList.add(trainNumberDetail);
            //更新车次 出发站  终点站 信息
            trainNumber.setFromStationId(detailList.get(0).getFromStationId());
            trainNumber.setFromCityId(detailList.get(0).getFromCityId());
            trainNumber.setToStationId(detailList.get(detailList.size() - 1).getToStationId());
            trainNumber.setToCityId(detailList.get(detailList.size() - 1).getToCityId());
            trainNumberMapper.updateByPrimaryKeySelective(trainNumber);
        }
        //TODO 考虑 前端 查询两个车站间所有的车次信息
    }

    public void delete(Integer id) {
        trainNumberDetailMapper.deleteByPrimaryKey(id);
    }
}
