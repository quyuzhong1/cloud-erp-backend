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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import com.common.business.utils.AbstractRedisUtil;
import com.common.business.utils.StringUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.enums.inventory.InventoryRedisOpEnum;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InventoryRedisUtil extends AbstractRedisUtil{
	
	public static String splitSign = "&&";
	public static String atSign = "@@";

	private static RedisSerializer stringRedisSerializer = new StringRedisSerializer();
	
	@Qualifier("inventoryRedisson")
	@Autowired
    private RedissonClient inventoryRedisson;
	
    @Resource
    private RedisTemplate inventoryRedisTemplate;

	@Override
	public RedisTemplate getRedisTemplate() {
		return inventoryRedisTemplate;
	}
	
	public RedissonMultiLock tryLock(String key) {
		return this.tryLock(Arrays.asList(key));
	}
	
	public RedissonMultiLock tryLock(String key , long waitTime) {
		return this.tryLock(Arrays.asList(key) , waitTime);
	}
	
	public RedissonMultiLock tryLock(List<String> keys) {
		return this.tryLock(keys, 30);
	}
	
	public RedissonMultiLock tryLock(List<String> keys , long waitTime) {
		List<RLock> rLocks = keys.stream()
                .map(inventoryRedisson::getLock)
                .collect(Collectors.toList());
		RedissonMultiLock multiLock = new RedissonMultiLock(rLocks.toArray(new RLock[0]));
		boolean locked = false;
    	try {
			locked = multiLock.tryLock(waitTime, TimeUnit.SECONDS);
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
		String opName = inventoryRedisOpEnum.getName();
		String logMsg = StringUtil.appendLogMsg("InventoryRedisUtil的execute操作：" + opName , args);
    	log.info("{}开始" , logMsg);
		int i = 0;
		String result = "";
		boolean isRetry = false;
		String execute = "";
		while(i < 3) {
			execute = (String) inventoryRedisTemplate.execute(InventoryRedisOpEnum.getDefaultRedisScript(inventoryRedisOpEnum), stringRedisSerializer, stringRedisSerializer, Arrays.asList(), args);
			log.info("库存redis操作{}，入参{}，结果{}" , opName , args ,execute);
			long sleepTime = 1000L;
			if(StringUtils.isNotBlank(execute)) {
				String[] resultSplitList = execute.split(splitSign);
				if(resultSplitList.length == 1) {
					result = execute;
				}else if(resultSplitList.length == 2) {
					isRetry = true;
					result = resultSplitList[0];
					try {
						sleepTime = Long.parseLong(resultSplitList[1]);
					} catch (NumberFormatException e) {
						log.error("库存redis操作{}，转换重试时间失败{}" , opName , execute);
					}
				}
			}
			if(isRetry) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}else {
				break;
			}
			i = i + 1;
		}
		if(!"0".equals(result)) {
			log.error("库存redis操作{}，结果为：{}，lua原始结果： {}" , opName , result , execute);
			throw new ServiceException(result);
		}
    	log.info("{}结束" , logMsg);
	}
	
}
