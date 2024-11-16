package com.cloud.erp.gateway.config;
import java.util.Objects;

import javax.annotation.Resource;

import com.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.common.business.config.RedisConfigProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ReactiveRedisConfig {

	@Resource
    private RedisConfigProperties redisProperties;
	
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(LettucePoolingClientConfiguration lettucePoolingClientConfiguration) {
        log.info("redis配置的节点: {}", redisProperties.getNodesInfoList());
        if (CollectionUtils.isEmpty(redisProperties.getNodesInfoList())) {
            ServiceException.runError("redis nodesInfo is empty");
        }

        int nodeSize = redisProperties.getNodesInfoList().size();
        String password = redisProperties.getPassword();
        if (1 == nodeSize) {
            //standalone 单机模式
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
            RedisNode redisNode = redisProperties.getNodesInfoList().get(0);
            configuration.setHostName(Objects.requireNonNull(redisNode.getHost()));
            configuration.setPort(Objects.requireNonNull(redisNode.getPort()));
            //如果没配置db，则默认为第0个
            configuration.setDatabase(Objects.isNull(redisProperties.getDbIndex()) ? 0 : redisProperties.getDbIndex());
            if (!StringUtils.isEmpty(password)) {
                configuration.setPassword(RedisPassword.of(password));
            }
            // factory.setShareNativeConnection(false);//是否允许多个线程操作共用同一个缓存连接，默认true，false时每个操作都将开辟新的连接
            return new LettuceConnectionFactory(configuration);
        } else {
            //cluster模式
            RedisClusterConfiguration configuration = new RedisClusterConfiguration();
            configuration.setClusterNodes(redisProperties.getNodesInfoList());
            configuration.setMaxRedirects(redisProperties.getNodesInfoList().size());
            if (!StringUtils.isEmpty(password)) {
                configuration.setPassword(RedisPassword.of(password));
            }
            // factory.setShareNativeConnection(false);//是否允许多个线程操作共用同一个缓存连接，默认true，false时每个操作都将开辟新的连接
            return new LettuceConnectionFactory(configuration, lettucePoolingClientConfiguration);
        }

    }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            @Qualifier("reactiveRedisConnectionFactory") ReactiveRedisConnectionFactory factory) {
        return new ReactiveStringRedisTemplate(factory);
    }

}
