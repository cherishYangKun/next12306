package com.next.mq;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : MessageBody
 * @Description :消息体
 * @Author : NathenYang
 * @Date: 2020-09-01 18:37
 */
@Getter
@Setter
@ToString
public class MessageBody {

    public int topic;

    public int delay;

    public long sendTime = System.currentTimeMillis();

    public String content;


}
