package com.next.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName : RabbitMqDelayConfig
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-01 18:40
 */
@Configuration
public class RabbitMqDelayConfig {


    @Bean("delayDirectExchange")
    public DirectExchange directExchange() {
        DirectExchange directExchange = new DirectExchange(QueueConstant.DELAY_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;

    }

    @Bean("delayNotifyQuene")
    public Queue notifyQuene() {
        return new Queue(QueueConstant.DELAY_QUENE);

    }

    @Bean("binding")
    public Binding binding(@Qualifier("delayDirectExchange") DirectExchange directExchange, @Qualifier("delayNotifyQuene") Queue notifyQuene) {
        return BindingBuilder.bind(notifyQuene).to(directExchange).with(QueueConstant.DELAY_BINDING);
    }
}
