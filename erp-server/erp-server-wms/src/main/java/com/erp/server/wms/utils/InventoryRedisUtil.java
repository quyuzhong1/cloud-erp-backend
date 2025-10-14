package com.erp.server.wms.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import com.common.business.utils.AbstractRedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.enums.inventory.InventoryRedisOpEnum;
import com.erp.model.wms.enums.inventory.InventoryRedisOpResultEnum;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InventoryRedisUtil extends AbstractRedisUtil{
	
	private static String splitSign = "@@";

	private static RedisSerializer stringRedisSerializer = new StringRedisSerializer();
	
	@Resource
    private RedissonClient inventoryRedissonClient;
	
    @Resource
    private RedisTemplate inventoryRedisTemplate;

	@Override
	public RedisTemplate getRedisTemplate() {
		return inventoryRedisTemplate;
	}
	
	public RedissonMultiLock tryLock(String key) {
		return this.tryLock(Arrays.asList(key));
	}
	
	public RedissonMultiLock tryLock(List<String> keys) {
		List<RLock> rLocks = keys.stream()
                .map(inventoryRedissonClient::getLock)
                .collect(Collectors.toList());
		RedissonMultiLock multiLock = new RedissonMultiLock(rLocks.toArray(new RLock[0]));
		boolean locked = false;
    	try {
			locked = multiLock.tryLock(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("库存重算获取锁失败" , e);
			Thread.currentThread().interrupt();
		}
    	if(locked) {
    		return multiLock;
    	}else {
    		return null;
    	}
	}
	
	public void unLock(RedissonMultiLock redissonMultiLock) {
		if(redissonMultiLock != null) {
			redissonMultiLock.unlock();
		}
	}
	
	public void execute(InventoryRedisOpEnum inventoryRedisOpEnum , Object... args) {
		InventoryRedisOpResultEnum inventoryRedisOpResultEnum = null;
		String opName = inventoryRedisOpEnum.getName();
		int i = 0;
		String resultCode = "";
		String resultName = "";
		boolean isRetry = false;
		String execute = "";
		while(i < 3) {
			execute = (String) inventoryRedisTemplate.execute(InventoryRedisOpEnum.getDefaultRedisScript(inventoryRedisOpEnum), stringRedisSerializer, stringRedisSerializer, Arrays.asList(), args);
			if(StringUtils.isNotBlank(execute)) {
				String[] resultSplitList = execute.split(splitSign);
				if(resultSplitList.length == 1) {
					inventoryRedisOpResultEnum = InventoryRedisOpResultEnum.getByOpAndCode(inventoryRedisOpEnum, execute);
					if(inventoryRedisOpResultEnum == null) {
						log.error("库存redis操作{} 未知的lua结果： {}" , opName , execute);
						throw new ServiceException(execute);
					}
					resultCode = inventoryRedisOpResultEnum.getCode();
					resultName = inventoryRedisOpResultEnum.getName();
				}else if(resultSplitList.length == 2) {
					resultCode = resultSplitList[0];
					resultName = resultSplitList[1];
				}else if(resultSplitList.length == 3) {
					resultCode = resultSplitList[0];
					resultName = resultSplitList[1];
					isRetry = Boolean.valueOf(resultSplitList[2]);
				}
			}
			if(isRetry) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}else {
				break;
			}
			i = i + 1;
		}
		if(!InventoryRedisOpResultEnum.SUCCESS.getCode().equals(resultCode)) {
			log.error("库存redis操作{}结果为：{}，lua原始结果： {}" , opName , resultName , execute);
			throw new ServiceException(resultName);
		}
	}
}
