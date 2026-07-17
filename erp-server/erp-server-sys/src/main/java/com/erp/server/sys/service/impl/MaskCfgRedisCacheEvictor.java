package com.erp.server.sys.service.impl;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏配置 Redis 缓存延迟双删工具。
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class MaskCfgRedisCacheEvictor {

    private static final long DELAY_DELETE_MS = 1000L;

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mask-cfg-delay-delete");
        t.setDaemon(true);
        return t;
    });

    @Resource
    private RedissonClient redissonClient;

    public void doubleDelete(String key, String source) {
        deleteQuietly(key, source, "first");
        EXECUTOR.schedule(() -> deleteQuietly(key, source, "second"), DELAY_DELETE_MS, TimeUnit.MILLISECONDS);
    }

    private void deleteQuietly(String key, String source, String phase) {
        try {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            bucket.delete();
            log.info("Mask cfg redis cache {} delete ok, key={}, source={}", phase, key, source);
        } catch (Throwable e) {
            log.warn("Mask cfg redis cache {} delete failed, key={}, source={}, msg={}",
                    phase, key, source, e.getMessage());
        }
    }
}
