package com.next.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Resource;

/**
 * @ClassName : TrainCacheService
 * @Description :
 * @Author : NathenYang
 */
@Service
@Slf4j
public class TrainCacheService {

    @Resource(name = "shardedJedisPool")
    private ShardedJedisPool shardedJedisPool;


    private ShardedJedis instance() {
        return shardedJedisPool.getResource();
    }

    private void safeClose(ShardedJedis shardedJedis) {
        try {
            if (shardedJedis != null) {
                shardedJedis.close();
            }
        } catch (Exception e) {
            log.error("jedis close Exception", e);
        }
    }

    public void set(String cacheKey, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }

        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = instance();
            shardedJedis.set(cacheKey, value);
        } catch (Exception e) {
            log.error("jedis.set exception ,cacheKey:{},value:{}", cacheKey, value);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }


    public void get(String cacheKey) {
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = instance();
            shardedJedis.get(cacheKey);
        } catch (Exception e) {
            log.error("jedis.get exception ,cacheKey:{}", cacheKey);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }

    public void hset(String cacheKey, String field, String value) {
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = instance();
            shardedJedis.hset(cacheKey, field, value);
        } catch (Exception e) {
            log.error("jedis,hset exception ,cacheKey:{},field:{},value:{}", cacheKey, field, value);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }

    public void hincrby(String cacheKey, String field, long value) {
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = instance();
            shardedJedis.hincrBy(cacheKey, field, value);
        } catch (Exception e) {
            log.error("jedis,hincrby exception ,cacheKey:{},field:{},value:{}", cacheKey, field, value);
            throw e;
        } finally {
            safeClose(shardedJedis);
        }
    }
}
