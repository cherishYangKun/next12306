package com.next.mq;

import com.google.common.collect.Lists;
import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import com.next.orderDao.TrainOrderDetailMapper;
import com.next.seatDao.TrainSeatMapper;
import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : RabbitMqServer
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-01 19:43
 */
@Component
@Slf4j
public class RabbitMqServer {

    @Resource
    public TrainOrderDetailMapper trainOrderDetailMapper;

    @Resource
    public TrainSeatMapper trainSeatMapper;


    @RabbitListener(queues = QueueConstant.COMMON_QUENE)
    public void receive(String message) {
        log.info("common receive message={}", message);
        //消息服务网络异常 超时 服务宕机 捕获
        try {
            MessageBody messageBody = JsonMapper.string2Obj(message, new TypeReference<MessageBody>() {
            });
            if (message == null) {
                return;
            }
            switch (messageBody.getTopic()) {
                case QueueTopic.ORDER_CREATE:
                    //创建订单成功 给用户发送消息 -> 拓展
                    //TODO
                    break;
                case QueueTopic.ORDER_CANCAL:
                    //订单取消
                    TrainOrder trainOrder = JsonMapper.string2Obj(messageBody.getContent(), new TypeReference<TrainOrder>() {
                    });
                    //订单取消需要回滚 已占座位 和数据
                    List<TrainOrderDetail> trainOrderDetails = trainOrderDetailMapper.getByParentOrderIds(Lists.newArrayList(trainOrder.getOrderId()));
                    for (TrainOrderDetail trainOrderDetail : trainOrderDetails) {
                        trainSeatMapper.cancelSeat(trainOrderDetail.getTrainNumberId(), trainOrderDetail.getTicket(), trainOrderDetail.getCarriageNumber()
                                , trainOrderDetail.getRowNumber(), trainOrderDetail.getSeatNumber(), trainOrderDetail.getTravellerId(), trainOrderDetail.getUserId()
                        );
                    }
                    break;
                default:
                    log.warn("common receive  message={}, no need handle", message);

            }
        } catch (Exception e) {
            log.error("common receive message exception message={}", message, e);
        }

    }
}


