package com.next;

import com.google.common.collect.Lists;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.List;

@SpringBootApplication
public class FrontendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }


    @Bean(name = "shardedJedisPool")
    public ShardedJedisPool shardedJedisPool() {
        JedisShardInfo shardInfo = new JedisShardInfo("127.0.0.1", 6379);
        List<JedisShardInfo> shardInfoList = Lists.newArrayList(shardInfo);
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(config, shardInfoList);
        return shardedJedisPool;
    }

}
