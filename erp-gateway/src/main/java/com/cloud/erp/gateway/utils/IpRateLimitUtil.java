package com.cloud.erp.gateway.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    /**
     * Redis Key前缀
     */
    private static final String IP_RATE_LIMIT_PREFIX = "ip:rate:limit:";
    private static final String IP_BLOCK_PREFIX = "ip:block:";

    /**
     * 默认配置
     */
    private static final int DEFAULT_MAX_REQUESTS = 100; // 默认最大请求次数
    private static final int DEFAULT_TIME_WINDOW = 60;   // 默认时间窗口（秒）
    private static final int DEFAULT_BLOCK_TIME = 300;   // 默认封禁时间（秒）

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
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }

        try {
            // 检查是否被封禁
            if (isBlocked(ipAddress)) {
                log.warn("IP {} 被临时封禁", ipAddress);
                return false;
            }

            // 使用滑动窗口检查访问频率
            String key = IP_RATE_LIMIT_PREFIX + ipAddress;
            RMap<String, Integer> rateMap = redisson.getMap(key);
            
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - (timeWindow * 1000L);
            
            // 清理过期记录
            rateMap.entrySet().removeIf(entry -> {
                long timestamp = Long.parseLong(entry.getKey());
                return timestamp < windowStart;
            });
            
            // 检查当前窗口内的请求次数
            int currentCount = rateMap.size();
            if (currentCount >= maxRequests) {
                // 超过限制，封禁IP
                blockIp(ipAddress, DEFAULT_BLOCK_TIME);
                log.warn("IP {} 访问频率过高，已封禁 {} 秒", ipAddress, DEFAULT_BLOCK_TIME);
                return false;
            }
            
            // 记录本次访问
            rateMap.put(String.valueOf(currentTime), 1);
            rateMap.expire(timeWindow + 10, TimeUnit.SECONDS); // 设置过期时间
            
            return true;
        } catch (Exception e) {
            log.error("检查IP访问频率失败: {}", ipAddress, e);
            return true; // 异常时允许访问，避免影响正常业务
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

    /**
     * 检查IP是否被封禁
     *
     * @param ipAddress IP地址
     * @return 是否被封禁
     */
    private boolean isBlocked(String ipAddress) {
        try {
            String blockKey = IP_BLOCK_PREFIX + ipAddress;
            return redisson.getBucket(blockKey).isExists();
        } catch (Exception e) {
            log.error("检查IP封禁状态失败: {}", ipAddress, e);
            return false;
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
            String blockKey = IP_BLOCK_PREFIX + ipAddress;
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
            String blockKey = IP_BLOCK_PREFIX + ipAddress;
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
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return "IP地址为空";
        }

        try {
            String key = IP_RATE_LIMIT_PREFIX + ipAddress;
            RMap<String, Integer> rateMap = redisson.getMap(key);
            
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - (60 * 1000L); // 1分钟窗口
            
            // 清理过期记录
            rateMap.entrySet().removeIf(entry -> {
                long timestamp = Long.parseLong(entry.getKey());
                return timestamp < windowStart;
            });
            
            int currentCount = rateMap.size();
            boolean isBlocked = isBlocked(ipAddress);
            
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
}
