package com.erp.server.wms.utils;

import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 盘点库存 Redis 锁：Lua 内按 pattern SCAN 冲突检测后对 lockKey 执行 SET NX。
 */
@Component
@Slf4j
public class StocktakingInventoryLockRedisUtil {

    private static final StringRedisSerializer STRING_REDIS_SERIALIZER = StringRedisSerializer.UTF_8;

    private static final DefaultRedisScript<Long> TRY_STOCKTAKING_INVENTORY_LOCK_SCRIPT;

    /**
     * ARGV[1]=conflictPattern，ARGV[2]=lockKey，ARGV[3]=lockValue（planCode）。
     * 调用方须传入尽量收窄的 pattern；MATCH 仅过滤 SCAN 返回，未命中时仍可能遍历 keyspace。
     */
    static {
        TRY_STOCKTAKING_INVENTORY_LOCK_SCRIPT = new DefaultRedisScript<>();
        TRY_STOCKTAKING_INVENTORY_LOCK_SCRIPT.setScriptText(
                "local pattern = ARGV[1]\n"
                        + "local lockKey = ARGV[2]\n"
                        + "local lockValue = ARGV[3]\n"
                        + "local existing = redis.call('GET', lockKey)\n"
                        + "if existing == lockValue then\n"
                        + "  return 1\n"
                        + "end\n"
                        + "if existing then\n"
                        + "  return 0\n"
                        + "end\n"
                        + "local cursor = '0'\n"
                        + "repeat\n"
                        + "  local result = redis.call('SCAN', cursor, 'MATCH', pattern, 'COUNT', 500)\n"
                        + "  cursor = result[1]\n"
                        + "  for _, key in ipairs(result[2]) do\n"
                        + "    if key ~= lockKey then\n"
                        + "      return 0\n"
                        + "    end\n"
                        + "  end\n"
                        + "until cursor == '0'\n"
                        + "if redis.call('SET', lockKey, lockValue, 'NX') then\n"
                        + "  return 1\n"
                        + "end\n"
                        + "return 0");
        TRY_STOCKTAKING_INVENTORY_LOCK_SCRIPT.setResultType(Long.class);
    }

    @Resource
    private RedisUtil redisUtil;

    /**
     * 盘点库存加锁：pattern 匹配到其它 lockKey 时返回 false；lockKey 已存在且 value 与 lockValue 相同视为成功。
     *
     * @param conflictPattern 单库存维度冲突检测 pattern，见 {@link com.common.business.constant.RedisCacheConstants#INVENTORY_LOCK}
     * @param lockKey         待写入的 lock key
     * @param lockValue       lock value（planCode）
     */
    public boolean tryStocktakingInventoryLock(String conflictPattern, String lockKey, String lockValue) {
        try {
            // Lua ARGV 须用 StringRedisSerializer，否则 FastJson valueSerializer 会把 key/value 序列化成带引号 JSON
            Long result = (Long) redisUtil.getRedisTemplate().execute(TRY_STOCKTAKING_INVENTORY_LOCK_SCRIPT,
                    STRING_REDIS_SERIALIZER, STRING_REDIS_SERIALIZER,
                    Collections.emptyList(), conflictPattern, lockKey, lockValue);
            if (result == null) {
                throw new ServiceException(ApiError.COMMON_REMOTE_SERVICE_ERROR, "Redis", "盘点加锁脚本返回为空");
            }
            return result == 1L;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("redis tryStocktakingInventoryLock error, pattern={}, lockKey={}", conflictPattern, lockKey, e);
            throw new ServiceException(ApiError.COMMON_REMOTE_SERVICE_ERROR, "Redis", "盘点加锁失败");
        }
    }
}
