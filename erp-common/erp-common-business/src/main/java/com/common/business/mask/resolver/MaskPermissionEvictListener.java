package com.common.business.mask.resolver;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.mask.MaskPermissionResolver;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏权限失效广播监听器（{@link RedisCacheConstants#MASK_PERM_EVICT_CHANNEL}）
 *
 * <p>本类位于 {@code erp-common-business}，与 SPI 接口 {@link MaskPermissionResolver}
 * 一同构成框架级失效闭环：所有引入 erp-common-business 的业务服务自动获得这个 listener，
 * 无需各服务额外配置。</p>
 *
 * <h3>设计要点</h3>
 * <ul>
 *   <li><b>注入 SPI 接口而非具体实现</b>：避免耦合到 {@code FeignMaskPermissionResolver}
 *       这样的具体实现。listener 只关心"收到 evict 消息要让 resolver 清缓存"，
 *       具体清的是本地 Caffeine / Redis / no-op，对 listener 透明。</li>
 *   <li><b>默认实现无害</b>：业务侧未引入 erp-rpc-sys 时，注入的是
 *       {@code LoginUserMaskPermissionResolver}，{@link MaskPermissionResolver#evict} 默认
 *       no-op，listener 收到广播也不会出错。</li>
 *   <li><b>沿用项目 Pub/Sub 模式</b>：与 {@code CfgMaskFieldRefreshListener /
 *       DorisQuerySettingRefreshListener} 完全同构，基于 Redisson {@link RTopic}，
 *       {@code @PostConstruct} 注册、{@code @PreDestroy} 解注册。</li>
 *   <li><b>{@code required=false} 的 RedissonClient</b>：极端环境（如无 Redis 的本地
 *       单元测试 / 离线工具进程）下不强制要求 Redis，listener 安静禁用。</li>
 * </ul>
 *
 * <h3>消息处理</h3>
 * <ul>
 *   <li>{@link MaskPermissionEvictMessage.Type#USER}：调 {@link MaskPermissionResolver#evict}</li>
 *   <li>{@link MaskPermissionEvictMessage.Type#ALL}：调 {@link MaskPermissionResolver#evictAll}</li>
 * </ul>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class MaskPermissionEvictListener {

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Resource
    private MaskPermissionResolver resolver;

    private volatile Integer listenerId;

    @PostConstruct
    public void subscribe() {
        if (redissonClient == null) {
            log.info("MaskPermissionEvictListener disabled: RedissonClient not available");
            return;
        }
        try {
            RTopic topic = redissonClient.getTopic(RedisCacheConstants.MASK_PERM_EVICT_CHANNEL);
            listenerId = topic.addListener(String.class, (channel, body) -> handle(body));
            log.info("MaskPermissionEvictListener subscribe channel={}, listenerId={}, resolver={}",
                    RedisCacheConstants.MASK_PERM_EVICT_CHANNEL, listenerId,
                    resolver == null ? null : resolver.getClass().getSimpleName());
        } catch (Throwable e) {
            log.warn("MaskPermissionEvictListener subscribe failed, will keep TTL-fallback only", e);
        }
    }

    @PreDestroy
    public void unsubscribe() {
        if (listenerId == null || redissonClient == null) {
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
        if (body == null || body.isEmpty() || resolver == null) {
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
                    log.info("MaskPermissionEvictListener evict users={}, source={}, resolver={}",
                            msg.getUids() == null ? 0 : msg.getUids().size(), msg.getSource(),
                            resolver.getClass().getSimpleName());
                    break;
                case ALL:
                    resolver.evictAll();
                    log.info("MaskPermissionEvictListener evict ALL, source={}, resolver={}",
                            msg.getSource(), resolver.getClass().getSimpleName());
                    break;
                default:
                    log.warn("MaskPermissionEvictListener unknown type={}", msg.getType());
            }
        } catch (Throwable e) {
            log.warn("MaskPermissionEvictListener handle msg failed, body={}", body, e);
        }
    }
}
