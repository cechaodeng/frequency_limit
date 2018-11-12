package com.kent.controller;

import com.kent.limit.SetLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Dcc on 2018/11/8.
 */
@RestController
public class TestController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SetLimit setLimit;

    @RequestMapping("/access")
    public void accessOnce(@Param("userId") String userId) {
        /**
         * 当请求过来时先判断是否超过数量
         * 如果没有，就ADD
         */
        long count = setLimit.count(userId);
        System.out.println(userId + "近" + setLimit.getTimeout() + setLimit.getTimeUnit() + "访问了" + count + "次");
        if (count == 10) {
            System.out.println(userId + "在" + setLimit.getTimeout() + setLimit.getTimeUnit().name() + "超过" + setLimit.getFrequency() + "次了，这次拒绝访问");
            return;
        }
        System.out.println(userId + "没有超过" + setLimit.getFrequency() + "次，添加这次请求进集合，并继续访问");
        setLimit.add(userId);
        System.out.println(userId + "加上这次，访问了多少次了：" + setLimit.count(userId));
    }
}
