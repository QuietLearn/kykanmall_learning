package com.mmall.util;

import com.mmall.common.RedisShardedPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ShardedJedis;

@Slf4j
public class ShardedRedisPoolUtil {
    /**
     * 重设key的超时时间 ，单位s
     * @param key
     * @param exTime
     * @return
     */
    public static Long expire(String key,Integer exTime){
        ShardedJedis shardedJedis = null;
        Long result = null;
        try {
            shardedJedis = RedisShardedPool.getJedisResource();
            result = shardedJedis.expire(key,exTime);
        } catch (Exception e) {
            log.error("set key:{}, error",key,e);
            RedisShardedPool.returnBrokenResource(shardedJedis);
            return result;
        }
        RedisShardedPool.returnResource(shardedJedis);
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
        ShardedJedis shardedJedis = null;
        String result = null;
        try {
            shardedJedis = RedisShardedPool.getJedisResource();
            result = shardedJedis.setex(key,exTime,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(shardedJedis);
            return result;
        }
        RedisShardedPool.returnResource(shardedJedis);
        return result;
    }

    public static String set(String key,String value){
        ShardedJedis shardedJedis = null;
        String result = null;
        try {
            shardedJedis = RedisShardedPool.getJedisResource();
            result = shardedJedis.set(key,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(shardedJedis);
            return result;
        }
        RedisShardedPool.returnResource(shardedJedis);
        return result;
    }

    public static String get(String key){
        ShardedJedis shardedJedis = null;
        String result = null;
        try {
            shardedJedis = RedisShardedPool.getJedisResource();
            result = shardedJedis.get(key);
        } catch (Exception e) {
            log.error("set key:{} error",key,e);
            RedisShardedPool.returnBrokenResource(shardedJedis);
            return result;
        }
        RedisShardedPool.returnResource(shardedJedis);
        return result;
    }

    public static Long del(String key){
        ShardedJedis shardedJedis = null;
        Long result = null;
        try {
            shardedJedis = RedisShardedPool.getJedisResource();
            result = shardedJedis.del(key);
        } catch (Exception e) {
            log.error("set key:{} error",key,e);
            RedisShardedPool.returnBrokenResource(shardedJedis);
            return result;
        }
        RedisShardedPool.returnResource(shardedJedis);
        return result;
    }

    public static void main(String[] args) {
        ShardedJedis shardedJedis = RedisShardedPool.getJedisResource();

        for(int i=0;i<10;i++){
            ShardedRedisPoolUtil.set("key"+i,"value"+i);
        }

        /*ShardedRedisPoolUtil.set("keyTest","value");
        ShardedRedisPoolUtil.get("keyTest");
        ShardedRedisPoolUtil.setEx("keyTest","value",60*10);
        ShardedRedisPoolUtil.expire("keyTest",60*20);

        ShardedRedisPoolUtil.del("keyTest");*/

        System.out.println("end");
    }


}
