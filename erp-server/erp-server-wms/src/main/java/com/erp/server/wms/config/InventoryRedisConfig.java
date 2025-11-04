package com.erp.server.wms.config;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
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

import cn.hutool.core.text.CharSequenceUtil;
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

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

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

    /**
     * 定义redission分布式锁客户端bean
     * @return
     */
    @Bean
    public RedissonClient inventoryRedisson()  {
        Config config = new Config();
        List<RedisNode> redisNodes =  inventoryRedisConfigProperties.getNodesInfoList();
        if(1 == redisNodes.size()) {
            //单机
            SingleServerConfig singleServerConfig = config.useSingleServer();
            singleServerConfig.setAddress(CharSequenceUtil.format("redis://{}:{}",redisNodes.get(0).getHost(),redisNodes.get(0).getPort()))
                    .setDatabase(inventoryRedisConfigProperties.getDbIndex()).setConnectionMinimumIdleSize(10);
            if(CharSequenceUtil.isNotEmpty(inventoryRedisConfigProperties.getPassword())) {
                singleServerConfig.setPassword(inventoryRedisConfigProperties.getPassword());
            }
        } else {
            //集群，需包含从节点，为保证高可用，Cluster模式一个主节点有一个从节点，一般为三主三从（Cluster集群的投票容错机制要求至少半数节点认为某个节点挂了，该节点才算是挂了，当只有两个节点时是无法进行投票的，所以说至少需要3个节点）
            ClusterServersConfig clusterServersConfig = config.useClusterServers();
            clusterServersConfig.setScanInterval(5000);
            if(CharSequenceUtil.isNotEmpty(inventoryRedisConfigProperties.getPassword())) {
                clusterServersConfig.setPassword(inventoryRedisConfigProperties.getPassword());
            }
            redisNodes.stream().forEach(redisNode -> clusterServersConfig.addNodeAddress(CharSequenceUtil.format("redis://{}:{}",redisNode.getHost(),redisNode.getPort())));
        }
        //看门狗的锁续期时间，默认30s，这里配置成15s
        config.setLockWatchdogTimeout(15000);
        return Redisson.create(config);
    }
}
