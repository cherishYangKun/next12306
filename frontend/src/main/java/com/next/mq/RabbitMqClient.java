package com.next.mq;

import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @ClassName : RabbitMqClient
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-01 19:23
 */
@Component
@Slf4j
public class RabbitMqClient {


    @Resource
    public RabbitTemplate rabbitTemplate;


    public void send(MessageBody messageBody) {
        try {
            String uuid = UUID.randomUUID().toString();
            CorrelationData correlationData = new CorrelationData(uuid);
            rabbitTemplate.convertAndSend(QueueConstant.COMMON_EXCHANGE, QueueConstant.COMMON_BINDING, JsonMapper.obj2String(messageBody), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT); //消息持久化
                    log.info("send message, {}", message);
                    return message;
                }
            }, correlationData);
        } catch (AmqpException e) {
            log.error("send message exception msg={}", messageBody.toString(), e);
        }
    }

    public void sendDelay(MessageBody messageBody, int delayTimeMills) {
        try {
            String uuid = UUID.randomUUID().toString();
            messageBody.setDelay(delayTimeMills);
            CorrelationData correlationData = new CorrelationData(uuid);
            rabbitTemplate.convertAndSend(QueueConstant.DELAY_EXCHANGE, QueueConstant.DELAY_BINDING, JsonMapper.obj2String(messageBody), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT); //消息持久化
                    message.getMessageProperties().setDelay(delayTimeMills); //设置延迟 时间
                    log.info("delay send message, {}", message);
                    return message;
                }
            }, correlationData);
        } catch (AmqpException e) {
            log.error("delay send message exception msg={}", messageBody.toString(), e);
        }
    }
}
