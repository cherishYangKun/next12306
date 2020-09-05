package com.next.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @ClassName : RabbitMqConfig
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-01 18:40
 */
@Configuration
public class RabbitMqConfig {


    @Bean("directExchange")
    @Primary //默认exchange
    public DirectExchange directExchange() {
        return new DirectExchange(QueueConstant.COMMON_EXCHANGE, true, false);

    }

    @Bean("notifyQuene")
    @Primary //默认队列
    public Queue notifyQuene() {
        return new Queue(QueueConstant.COMMON_QUENE);

    }

    @Bean("bindingNotify")
    @Primary //默认路由关系
    public Binding binding(@Qualifier("directExchange") DirectExchange directExchange, @Qualifier("notifyQuene") Queue notifyQuene) {
        return BindingBuilder.bind(notifyQuene).to(directExchange).with(QueueConstant.COMMON_BINDING);
    }
}
