package com.common.business.utils;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisUtil extends AbstractRedisUtil{

    @Resource
    private RedisTemplate redisTemplate;

	@Override
	public RedisTemplate getRedisTemplate() {
		return redisTemplate;
	}
}
