package com.common.business.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.common.business.constant.RedisCacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 生成ID规则，支持集群模式
 *
 * @Author Cloud
 * @Date 2024/6/28 16:45
 **/
@Component
@Slf4j
public class IdGeneratorUtil {
    @Resource
    private RedisUtil redisUtil;

    @Value("${spring.application.name:default}")
    private String applicationName;

    public IdentifierGenerator idGenerator() {
        // 从redis中获取下一个序列号
        long seqId = getSnowFlakeKey();
        // 将序列值转换为2进制，取低10位数字，其中1-5转化为10进制后赋值给workerId，6-10转化为10进制后赋值给dataCenterId
        String binaryString = Long.toBinaryString(seqId);

        String low;
        String high;
        int length = binaryString.length();
        if (length>10) {
            low = binaryString.substring(length-5, length);
            high = binaryString.substring(length-10, length-5);
        } else if (length>5) {
            low = binaryString.substring(length-5, length);
            high = binaryString.substring(0, length-5);
        } else {
            low = binaryString;
            high = "0";
        }
        // 将二进制数字转换为10进制
        long workerId = Long.parseLong(high, 2);
        long dataCenterId = Long.parseLong(low, 2);
        log.info("雪花算法:应用名称:{},workerId:{},dataCenterId={}",applicationName, workerId, dataCenterId);
        return new DefaultIdentifierGenerator(dataCenterId, workerId);
    }

    private long getSnowFlakeKey() {
        // 没有应用一套序列号
        String redisKey = CharSequenceUtil.format(RedisCacheConstants.SNOWFLAKE_KEY, applicationName);
        return redisUtil.incr(redisKey, 1L);
    }
}
