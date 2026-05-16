package com.common.business.mask.listener;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.mask.cache.CfgMaskFieldFullCacheDTO;
import com.common.business.mask.cache.CfgMaskFieldLocalCache;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段脱敏配置 Redis Pub/Sub 监听器
 *
 * <p>沿用项目里 {@link com.common.business.listener.DorisQuerySettingRefreshListener} 同一套机制：
 * 基于 Redisson {@link RTopic} 自注册，{@code @PostConstruct} 注册、{@code @PreDestroy} 解注册。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class CfgMaskFieldRefreshListener {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private CfgMaskFieldLocalCache localCache;

    private volatile Integer listenerId;

    @PostConstruct
    public void subscribe() {
        RTopic topic = redissonClient.getTopic(RedisCacheConstants.MASK_FIELD_CFG_REFRESH_CHANNEL);
        listenerId = topic.addListener(String.class, (channel, body) -> handle(body));
        log.info("CfgMaskFieldRefreshListener subscribe channel={}, listenerId={}",
                RedisCacheConstants.MASK_FIELD_CFG_REFRESH_CHANNEL, listenerId);
    }

    @PreDestroy
    public void unsubscribe() {
        if (listenerId == null) {
            return;
        }
        try {
            redissonClient.getTopic(RedisCacheConstants.MASK_FIELD_CFG_REFRESH_CHANNEL)
                    .removeListener(listenerId);
            log.info("CfgMaskFieldRefreshListener unsubscribe channel={}, listenerId={}",
                    RedisCacheConstants.MASK_FIELD_CFG_REFRESH_CHANNEL, listenerId);
        } catch (Throwable e) {
            log.warn("CfgMaskFieldRefreshListener unsubscribe failed", e);
        } finally {
            listenerId = null;
        }
    }

    private void handle(String body) {
        if (body == null || body.isEmpty()) {
            return;
        }
        try {
            CfgMaskFieldFullCacheDTO payload = JSON.parseObject(body, CfgMaskFieldFullCacheDTO.class);
            localCache.apply(payload);
        } catch (Throwable e) {
            log.warn("CfgMaskFieldRefreshListener handle message failed, snapshot keeps unchanged", e);
        }
    }
}
