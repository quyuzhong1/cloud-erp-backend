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
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.AbstractRedisUtil;
import com.common.business.utils.StringUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.enums.inventory.InventoryRedisOpEnum;
import com.erp.server.wms.inventory.VirtualInventoryUnallocCheckHelper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InventoryRedisUtil extends AbstractRedisUtil{
	
	public static String splitSign = "&&";
	public static String atSign = "@@";

	/** 仓位库存不足错误模板：当前库存占位符 */
	public static final String LOCATION_ERROR_CURRENT_PLACEHOLDER = "ss1ss";

	/** 仓位库存不足错误模板：缺少数占位符（与未分配错误的 ssvss 区分） */
	public static final String LOCATION_ERROR_SHORTAGE_PLACEHOLDER = "sslss";

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
		// try.lua 存在两种失败通道：① redis.error_reply -> RedisSystemException（未分配等业务失败，不重试）；
		// ② JSON {success:false,sleep}（仓位库存不足/重算中，可重试）。排查时需区分异常类型与 lua 原始返回值。
		int i = 0;
		boolean success = false;
		String errormsg = "";
		boolean isRetry = false;
		String execute = "";
		while(i < 3) {
			try {
				execute = (String) inventoryRedisTemplate.execute(InventoryRedisOpEnum.getDefaultRedisScript(inventoryRedisOpEnum), stringRedisSerializer, stringRedisSerializer, Arrays.asList(), args);
			} catch (DataAccessException redisEx) {
				if (isLuaScriptBusinessError(redisEx)) {
					throwLuaBusinessException(opName, redisEx);
				}
				if (isWriteOpNoRetryOnUnknownResult(inventoryRedisOpEnum)) {
					log.error("库存redis写操作{}基础设施异常，执行结果未知，禁止重试", opName, redisEx);
					throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED);
				}
				log.warn("库存redis操作{}基础设施异常，准备重试 attempt={}/3", opName, i + 1, redisEx);
				if (i >= 2) {
					throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED);
				}
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				i++;
				continue;
			}
			log.warn("库存redis操作{}，入参{}，lua结果：{}" , opName , args ,execute);
			long sleep = 0L;
			if(StringUtils.isNotBlank(execute)) {
				JSONObject parseObject = JSON.parseObject(execute);
				Boolean successVal = parseObject.getBoolean("success");
				if(successVal != null) {
					success = successVal.booleanValue();
				}
				Long sleepVal = parseObject.getLong("sleep");
				if(sleepVal != null) {
					isRetry = true;
					sleep = sleepVal.longValue();
				}else {
					isRetry = false;
				}
				errormsg = parseObject.getString("errormsg");
			}
			if(isRetry) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}else {
				break;
			}
			i = i + 1;
		}
		if(!success) {
			log.error("库存redis操作{}，结果为：{}，lua原始结果： {}" , opName , errormsg , execute);
			throw new ServiceException(errormsg);
		}
    	log.info("{}结束" , logMsg);
	}

	/**
	 * 写脚本（TRY/COMMIT/ROLLBACK）在 Redis 已执行但客户端结果未知时禁止重试，避免重复扣减。
	 *
	 * @param inventoryRedisOpEnum 库存 Redis 操作类型
	 * @return true 表示基础设施异常时不重试
	 */
	private static boolean isWriteOpNoRetryOnUnknownResult(InventoryRedisOpEnum inventoryRedisOpEnum) {
		return inventoryRedisOpEnum == InventoryRedisOpEnum.TRY
				|| inventoryRedisOpEnum == InventoryRedisOpEnum.COMMIT
				|| inventoryRedisOpEnum == InventoryRedisOpEnum.ROLLBACK;
	}

	/**
	 * 解析 Lua {@code redis.error_reply} 并转为业务异常。
	 * <p>
	 * 约定：{@code biz_error} 以 {@link VirtualInventoryUnallocCheckHelper#UNALLOC_LUA_ERROR_PREFIX} 开头时映射
	 * {@link ApiError#VM_CHECK_OUT_VIRTUAL_INVENTORY}；其它 {@code biz_error} 暂用默认 code，扩展时按前缀增映射。
	 * 仓位库存不足等可重试场景仍走 JSON {@code {success:false}}，不经本方法。
	 * </p>
	 *
	 * @param opName  Redis 操作名
	 * @param redisEx Spring Redis 访问异常
	 */
	private void throwLuaBusinessException(String opName, Exception redisEx) {
		String luaErr = extractLuaErrorMessage(redisEx);
		log.error("库存redis操作{} Lua业务失败：{}", opName, luaErr, redisEx);
		if (VirtualInventoryUnallocCheckHelper.isUnallocLuaBusinessError(luaErr)) {
			throw new ServiceException(ApiError.VM_CHECK_OUT_VIRTUAL_INVENTORY.getCode(),
					VirtualInventoryUnallocCheckHelper.stripUnallocLuaErrorPrefix(luaErr));
		}
		if (VirtualInventoryUnallocCheckHelper.isInventoryLuaBusinessError(luaErr)) {
			throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED.getCode(),
					VirtualInventoryUnallocCheckHelper.stripInventoryLuaBusinessErrorPrefix(luaErr));
		}
		throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED);
	}

	/**
	 * 判断 Redis 异常是否来自 Lua {@code redis.error_reply}（业务失败，不可重试）。
	 * 仅识别带约定前缀的 Lua 业务错误，避免将 WRONGTYPE 等基础设施异常误判为业务失败。
	 *
	 * @param redisEx Spring Redis 访问异常
	 * @return {@code true} 表示 Lua 脚本主动返回的业务错误
	 */
	private static boolean isLuaScriptBusinessError(Exception redisEx) {
		return StringUtils.isNotBlank(resolveLuaBusinessErrorMessage(redisEx));
	}

	/**
	 * 从单条异常消息中解析 Lua 业务错误正文（支持直连前缀或 {@code ERR } 前缀）。
	 *
	 * @param raw 异常 message
	 * @return 约定前缀业务正文；无法识别则 null
	 */
	static String resolveLuaBusinessErrorBody(String raw) {
		if (StringUtils.isBlank(raw)) {
			return null;
		}
		if (VirtualInventoryUnallocCheckHelper.isUnallocLuaBusinessError(raw)
				|| VirtualInventoryUnallocCheckHelper.isInventoryLuaBusinessError(raw)) {
			return raw;
		}
		if (raw.contains("ERR ")) {
			String body = raw.substring(raw.indexOf("ERR ") + 4);
			if (VirtualInventoryUnallocCheckHelper.isUnallocLuaBusinessError(body)
					|| VirtualInventoryUnallocCheckHelper.isInventoryLuaBusinessError(body)) {
				return body;
			}
		}
		return null;
	}

	/**
	 * 从异常链中提取 Lua 业务错误正文（含 {@code ERR } 前缀或直连约定前缀）。
	 *
	 * @param ex Spring/Redis 异常
	 * @return 业务错误正文；非约定 Lua 业务错误则返回 null
	 */
	static String resolveLuaBusinessErrorMessage(Throwable ex) {
		while (ex != null) {
			String body = resolveLuaBusinessErrorBody(ex.getMessage());
			if (StringUtils.isNotBlank(body)) {
				return body;
			}
			ex = ex.getCause();
		}
		return null;
	}

	/**
	 * 从 Spring Redis 异常消息中提取 Lua {@code error_reply} 正文。
	 *
	 * @param redisEx Spring Redis 访问异常
	 * @return 约定 Lua 业务错误正文；无法识别时返回 null
	 */
	private static String extractLuaErrorMessage(Exception redisEx) {
		return resolveLuaBusinessErrorMessage(redisEx);
	}

}
