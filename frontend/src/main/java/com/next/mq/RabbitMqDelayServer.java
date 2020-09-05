package com.next.mq;

import com.next.dto.RollBackSeatDto;
import com.next.model.TrainOrder;
import com.next.service.TrainOrderService;
import com.next.service.TrainSeatService;
import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName : RabbitMqServer
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-01 19:43
 */
@Component
@Slf4j
public class RabbitMqDelayServer {


    @Resource
    public TrainSeatService trainSeatService;

    @Resource
    public TrainOrderService trainOrderService;


    @RabbitListener(queues = QueueConstant.DELAY_QUENE)
    public void receive(String message) {
        log.info("delay receive message={}", message);
        //消息服务网络异常 超时 服务宕机 捕获
        try {
            MessageBody messageBody = JsonMapper.string2Obj(message, new TypeReference<MessageBody>() {
            });
            if (message == null) {
                return;
            }
            switch (messageBody.getTopic()) {
                case QueueTopic.SEAT_PLACE_ROLLBACK:  //消息队列重试 座位回滚
                    RollBackSeatDto rollBackSeatDto = JsonMapper.string2Obj(messageBody.getContent(), new TypeReference<RollBackSeatDto>() {
                    });
                    //消息延迟发送 重新 座位回滚
                    trainSeatService.rollBackSeat(rollBackSeatDto.getTrainSeat(), rollBackSeatDto.getFromStationIds(), 0);
                    break;
                case QueueTopic.ORDER_PAY_DELAY_CHECK: //订单超时检查
                    TrainOrder trainOrder = JsonMapper.string2Obj(messageBody.getContent(), new TypeReference<TrainOrder>() {
                    });
                    trainOrderService.delayCheckOrder(trainOrder); //处理订单超时
                    break;
                default:
                    log.warn("delay receive  message={}, no need handle", message);

            }
        } catch (Exception e) {
            log.error("delay receive message exception message={}", message, e);
        }
    }
}


