package com.erp.server.wms.config;

import java.net.UnknownHostException;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.common.business.config.FastJson2JsonRedisSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * @Classname RdeisConfig

 * @Date 2022-07-28 16:42
 * @Created by yl
 */
@Slf4j
@Configuration
public class InventoryRedisConfig {

    @Resource
    private InventoryRedisConfigProperties inventoryRedisConfigProperties;

    @Bean
    public RedisConnectionFactory inventoryRedisConnectionFactory() {
        log.info("redis配置的节点: {}", inventoryRedisConfigProperties.getNodesInfoList());
        if (CollectionUtils.isEmpty(inventoryRedisConfigProperties.getNodesInfoList())) {
            throw new RuntimeException("redis nodesInfo is empty");
        }

        int nodeSize = inventoryRedisConfigProperties.getNodesInfoList().size();
        String password = inventoryRedisConfigProperties.getPassword();
        if (1 == nodeSize) {
            //standalone 单机模式
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
            RedisNode redisNode = inventoryRedisConfigProperties.getNodesInfoList().get(0);
            configuration.setHostName(Objects.requireNonNull(redisNode.getHost()));
            configuration.setPort(Objects.requireNonNull(redisNode.getPort()));
            //如果没配置db，则默认为第0个
            configuration.setDatabase(Objects.isNull(inventoryRedisConfigProperties.getDbIndex()) ? 0 : inventoryRedisConfigProperties.getDbIndex());
            if (!StringUtils.isEmpty(password)) {
                configuration.setPassword(RedisPassword.of(password));
            }
            // factory.setShareNativeConnection(false);//是否允许多个线程操作共用同一个缓存连接，默认true，false时每个操作都将开辟新的连接
            LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(configuration);
            return lettuceConnectionFactory;
        } else {
            //cluster模式
            RedisClusterConfiguration configuration = new RedisClusterConfiguration();
            configuration.setClusterNodes(inventoryRedisConfigProperties.getNodesInfoList());
            configuration.setMaxRedirects(inventoryRedisConfigProperties.getNodesInfoList().size());
            if (!StringUtils.isEmpty(password)) {
                configuration.setPassword(RedisPassword.of(password));
            }
            // factory.setShareNativeConnection(false);//是否允许多个线程操作共用同一个缓存连接，默认true，false时每个操作都将开辟新的连接
            return new LettuceConnectionFactory(configuration, getPoolConfig());
        }

    }

    private LettucePoolingClientConfiguration getPoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(inventoryRedisConfigProperties.getMaxTotal() != null ? inventoryRedisConfigProperties.getMaxTotal() : GenericObjectPoolConfig.DEFAULT_MAX_TOTAL);
        poolConfig.setMaxWaitMillis(Objects.isNull(inventoryRedisConfigProperties.getMaxWait()) ? -1 : inventoryRedisConfigProperties.getMaxWait());
        poolConfig.setMaxIdle(Objects.isNull(inventoryRedisConfigProperties.getMaxIdle()) ? 10 : inventoryRedisConfigProperties.getMaxIdle());
        poolConfig.setMinIdle(Objects.isNull(inventoryRedisConfigProperties.getMinIdle()) ? 0 : inventoryRedisConfigProperties.getMinIdle());
        log.info("redis最大连接数: {}", poolConfig.getMaxTotal());
        return LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                //.commandTimeout(Duration.ofMillis(1000))
                //.shutdownTimeout(Duration.ofMillis(5000))
                .build();
    }

    @Bean
    public RedisTemplate<Object, Object> inventoryRedisTemplate(RedisConnectionFactory inventoryRedisConnectionFactory){
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(inventoryRedisConnectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate inventoryStringRedisTemplate(RedisConnectionFactory inventoryRedisConnectionFactory) throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(inventoryRedisConnectionFactory);
        return template;
    }

}
