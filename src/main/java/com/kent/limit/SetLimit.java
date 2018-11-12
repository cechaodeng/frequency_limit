package com.kent.limit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dcc on 2018/11/8.
 */
@Component
public class SetLimit implements Limit {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //private String userId = "zzsb";//当前用户id，实际生产中应该从session中取值


    @Value("${limit.frequency}")
    private int frequency;

    @Value("${limit.timeout}")
    private int timeout;

    private TimeUnit timeUnit;

    private SetLimit() {
        this.timeUnit = TimeUnit.SECONDS;
    }

    public SetLimit(int frequency, int timeout, TimeUnit timeUnit) {
        this.frequency = frequency;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    /**
     * 每次访问调用一次add，表示访问了一次
     */
    @Override
    public void add(String userId) {
        String redisQueueKey = userId + "setQueue";
        //判断该用户的编号队列是否存在，不存在说明该用户没有登录
        List<String> list = new ArrayList<>();
        list.add(redisQueueKey);
        long num = stringRedisTemplate.countExistingKeys(list);
        if (num == 0) {
            System.out.println(userId + "用户没有登录");
            //模拟登录
            for (int i = 0; i < frequency; i++) {
                stringRedisTemplate.opsForList().leftPush(redisQueueKey, String.valueOf(i));
            }
            //根据业务需求，有些用户存在的时候不需要太长，我这里设置30分钟
            stringRedisTemplate.expire(redisQueueKey, 30, TimeUnit.MINUTES);
        }
        //获取集合中Key的编号
        String no = stringRedisTemplate.opsForList().rightPopAndLeftPush(redisQueueKey, redisQueueKey);
        //将带编号的key放进集合，并加上过期时间
        stringRedisTemplate.opsForSet().add(userId + "limitSet" + no, "");
        stringRedisTemplate.expire(userId + "limitSet" + no, timeout, timeUnit);//1分钟过期
    }

    /**
     * 统计用做访问记录的key的数量，也就是统计在规定时间访问了多少次
     */
    @Override
    public long count(String userId) {
        /**
         * 根据用户名初始化key集合
         */
        List keyList = new ArrayList(frequency);
        for (int i = 0; i < frequency; i++) {
            keyList.add(userId + "limitSet" + i);
        }
        Long keyNum = stringRedisTemplate.countExistingKeys(keyList);
        return keyNum;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
