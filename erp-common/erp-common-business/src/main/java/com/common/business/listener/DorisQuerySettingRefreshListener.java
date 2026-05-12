package com.common.business.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.common.business.cache.DorisQuerySettingLocalCache;
import com.common.business.dto.DorisQuerySettingFullCacheDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * 订阅 DMP 广播的全量 Doris 路由配置，反序列化后交给 {@link DorisQuerySettingLocalCache} 原子替换
 *
 * <p>仅当 {@code spring.datasource.dynamic.enabled=true} 时启用，与 {@link DorisQuerySettingLocalCache}
 * 及 {@code DorisQuerySettingListenerConfig} 三者条件保持一致，避免在未启用动态数据源的服务里因找不到
 * {@link DorisQuerySettingLocalCache} 而启动失败。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.datasource.dynamic.enabled", havingValue = "true")
public class DorisQuerySettingRefreshListener implements MessageListener {

    @Autowired
    private DorisQuerySettingLocalCache localCache;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (message == null || message.getBody() == null || message.getBody().length == 0) {
            return;
        }
        try {
            String body = new String(message.getBody());
            DorisQuerySettingFullCacheDTO payload = JSON.parseObject(body, DorisQuerySettingFullCacheDTO.class);
            localCache.apply(payload);
        } catch (Throwable e) {
            log.warn("DorisQuerySettingRefreshListener handle message failed, snapshot keeps unchanged", e);
        }
    }
}
