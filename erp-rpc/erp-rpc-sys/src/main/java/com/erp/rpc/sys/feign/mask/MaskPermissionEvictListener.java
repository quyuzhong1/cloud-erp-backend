package com.erp.rpc.sys.feign.mask;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.mask.resolver.MaskPermissionEvictMessage;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏权限失效广播监听器（{@link RedisCacheConstants#MASK_PERM_EVICT_CHANNEL}）
 *
 * <p>沿用项目 {@code CfgMaskFieldRefreshListener / DorisQuerySettingRefreshListener} 同一模式：
 * 基于 Redisson {@link RTopic}，{@code @PostConstruct} 注册、{@code @PreDestroy} 解注册。</p>
 *
 * <h3>消息处理</h3>
 * <ul>
 *   <li>{@link MaskPermissionEvictMessage.Type#USER}：调 {@link FeignMaskPermissionResolver#evict(java.util.Collection)}</li>
 *   <li>{@link MaskPermissionEvictMessage.Type#ALL}：调 {@link FeignMaskPermissionResolver#evictAll()}</li>
 * </ul>
 *
 * <h3>设计</h3>
 * <p>Listener 独立 Bean，便于单测 / mock，并与 Resolver 解耦。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class MaskPermissionEvictListener {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private FeignMaskPermissionResolver resolver;

    private volatile Integer listenerId;

    @PostConstruct
    public void subscribe() {
        RTopic topic = redissonClient.getTopic(RedisCacheConstants.MASK_PERM_EVICT_CHANNEL);
        listenerId = topic.addListener(String.class, (channel, body) -> handle(body));
        log.info("MaskPermissionEvictListener subscribe channel={}, listenerId={}",
                RedisCacheConstants.MASK_PERM_EVICT_CHANNEL, listenerId);
    }

    @PreDestroy
    public void unsubscribe() {
        if (listenerId == null) {
            return;
        }
        try {
            redissonClient.getTopic(RedisCacheConstants.MASK_PERM_EVICT_CHANNEL)
                    .removeListener(listenerId);
            log.info("MaskPermissionEvictListener unsubscribe channel={}, listenerId={}",
                    RedisCacheConstants.MASK_PERM_EVICT_CHANNEL, listenerId);
        } catch (Throwable e) {
            log.warn("MaskPermissionEvictListener unsubscribe failed", e);
        } finally {
            listenerId = null;
        }
    }

    private void handle(String body) {
        if (body == null || body.isEmpty()) {
            return;
        }
        try {
            MaskPermissionEvictMessage msg = JSON.parseObject(body, MaskPermissionEvictMessage.class);
            if (msg == null || msg.getType() == null) {
                log.warn("MaskPermissionEvictListener got invalid msg: {}", body);
                return;
            }
            switch (msg.getType()) {
                case USER:
                    resolver.evict(msg.getUids());
                    log.info("MaskPermissionEvictListener evict users={}, source={}",
                            msg.getUids() == null ? 0 : msg.getUids().size(), msg.getSource());
                    break;
                case ALL:
                    resolver.evictAll();
                    log.info("MaskPermissionEvictListener evict ALL, source={}", msg.getSource());
                    break;
                default:
                    log.warn("MaskPermissionEvictListener unknown type={}", msg.getType());
            }
        } catch (Throwable e) {
            log.warn("MaskPermissionEvictListener handle msg failed, body={}", body, e);
        }
    }
}
