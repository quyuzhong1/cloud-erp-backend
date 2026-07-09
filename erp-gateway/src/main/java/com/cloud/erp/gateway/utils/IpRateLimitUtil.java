package com.cloud.erp.gateway.utils;

import com.common.business.constant.RedisCacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * IP访问频率限制工具类
 * 基于Redisson滑动时间窗口的IP防护机制
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
@Component
public class IpRateLimitUtil {

    @Resource
    private RedissonClient redisson;

    @Value("${gateway.rate-limit.allow-unknown-ip:false}")
    private Boolean allowUnknownIp;

    private static final String UNKNOWN_IP = "unknown";

    /**
     * 原子滑动窗口限流脚本。
     * KEYS[1] 限流 ZSET，KEYS[2] 封禁 key；
     * ARGV[1] 当前毫秒时间，ARGV[2] 窗口起点，ARGV[3] 最大请求数，
     * ARGV[4] 限流 key 过期秒数，ARGV[5] 封禁秒数，ARGV[6] 本次请求唯一成员。
     */
    private static final String RATE_LIMIT_SCRIPT =
            "if redis.call('exists', KEYS[2]) == 1 then return 0 end " +
            "redis.call('zremrangebyscore', KEYS[1], 0, tonumber(ARGV[2])) " +
            "redis.call('zadd', KEYS[1], tonumber(ARGV[1]), ARGV[6]) " +
            "local count = redis.call('zcard', KEYS[1]) " +
            "redis.call('expire', KEYS[1], tonumber(ARGV[4])) " +
            "if count > tonumber(ARGV[3]) then " +
            "  redis.call('set', KEYS[2], 'blocked', 'EX', tonumber(ARGV[5])) " +
            "  return 0 " +
            "end " +
            "return 1";

    /**
     * 统计脚本也走 Redis 原子操作，避免查询时和清理窗口产生不一致读。
     */
    private static final String STATS_SCRIPT =
            "redis.call('zremrangebyscore', KEYS[1], 0, tonumber(ARGV[1])) " +
            "local count = redis.call('zcard', KEYS[1]) " +
            "local blocked = redis.call('exists', KEYS[2]) " +
            "return {count, blocked}";

    /**
     * 默认配置
     */
    private static final int DEFAULT_MAX_REQUESTS = 100; // 默认最大请求次数
    private static final int DEFAULT_TIME_WINDOW = 60;   // 默认时间窗口（秒）
    private static final int DEFAULT_BLOCK_TIME = 300;   // 默认封禁时间（秒）

    /**
     * API Token 入口的全局兜底保护，防止多 IP 分散流量绕过单 IP 限流后压垮 sys Feign。
     */
    private static final int API_TOKEN_GLOBAL_MAX_REQUESTS = 3000;

    /**
     * API Token 按路径限流，避免单个白名单接口或不存在路径被集中打爆。
     */
    private static final int API_TOKEN_PATH_MAX_REQUESTS = 1000;

    /**
     * 同一 tokenHash 连续失败达到阈值后临时封禁，拦截固定 token 的重复探测。
     */
    private static final int API_TOKEN_FAILURE_MAX_REQUESTS = 10;

    private static final int API_TOKEN_RATE_LIMIT_WINDOW_SECONDS = 60;

    private static final String API_TOKEN_GLOBAL_RATE_DIMENSION = "api:token:global";

    private static final String API_TOKEN_PATH_RATE_DIMENSION_PREFIX = "api:token:path:";

    private static final String API_TOKEN_FAILURE_RATE_DIMENSION_PREFIX = "api:token:failure:";

    private static final String API_TOKEN_GLOBAL_RATE_LOG_NAME = "API_TOKEN_GLOBAL_RATE";

    private static final String API_TOKEN_PATH_RATE_LOG_NAME = "API_TOKEN_PATH_RATE";

    private static final String API_TOKEN_FAILURE_RATE_LOG_NAME = "API_TOKEN_FAILURE_RATE";

    /**
     * 检查IP是否允许访问
     *
     * @param ipAddress IP地址
     * @return 是否允许访问
     */
    public boolean isAllowed(String ipAddress) {
        return isAllowed(ipAddress, DEFAULT_MAX_REQUESTS, DEFAULT_TIME_WINDOW);
    }

    /**
     * 检查IP是否允许访问（自定义配置）
     * 使用Redisson滑动窗口实现
     *
     * @param ipAddress   IP地址
     * @param maxRequests 最大请求次数
     * @param timeWindow  时间窗口（秒）
     * @return 是否允许访问
     */
    public boolean isAllowed(String ipAddress, int maxRequests, int timeWindow) {
        return isAllowedByKey(ipAddress, maxRequests, timeWindow);
    }

    public boolean isAllowedByKey(String keyPart, int maxRequests, int timeWindow) {
        return isAllowedByKey(keyPart, maxRequests, timeWindow, keyPart);
    }

    private boolean isAllowedByKey(String keyPart, int maxRequests, int timeWindow, String logName) {
        if (keyPart == null || keyPart.trim().isEmpty()) {
            return false;
        }
        if (UNKNOWN_IP.equalsIgnoreCase(keyPart) && !Boolean.TRUE.equals(allowUnknownIp)) {
            return false;
        }
        return isAllowedByRedisKey(buildRateLimitKey(keyPart), buildBlockKey(keyPart), maxRequests, timeWindow, logName);
    }

    private boolean isAllowedByRedisKey(String key, String blockKey, int maxRequests, int timeWindow, String logName) {
        try {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - (timeWindow * 1000L);
            // 成员值加随机后缀，避免同毫秒多个请求覆盖同一个 ZSET member。
            String member = currentTime + "-" + ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);

            Number allowed = redisson.getScript(StringCodec.INSTANCE).eval(
                    RScript.Mode.READ_WRITE,
                    RATE_LIMIT_SCRIPT,
                    RScript.ReturnType.INTEGER,
                    Arrays.<Object>asList(key, blockKey),
                    String.valueOf(currentTime),
                    String.valueOf(windowStart),
                    String.valueOf(maxRequests),
                    String.valueOf(timeWindow + 10),
                    String.valueOf(DEFAULT_BLOCK_TIME),
                    member);
            boolean pass = allowed != null && allowed.longValue() == 1L;
            if (!pass) {
                log.warn("访问对象 {} 访问频率过高或已被封禁", logName);
            }
            return pass;
        } catch (Exception e) {
            log.error("检查访问频率失败: {}", logName, e);
            // 入口限流组件采用安全优先的 fail-close：Redis/Lua 异常时拒绝开放接口/API Token 请求，
            // 避免故障期间绕过防护；上线需配套 Redis 可用性监控，必要时再改成配置化策略。
            return false;
        }
    }

    /**
     * 检查IP是否允许访问（针对开放API的严格限制）
     *
     * @param ipAddress IP地址
     * @return 是否允许访问
     */
    public boolean isOpenApiAllowed(String ipAddress) {
        // 开放API使用更严格的限制
        return isAllowed(ipAddress, 100, 60); // 1分钟内最多100次请求
    }

    /**
     * 检查IP是否允许访问（针对普通接口的限制）
     *
     * @param ipAddress IP地址
     * @return 是否允许访问
     */
    public boolean isNormalApiAllowed(String ipAddress) {
        // 普通接口使用标准限制
        return isAllowed(ipAddress, 200, 60); // 1分钟内最多200次请求
    }

    public boolean isApiTokenGlobalAllowed() {
        String slotTag = buildSlotTag(API_TOKEN_GLOBAL_RATE_DIMENSION);
        return isAllowedByRedisKey(formatRedisKey(RedisCacheConstants.GATEWAY_API_TOKEN_RATE_GLOBAL, slotTag),
                formatRedisKey(RedisCacheConstants.GATEWAY_RATE_LIMIT_BLOCK, slotTag),
                API_TOKEN_GLOBAL_MAX_REQUESTS,
                API_TOKEN_RATE_LIMIT_WINDOW_SECONDS,
                API_TOKEN_GLOBAL_RATE_LOG_NAME);
    }

    public boolean isApiTokenPathAllowed(String pathHash) {
        String slotTag = buildSlotTag(API_TOKEN_PATH_RATE_DIMENSION_PREFIX + pathHash);
        return isAllowedByRedisKey(formatRedisKey(RedisCacheConstants.GATEWAY_API_TOKEN_RATE_PATH, slotTag),
                formatRedisKey(RedisCacheConstants.GATEWAY_RATE_LIMIT_BLOCK, slotTag),
                API_TOKEN_PATH_MAX_REQUESTS,
                API_TOKEN_RATE_LIMIT_WINDOW_SECONDS,
                API_TOKEN_PATH_RATE_LOG_NAME);
    }

    public boolean isApiTokenFailureBlocked(String tokenHash) {
        String slotTag = buildSlotTag(API_TOKEN_FAILURE_RATE_DIMENSION_PREFIX + tokenHash);
        return isBlockedByRedisKey(formatRedisKey(RedisCacheConstants.GATEWAY_RATE_LIMIT_BLOCK, slotTag), API_TOKEN_FAILURE_RATE_LOG_NAME);
    }

    public boolean recordApiTokenFailure(String tokenHash) {
        String slotTag = buildSlotTag(API_TOKEN_FAILURE_RATE_DIMENSION_PREFIX + tokenHash);
        return isAllowedByRedisKey(formatRedisKey(RedisCacheConstants.GATEWAY_API_TOKEN_RATE_FAILURE, slotTag),
                formatRedisKey(RedisCacheConstants.GATEWAY_RATE_LIMIT_BLOCK, slotTag),
                API_TOKEN_FAILURE_MAX_REQUESTS,
                API_TOKEN_RATE_LIMIT_WINDOW_SECONDS,
                API_TOKEN_FAILURE_RATE_LOG_NAME);
    }

    /**
     * 检查IP是否被封禁
     *
     * @param ipAddress IP地址
     * @return 是否被封禁
     */
    private boolean isBlocked(String ipAddress) {
        return isBlockedByKey(ipAddress);
    }

    private boolean isBlockedByKey(String keyPart) {
        return isBlockedByRedisKey(buildBlockKey(keyPart), keyPart);
    }

    private boolean isBlockedByRedisKey(String blockKey, String logName) {
        try {
            return redisson.getBucket(blockKey).isExists();
        } catch (Exception e) {
            log.error("检查访问对象封禁状态失败: {}", logName, e);
            // 与限流计数保持一致的安全优先策略：封禁状态不可判定时按已封禁处理。
            return true;
        }
    }

    /**
     * 手动封禁IP
     *
     * @param ipAddress  IP地址
     * @param blockTime  封禁时间（秒）
     */
    public void blockIp(String ipAddress, int blockTime) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return;
        }

        try {
            String blockKey = buildBlockKey(ipAddress);
            redisson.getBucket(blockKey).set("blocked", blockTime, TimeUnit.SECONDS);
            log.warn("手动封禁IP {}，封禁时间: {} 秒", ipAddress, blockTime);
        } catch (Exception e) {
            log.error("封禁IP失败: {}", ipAddress, e);
        }
    }

    /**
     * 解除IP封禁
     *
     * @param ipAddress IP地址
     */
    public void unblockIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return;
        }

        try {
            String blockKey = buildBlockKey(ipAddress);
            redisson.getBucket(blockKey).delete();
            log.info("解除IP {} 的封禁", ipAddress);
        } catch (Exception e) {
            log.error("解除IP封禁失败: {}", ipAddress, e);
        }
    }

    /**
     * 获取IP访问统计信息
     *
     * @param ipAddress IP地址
     * @return 访问统计信息
     */
    public String getIpStats(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ipAddress)) {
            return "IP地址为空";
        }

        try {
            String key = buildRateLimitKey(ipAddress);
            String blockKey = buildBlockKey(ipAddress);
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - (60 * 1000L); // 1分钟窗口

            List<Object> stats = redisson.getScript(StringCodec.INSTANCE).eval(
                    RScript.Mode.READ_WRITE,
                    STATS_SCRIPT,
                    RScript.ReturnType.MULTI,
                    Arrays.<Object>asList(key, blockKey),
                    String.valueOf(windowStart));
            long currentCount = stats == null || stats.isEmpty() ? 0L : ((Number) stats.get(0)).longValue();
            boolean isBlocked = stats != null && stats.size() > 1 && ((Number) stats.get(1)).longValue() == 1L;
            
            return String.format("IP %s 当前请求次数: %d, 是否被封禁: %s",
                    ipAddress, currentCount, isBlocked ? "是" : "否");
        } catch (Exception e) {
            log.error("获取IP统计信息失败: {}", ipAddress, e);
            return "获取统计信息失败: " + e.getMessage();
        }
    }

    /**
     * 清理过期的IP记录（Redis会自动清理，此方法保留用于手动清理）
     */
    public void cleanExpiredRecords() {
        try {
            // Redis会自动清理过期的key，这里可以添加额外的清理逻辑
            log.info("Redis自动清理过期记录，无需手动清理");
        } catch (Exception e) {
            log.error("清理过期记录失败", e);
        }
    }

    private String buildRateLimitKey(String ipAddress) {
        // Redis Cluster 下两个 key 必须使用相同 hash tag，否则 Lua 多 key 操作会 CROSSSLOT。
        return formatRedisKey(RedisCacheConstants.GATEWAY_IP_RATE_LIMIT, buildSlotTag(ipAddress));
    }

    private String buildBlockKey(String ipAddress) {
        // 与限流 key 共用 hash tag，保证限流和封禁状态在同一 slot 内原子处理。
        return formatRedisKey(RedisCacheConstants.GATEWAY_RATE_LIMIT_BLOCK, buildSlotTag(ipAddress));
    }

    private String buildSlotTag(String value) {
        return "{" + normalizeKeyPart(value) + "}";
    }

    private String formatRedisKey(String template, String value) {
        return template.replace("{}", value);
    }

    private String normalizeKeyPart(String value) {
        return value.trim().replace("{", "").replace("}", "");
    }
}
