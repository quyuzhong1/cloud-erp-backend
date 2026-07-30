package com.erp.server.wms.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.erp.model.wms.enums.inventory.InventoryRedisOpKeyEnum;
import com.erp.server.wms.util.InventoryUnallocCheckHelper;

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

	/**
	 * 强制释放 Redisson 锁（无条件删除 key）；勿用于延迟补偿，误释其他事务新持有锁的风险见 {@link #safeUnlockByThreadId(String, long)}。
	 *
	 * @param lockKey Redis 锁 key
	 */
	public void forceUnlock(String lockKey) {
		if (StringUtils.isBlank(lockKey)) {
			return;
		}
		inventoryRedisson.getLock(lockKey).forceUnlock();
	}

	/**
	 * 按原持锁 threadId 释放单把锁（跨线程/XA/补偿 Job 使用）。
	 * 若锁已不存在，或已由其他线程持有，则视为无需再补偿并返回 true。
	 *
	 * @param lockKey  Redis 锁 key
	 * @param threadId 加锁时 {@link Thread#getId()}
	 * @return true 表示无需再重试；false 表示释放失败且可重试
	 */
	public boolean safeUnlockByThreadId(String lockKey, long threadId) {
		if (StringUtils.isBlank(lockKey)) {
			return true;
		}
		RLock lock = inventoryRedisson.getLock(lockKey);
		try {
			if (!lock.isLocked()) {
				return true;
			}
			// Redisson 3.10.x RLock 无 unlock(long)，跨线程释放须用 unlockAsync(threadId)
			lock.unlockAsync(threadId).get();
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("unlockByThreadId 被中断 lockKey={} threadId={}", lockKey, threadId, e);
			return false;
		} catch (Exception e) {
			Throwable cause = e.getCause() != null ? e.getCause() : e;
			if (cause instanceof IllegalMonitorStateException) {
				if (lock.isLocked()) {
					log.warn("未分配共享锁非原持锁线程，放弃补偿释放 lockKey={} threadId={}", lockKey, threadId);
				}
				return true;
			}
			log.warn("unlockByThreadId 失败 lockKey={} threadId={}", lockKey, threadId, e);
			return false;
		}
	}

	/**
	 * 批量按原持锁 threadId 释放 Redisson 锁。
	 *
	 * @param lockKeyToThreadId lock key 与加锁 threadId 映射
	 * @return 仍需补偿重试的 lock key 及 threadId
	 */
	public Map<String, Long> unlockByThreadId(Map<String, Long> lockKeyToThreadId) {
		if (lockKeyToThreadId == null || lockKeyToThreadId.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Long> failed = new LinkedHashMap<>();
		for (Map.Entry<String, Long> entry : lockKeyToThreadId.entrySet()) {
			if (!safeUnlockByThreadId(entry.getKey(), entry.getValue())) {
				failed.put(entry.getKey(), entry.getValue());
			}
		}
		return failed;
	}

	/**
	 * 批量强制释放 Redisson 锁。
	 *
	 * @param lockKeys Redis 锁 key 列表
	 * @return 释放失败的 lock key 列表
	 */
	public List<String> forceUnlock(List<String> lockKeys) {
		if (lockKeys == null || lockKeys.isEmpty()) {
			return new ArrayList<>();
		}
		List<String> failedKeys = new ArrayList<>();
		for (String lockKey : lockKeys) {
			try {
				forceUnlock(lockKey);
			} catch (Exception e) {
				log.warn("forceUnlock 失败 lockKey={}", lockKey, e);
				failedKeys.add(lockKey);
			}
		}
		return failedKeys;
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
				if (isLuaScriptRuntimeError(redisEx)) {
					throwLuaRuntimeException(opName, redisEx);
				}
				if (isWriteOpNoRetryOnUnknownResult(inventoryRedisOpEnum)) {
					log.error("库存redis写操作{}基础设施异常，执行结果未知，禁止重试", opName, redisEx);
					throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED, "Redis写操作异常，执行结果未知");
				}
				log.warn("库存redis操作{}基础设施异常，准备重试 attempt={}/3", opName, i + 1, redisEx);
				if (i >= 2) {
					throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED, "Redis操作重试失败");
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
	 * 约定：{@code biz_error} 以 {@link InventoryUnallocCheckHelper#UNALLOC_LUA_ERROR_PREFIX} 开头时映射
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
		if (InventoryUnallocCheckHelper.isUnallocLuaBusinessError(luaErr)) {
			throw new ServiceException(ApiError.VM_CHECK_OUT_VIRTUAL_INVENTORY.getCode(),
					InventoryUnallocCheckHelper.stripUnallocLuaErrorPrefix(luaErr));
		}
		if (InventoryUnallocCheckHelper.isInventoryLuaBusinessError(luaErr)) {
			throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED.getCode(),
					InventoryUnallocCheckHelper.stripInventoryLuaBusinessErrorPrefix(luaErr));
		}
		throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED,
				StringUtils.defaultIfBlank(luaErr, "Redis Lua业务失败"));
	}

	/**
	 * 解析 Lua 脚本运行时错误（非 {@code redis.error_reply} 业务失败）并抛出可读异常。
	 * 前端仅返回固定文案，脚本行号等详情写入 error 日志。
	 *
	 * @param opName  Redis 操作名
	 * @param redisEx Spring Redis 访问异常
	 */
	private void throwLuaRuntimeException(String opName, Exception redisEx) {
		String runtimeErr = resolveLuaRuntimeErrorMessage(redisEx);
		log.error("库存redis操作{} Lua脚本运行时失败：{}", opName, runtimeErr, redisEx);
		throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED, "Redis Lua脚本执行失败，请联系管理员");
	}

	/**
	 * 判断是否为 Lua 脚本运行时错误（如类型错误、拼接失败），区别于网络超时等基础设施异常。
	 *
	 * @param redisEx Spring Redis 访问异常
	 * @return true 表示 Lua EVAL 执行过程中脚本自身崩溃
	 */
	private static boolean isLuaScriptRuntimeError(Exception redisEx) {
		return StringUtils.isNotBlank(resolveLuaRuntimeErrorMessage(redisEx));
	}

	/**
	 * 从异常链提取 Lua 脚本运行时错误摘要（{@code user_script:} / {@code Error running script}）。
	 *
	 * @param ex 异常
	 * @return 可读错误摘要；无法识别则 null
	 */
	static String resolveLuaRuntimeErrorMessage(Throwable ex) {
		while (ex != null) {
			String msg = ex.getMessage();
			if (StringUtils.isNotBlank(msg)
					&& (msg.contains("user_script:") || msg.contains("Error running script"))) {
				int scriptIdx = msg.indexOf("user_script:");
				if (scriptIdx >= 0) {
					return msg.substring(scriptIdx).trim();
				}
				return msg.trim();
			}
			ex = ex.getCause();
		}
		return null;
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
		if (InventoryUnallocCheckHelper.isUnallocLuaBusinessError(raw)
				|| InventoryUnallocCheckHelper.isInventoryLuaBusinessError(raw)) {
			return raw;
		}
		if (raw.contains("ERR ")) {
			String body = raw.substring(raw.indexOf("ERR ") + 4);
			if (InventoryUnallocCheckHelper.isUnallocLuaBusinessError(body)
					|| InventoryUnallocCheckHelper.isInventoryLuaBusinessError(body)) {
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

	/**
	 * 解析 {@code inventory:current} Redis 值的首段基量（不含 TRY 在途段）。
	 *
	 * @param redisQtyObj Redis GET 返回值
	 * @return 基量；空或非法时返回 0
	 */
	public static int parseBaseCurrentQty(Object redisQtyObj) {
		if (redisQtyObj == null) {
			return 0;
		}
		String[] split = redisQtyObj.toString().split(splitSign);
		if (split.length == 0 || StringUtils.isBlank(split[0])) {
			return 0;
		}
		try {
			return Integer.parseInt(split[0].trim());
		} catch (NumberFormatException e) {
			log.warn("Redis current 基量解析失败 base={}", split[0]);
			return 0;
		}
	}

	/**
	 * 解析 {@code inventory:current} TRY 片段数量，兼容二段 {@code txn@@qty} 与三段 {@code txn@@operationId@@qty}。
	 * <p>非法片段 fail-closed，与 {@link InventoryUnallocCheckHelper#sumPendingReserve} 预占解析策略一致。</p>
	 *
	 * @param qtySplit 按 {@link #atSign} 拆分后的 TRY 片段
	 * @return TRY 数量（可为负，表示出库在途）
	 */
	public static int parseTrySegmentQty(String[] qtySplit) {
		if (qtySplit == null || qtySplit.length < 2) {
			return 0;
		}
		String qtyStr = qtySplit.length >= 3 ? qtySplit[2] : qtySplit[1];
		if (StringUtils.isBlank(qtyStr)) {
			log.warn("Redis current TRY 片段数量为空 segment={}", String.join(atSign, qtySplit));
			ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "Redis库存TRY片段数量为空");
		}
		try {
			return Integer.parseInt(qtyStr.trim());
		} catch (NumberFormatException e) {
			log.warn("Redis current TRY 片段数量解析失败 segment={}", String.join(atSign, qtySplit));
			ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "Redis库存TRY片段解析失败");
		}
		throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED, "Redis库存TRY片段解析失败");
	}

	/**
	 * 计算 {@code inventory:current} 含 TRY 在途后的可用量。
	 * <p>负 TRY 或当前事务 TRY 均计入；与 try.lua 写入的三段格式一致。</p>
	 *
	 * @param redisQtyObj   Redis GET 返回值
	 * @param transactionId 当前全局事务 ID 或 traceId，可为空
	 * @return 基量加在途 TRY 后的数量；key 不存在时返回 0
	 */
	public static int computeCurrentQtyWithTry(Object redisQtyObj, String transactionId) {
		if (redisQtyObj == null) {
			return 0;
		}
		String[] split = redisQtyObj.toString().split(splitSign);
		if (split.length == 0 || StringUtils.isBlank(split[0])) {
			return 0;
		}
		int redisQty;
		try {
			redisQty = Integer.parseInt(split[0].trim());
		} catch (NumberFormatException e) {
			log.warn("Redis current 基量解析失败 base={}", split[0]);
			return 0;
		}
		for (String segment : split) {
			String[] qtySplit = segment.split(atSign);
			if (qtySplit.length < 2) {
				continue;
			}
			int tryQty = parseTrySegmentQty(qtySplit);
			if (tryQty < 0 || (StringUtils.isNotBlank(transactionId) && qtySplit[0].equals(transactionId))) {
				redisQty += tryQty;
			}
		}
		return redisQty;
	}

	/**
	 * 批量 MGET {@code inventory:current} 基量，避免预检按 inventoryId 逐条 GET。
	 *
	 * @param inventoryIds 库存 ID 集合
	 * @return inventoryId → 基量
	 */
	public Map<String, Integer> batchGetBaseQtyByInventoryIds(Collection<String> inventoryIds) {
		if (inventoryIds == null || inventoryIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<String> distinctIds = inventoryIds.stream()
				.filter(StringUtils::isNotBlank)
				.distinct()
				.collect(Collectors.toList());
		if (distinctIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<String> redisKeys = distinctIds.stream()
				.map(id -> InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, id))
				.collect(Collectors.toList());
		List<Object> values = inventoryRedisTemplate.opsForValue().multiGet(redisKeys);
		Map<String, Integer> result = new HashMap<>(distinctIds.size());
		for (int i = 0; i < distinctIds.size(); i++) {
			Object val = values != null && i < values.size() ? values.get(i) : null;
			result.put(distinctIds.get(i), parseBaseCurrentQty(val));
		}
		return result;
	}

}
