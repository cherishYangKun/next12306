package com.next.service;

import com.next.exception.BusinessException;
import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import com.next.mq.MessageBody;
import com.next.mq.QueueTopic;
import com.next.mq.RabbitMqClient;
import com.next.orderDao.TrainOrderDetailMapper;
import com.next.orderDao.TrainOrderMapper;
import com.next.param.CancelOrderParam;
import com.next.param.PayOrderParam;
import com.next.util.BeanValidator;
import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName : TrainOrderService
 * @Description :订单超时业务
 * @Author : NathenYang
 * @Date: 2020-09-03 00:13
 */
@Service
@Slf4j
public class TrainOrderService {

    @Resource
    public TrainOrderMapper trainOrderMapper;

    @Resource
    public TrainOrderDetailMapper trainOrderDetailMapper;

    @Resource
    public RabbitMqClient rabbitMqClient;


    /**
     * 检查订单超时业务处理
     *
     * @param trainOrder
     */
    public void delayCheckOrder(TrainOrder trainOrder) {
        log.info("delay check order={}", trainOrder);
        TrainOrder order = trainOrderMapper.findByOrderId(trainOrder.getOrderId());
        if (order == null) {
            log.info("delay check order is null order={}", order);
            return;
        }
        if (order.getStatus() == 10) { //订单已超时 取消订单
            log.info("order pay expired ,force cancel order ={}", order);
            order.setStatus(30); //取消
            trainOrderMapper.updateByPrimaryKeySelective(order);

            //取消订单发送消息
            MessageBody messageBody = new MessageBody();
            messageBody.setTopic(QueueTopic.ORDER_CANCAL);
            messageBody.setContent(JsonMapper.obj2String(order));
            rabbitMqClient.send(messageBody);

        }
    }

    public List<TrainOrder> getOrderList(long userId) {
        return trainOrderMapper.getByUserId(userId);
    }

    public List<TrainOrderDetail> getOrderDetailList(List<String> parentOrderIdList) {
        return trainOrderDetailMapper.getByParentOrderIds(parentOrderIdList);
    }

    public void payOrder(PayOrderParam param, long userId) {
        BeanValidator.check(param);
        TrainOrder trainOrder = trainOrderMapper.findByOrderId(param.getOrderId());
        if (trainOrder == null) {
            throw new BusinessException("未查询到该订单");
        }
        if (trainOrder.getUserId() != userId) {
            throw new BusinessException("不允许操作他人的订单");
        }
        if (trainOrder.getStatus() != 10) {
            throw new BusinessException("订单不合法，请刷新后操作");
        }
        trainOrder.setStatus(20);
        trainOrderMapper.updateByPrimaryKeySelective(trainOrder);

        MessageBody messageBody = new MessageBody();
        messageBody.setTopic(QueueTopic.ORDER_PAY_SUCCESS);
        messageBody.setContent(JsonMapper.obj2String(trainOrder));
        rabbitMqClient.send(messageBody);
    }

    public void cancelOrder(CancelOrderParam param, long userId) {
        BeanValidator.check(param);
        TrainOrder trainOrder = trainOrderMapper.findByOrderId(param.getOrderId());
        if (trainOrder == null) {
            throw new BusinessException("未查询到该订单");
        }
        if (trainOrder.getUserId() != userId) {
            throw new BusinessException("不允许操作他人的订单");
        }
        if (trainOrder.getStatus() != 20) {
            throw new BusinessException("订单不合法，请刷新后操作");
        }
        if (trainOrder.getTrainStart().before(new Date())) {
            throw new BusinessException("订单已经过了可取消的时间段");
        }
        trainOrder.setStatus(40);
        trainOrderMapper.updateByPrimaryKeySelective(trainOrder);

        MessageBody messageBody = new MessageBody();
        messageBody.setTopic(QueueTopic.ORDER_CANCAL);
        messageBody.setContent(JsonMapper.obj2String(trainOrder));
        rabbitMqClient.send(messageBody);
    }


}
