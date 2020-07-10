package com.kakarote.crm9.common.config.cache;

import com.jfinal.plugin.activerecord.cache.ICache;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.utils.CrmProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * redis 缓存类
 * @author honglei.wan
 */
public class RedisCache implements ICache {

    private static final String DEFAULT_CACHE_NAME = CrmProps.getInstance().get("MainRedis.cacheName");

    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    @Override
    public <T> T get(String cacheName, Object key) {
        Cache cache= Redis.use(cacheName);
        if(cache==null){
            return null;
        }
        return cache.get(key);
    }

    public <T> T get(Object key) {
        return get(DEFAULT_CACHE_NAME, key);
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        Cache cache= Redis.use(cacheName);
        if(cache==null){
            return;
        }
        cache.setex(key,1800,value);
    }
    public void put(Object key, Object value, int expireTime, TimeUnit timeUnit){
        Cache cache= Redis.use(DEFAULT_CACHE_NAME);
        if(cache==null){
            return;
        }

        cache.setex(key, (int) timeUnit.toSeconds(expireTime),value);
    }

    public void put(Object key, Object value){
        put(DEFAULT_CACHE_NAME, key, value);
    }

    @Override
    public void remove(String cacheName, Object key) {
        Cache cache= Redis.use(cacheName);
        if(cache==null){
            return;
        }
        cache.del(key);
    }

    public void remove(Object key){
        remove(DEFAULT_CACHE_NAME, key);
    }

    @Deprecated
    @Override
    public void removeAll(String cacheName) {
        logger.error("错误调用 redisCache 中 removeAll 方法");
    }

    /**
     * 判断key是否存在
     * @param cacheName
     * @param key
     * @return
     */
    private boolean exists(String cacheName, Object key) {
        Cache cache= Redis.use(cacheName);
        if(cache==null){
            return false;
        }
        return cache.exists(key);
    }

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public boolean exist(Object key) {
        return exists(DEFAULT_CACHE_NAME, key);
    }
}
