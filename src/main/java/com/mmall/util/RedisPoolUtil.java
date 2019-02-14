package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
@Slf4j
public class RedisPoolUtil {


    /**
     * 重设key的超时时间 ，单位s
     * @param key
     * @param exTime
     * @return
     */
    public static Long expire(String key,Integer exTime){
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedisResource();
            result = jedis.expire(key,exTime);
        } catch (Exception e) {
            log.error("set key:{}, error",key,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置key，value的超时时间
     * @param key
     * @param value
     * @param exTime
     * @return
     */
    public static String setEx(String key,String value,Integer exTime){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedisResource();
            result = jedis.setex(key,exTime,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,value,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String set(String key,String value){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedisResource();
            result = jedis.set(key,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,value,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String get(String key){
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedisResource();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("set key:{} error",key,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static Long del(String key){
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedisResource();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("set key:{} error",key,e);
            RedisPool.returnBrokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        Jedis jedis = RedisPool.getJedisResource();
        RedisPoolUtil.set("keyTest","value");
        RedisPoolUtil.get("keyTest");
        RedisPoolUtil.setEx("keyTest","value",60*10);
        RedisPoolUtil.expire("keyTest",60*20);

        RedisPoolUtil.del("keyTest");

        System.out.println("end");
    }
}
