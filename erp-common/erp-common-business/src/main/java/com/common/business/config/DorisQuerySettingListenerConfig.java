package com.common.business.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.ChannelTopic;

import com.common.business.constant.RedisCacheConstants;
import com.common.business.listener.DorisQuerySettingRefreshListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 仅当启用动态数据源时，注册 Redis Pub/Sub 监听容器，订阅
 * {@link RedisCacheConstants#DORIS_QUERY_CFG_REFRESH_CHANNEL} 频道。
 *
 * <p>
 * 项目此前没有任何 Redis Pub/Sub 用例，因此独立声明 {@link RedisMessageListenerContainer} Bean，
 * 避免与未来其它订阅相互干扰；Bean 名称带功能后缀，便于排查。
 * </p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.datasource.dynamic.enabled", havingValue = "true")
public class DorisQuerySettingListenerConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private DorisQuerySettingRefreshListener dorisQuerySettingRefreshListener;

    @Bean(name = "dorisQuerySettingListenerContainer", destroyMethod = "destroy")
    public RedisMessageListenerContainer dorisQuerySettingListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        Topic topic = new ChannelTopic(RedisCacheConstants.DORIS_QUERY_CFG_REFRESH_CHANNEL);
        container.addMessageListener(dorisQuerySettingRefreshListener, topic);
        log.info("DorisQuerySettingListenerContainer subscribe channel={}",
                RedisCacheConstants.DORIS_QUERY_CFG_REFRESH_CHANNEL);
        return container;
    }
}
