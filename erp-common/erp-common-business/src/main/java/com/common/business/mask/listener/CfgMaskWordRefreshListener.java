package com.common.business.mask.listener;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.mask.cache.CfgMaskWordFullCacheDTO;
import com.common.business.mask.cache.CfgMaskWordLocalCache;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏词典 Redis Pub/Sub 监听器
 *
 * <p>结构与 {@link CfgMaskFieldRefreshListener} 一致；收到广播后调
 * {@link CfgMaskWordLocalCache#apply}，由本地缓存内部完成"差量同步到 SensitiveWordBs"。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class CfgMaskWordRefreshListener {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private CfgMaskWordLocalCache localCache;

    private volatile Integer listenerId;

    @PostConstruct
    public void subscribe() {
        RTopic topic = redissonClient.getTopic(RedisCacheConstants.MASK_WORD_CFG_REFRESH_CHANNEL);
        listenerId = topic.addListener(String.class, (channel, body) -> handle(body));
        log.info("CfgMaskWordRefreshListener subscribe channel={}, listenerId={}",
                RedisCacheConstants.MASK_WORD_CFG_REFRESH_CHANNEL, listenerId);
    }

    @PreDestroy
    public void unsubscribe() {
        if (listenerId == null) {
            return;
        }
        try {
            redissonClient.getTopic(RedisCacheConstants.MASK_WORD_CFG_REFRESH_CHANNEL)
                    .removeListener(listenerId);
            log.info("CfgMaskWordRefreshListener unsubscribe channel={}, listenerId={}",
                    RedisCacheConstants.MASK_WORD_CFG_REFRESH_CHANNEL, listenerId);
        } catch (Throwable e) {
            log.warn("CfgMaskWordRefreshListener unsubscribe failed", e);
        } finally {
            listenerId = null;
        }
    }

    private void handle(String body) {
        if (body == null || body.isEmpty()) {
            return;
        }
        try {
            CfgMaskWordFullCacheDTO payload = JSON.parseObject(body, CfgMaskWordFullCacheDTO.class);
            localCache.apply(payload);
        } catch (Throwable e) {
            log.warn("CfgMaskWordRefreshListener handle message failed, snapshot keeps unchanged", e);
        }
    }
}
