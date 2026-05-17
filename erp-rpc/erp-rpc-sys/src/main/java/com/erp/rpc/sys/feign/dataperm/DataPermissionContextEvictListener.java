package com.erp.rpc.sys.feign.dataperm;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dataperm.DataPermissionContextEvictMessage;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据权限上下文失效广播监听器（{@link RedisCacheConstants#DATA_PERM_CTX_EVICT_CHANNEL}）
 *
 * <p>位于 {@code erp-rpc-sys}：业务节点引入 {@code erp-rpc-sys} 即自动获得 listener，
 * 与 {@link FeignDataPermissionContextResolver} 紧耦合（直接注入具体 resolver 而非 SPI 接口）。
 * Step 1 已经评估过：本仓库 dataPerm 只会有 Feign 一种实现，没必要再抽 SPI 接口。</p>
 *
 * <h3>设计要点</h3>
 * <ul>
 *   <li><b>沿用 mask 的同构 Pub/Sub 模式</b>：与 {@code MaskPermissionEvictListener} 完全同构，
 *       基于 Redisson {@link RTopic}，{@code @PostConstruct} 注册、{@code @PreDestroy} 解注册。</li>
 *   <li><b>{@code required=false} 的 RedissonClient</b>：极端环境（无 Redis 的本地单测 / 离线工具）下
 *       不强制要求 Redis，listener 安静禁用，本地 TTL 仍然兜底。</li>
 *   <li><b>sys 进程本身也会订阅</b>：sys 也引 erp-rpc-sys，所以 sys 进程也会装配本 listener；
 *       但 sys 进程不通过 aspect 走 resolver，cache 始终空，listener 收到消息只是 {@code cache.invalidateAll()}
 *       no-op，不会引发 loopback 风暴（publisher 只在写路径触发，不在 listener 里触发）。</li>
 * </ul>
 *
 * <h3>消息处理</h3>
 * <ul>
 *   <li>{@link DataPermissionContextEvictMessage.Type#USER}：调
 *       {@link FeignDataPermissionContextResolver#evict}</li>
 *   <li>{@link DataPermissionContextEvictMessage.Type#ALL}：调
 *       {@link FeignDataPermissionContextResolver#evictAll}</li>
 * </ul>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class DataPermissionContextEvictListener {

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Resource
    private FeignDataPermissionContextResolver resolver;

    private volatile Integer listenerId;

    @PostConstruct
    public void subscribe() {
        if (redissonClient == null) {
            log.info("DataPermissionContextEvictListener disabled: RedissonClient not available");
            return;
        }
        try {
            RTopic topic = redissonClient.getTopic(RedisCacheConstants.DATA_PERM_CTX_EVICT_CHANNEL);
            listenerId = topic.addListener(String.class, (channel, body) -> handle(body));
            log.info("DataPermissionContextEvictListener subscribe channel={}, listenerId={}, resolver={}",
                    RedisCacheConstants.DATA_PERM_CTX_EVICT_CHANNEL, listenerId,
                    resolver == null ? null : resolver.getClass().getSimpleName());
        } catch (Throwable e) {
            log.warn("DataPermissionContextEvictListener subscribe failed, will keep TTL-fallback only", e);
        }
    }

    @PreDestroy
    public void unsubscribe() {
        if (listenerId == null || redissonClient == null) {
            return;
        }
        try {
            redissonClient.getTopic(RedisCacheConstants.DATA_PERM_CTX_EVICT_CHANNEL)
                    .removeListener(listenerId);
            log.info("DataPermissionContextEvictListener unsubscribe channel={}, listenerId={}",
                    RedisCacheConstants.DATA_PERM_CTX_EVICT_CHANNEL, listenerId);
        } catch (Throwable e) {
            log.warn("DataPermissionContextEvictListener unsubscribe failed", e);
        } finally {
            listenerId = null;
        }
    }

    private void handle(String body) {
        if (body == null || body.isEmpty() || resolver == null) {
            return;
        }
        try {
            DataPermissionContextEvictMessage msg = JSON.parseObject(body, DataPermissionContextEvictMessage.class);
            if (msg == null || msg.getType() == null) {
                log.warn("DataPermissionContextEvictListener got invalid msg: {}", body);
                return;
            }
            switch (msg.getType()) {
                case USER:
                    resolver.evict(msg.getUids());
                    log.info("DataPermissionContextEvictListener evict users={}, source={}",
                            msg.getUids() == null ? 0 : msg.getUids().size(), msg.getSource());
                    break;
                case ALL:
                    resolver.evictAll();
                    log.info("DataPermissionContextEvictListener evict ALL, source={}", msg.getSource());
                    break;
                default:
                    log.warn("DataPermissionContextEvictListener unknown type={}", msg.getType());
            }
        } catch (Throwable e) {
            log.warn("DataPermissionContextEvictListener handle msg failed, body={}", body, e);
        }
    }
}
