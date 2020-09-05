package com.next.service;

import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import com.next.orderDao.TrainOrderDetailMapper;
import com.next.orderDao.TrainOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : TransactionService
 * @Description : 事务性操作
 * @Author : NathenYang
 * @Date: 2020-09-02 22:31
 */
@Service
public class TransactionService {

    @Resource
    public TrainOrderMapper trainOrderMapper;

    @Resource
    public TrainOrderDetailMapper trainOrderDetailMapper;


    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(TrainOrder trainOrder, List<TrainOrderDetail> trainOrderDetails) {
        for (TrainOrderDetail trainOrderDetail : trainOrderDetails) {
            trainOrderDetailMapper.insertSelective(trainOrderDetail);
        }
        trainOrderMapper.insertSelective(trainOrder);
    }
}
