package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

public class RedisShardedPool {
    private static ShardedJedisPool pool;//jedis连接池
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20")); //最大连接数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","20"));//在jedispool中最大的idle状态(空闲的)的jedis实例的个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","10"));//在jedispool中最小的idle状态(空闲的)的jedis实例的个数

    //当我们从jedis pool中包有一个jedis实例，拿一个jedis实例，即java与redis服务端的通信客户端，是否需要测试
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));//在borrow一个jedis实例的时候，是否要进行验证操作，如果赋值true。则得到的jedis实例肯定是可以用的。

    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return","false"));//在return一个jedis实例的时候，是否要进行验证操作，如果赋值true。则放回jedispool的jedis实例肯定是可以用的。

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));

    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();


        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);//连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。

        JedisShardInfo shard1Info = new JedisShardInfo(redis1Ip,redis1Port,1000*2);
        JedisShardInfo shard2Info = new JedisShardInfo(redis2Ip,redis2Port,1000*2);
        List<JedisShardInfo> shardInfoList = new ArrayList<>(2);
        shardInfoList.add(shard1Info);
        shardInfoList.add(shard2Info);
        //MURMUR_HASH----->consistent hashing 一致性算法
        //后面的格式可能是用来hash的函数
        pool = new ShardedJedisPool(config,shardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    /**
     * 为了这个类在加载到jvm的时候,就初始化连接池
     */
    static{
        initPool();
    }

    public static ShardedJedis getJedisResource(){
        return pool.getResource();
    }

    public static void returnBrokenResource(ShardedJedis shardedJedis){
        pool.returnBrokenResource(shardedJedis);
    }

    public static void returnResource(ShardedJedis shardedJedis){
        pool.returnResource(shardedJedis);
    }

    public static void main(String[] args) {
        ShardedJedis shardedJedis = RedisShardedPool.getJedisResource();
        for(int i=0;i<10;i++){
            shardedJedis.set("key"+i,"value"+i);
        }
//        shardedJedis.set("hyj","fighting");
        RedisShardedPool.returnResource(shardedJedis);

        //pool.destroy();//临时调用，销毁连接池中的所有连接
        System.out.println("program is end");
    }
}
