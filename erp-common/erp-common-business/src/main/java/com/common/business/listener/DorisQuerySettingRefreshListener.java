package com.common.business.listener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.common.business.cache.DorisQuerySettingLocalCache;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.DorisQuerySettingFullCacheDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * 订阅 DMP 广播的全量 Doris 路由配置，反序列化后交给 {@link DorisQuerySettingLocalCache} 原子替换。
 *
 * <p>基于 Redisson {@link RTopic} 自注册，与项目中 {@code NoticeClusterSubscriber} 同一套机制，避免再单独
 * 维护 Spring {@code RedisMessageListenerContainer}。{@code @PostConstruct} 注册 listener，
 * {@code @PreDestroy} 解除注册，避免应用关闭时野线程残留。</p>
 *
 * <p>仅当 {@code spring.datasource.dynamic.enabled=true} 时启用，与 {@link DorisQuerySettingLocalCache}
 * 条件保持一致，避免在未启用动态数据源的服务里因找不到 {@link DorisQuerySettingLocalCache} 而启动失败。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.datasource.dynamic.enabled", havingValue = "true")
public class DorisQuerySettingRefreshListener {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private DorisQuerySettingLocalCache localCache;

    private volatile Integer listenerId;

    @PostConstruct
    public void subscribe() {
        RTopic topic = redissonClient.getTopic(RedisCacheConstants.DORIS_QUERY_CFG_REFRESH_CHANNEL);
        listenerId = topic.addListener(String.class, (channel, body) -> handle(body));
        log.info("DorisQuerySettingRefreshListener subscribe channel={}, listenerId={}",
                RedisCacheConstants.DORIS_QUERY_CFG_REFRESH_CHANNEL, listenerId);
    }

    @PreDestroy
    public void unsubscribe() {
        if (listenerId == null) {
            return;
        }
        try {
            redissonClient.getTopic(RedisCacheConstants.DORIS_QUERY_CFG_REFRESH_CHANNEL)
                    .removeListener(listenerId);
            log.info("DorisQuerySettingRefreshListener unsubscribe channel={}, listenerId={}",
                    RedisCacheConstants.DORIS_QUERY_CFG_REFRESH_CHANNEL, listenerId);
        } catch (Throwable e) {
            log.warn("DorisQuerySettingRefreshListener unsubscribe failed", e);
        } finally {
            listenerId = null;
        }
    }

    private void handle(String body) {
        if (body == null || body.isEmpty()) {
            return;
        }
        try {
            DorisQuerySettingFullCacheDTO payload = JSON.parseObject(body, DorisQuerySettingFullCacheDTO.class);
            localCache.apply(payload);
        } catch (Throwable e) {
            log.warn("DorisQuerySettingRefreshListener handle message failed, snapshot keeps unchanged", e);
        }
    }
}
