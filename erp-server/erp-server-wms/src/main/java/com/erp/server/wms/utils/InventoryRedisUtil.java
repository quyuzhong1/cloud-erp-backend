package com.erp.server.wms.utils;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.common.business.utils.AbstractRedisUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InventoryRedisUtil extends AbstractRedisUtil{

    @Resource
    private RedisTemplate inventoryRedisTemplate;

	@Override
	public RedisTemplate getRedisTemplate() {
		return inventoryRedisTemplate;
	}
}
