package com.common.business.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.common.business.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname RdeisConfig

 * @Date 2022-07-28 16:42
 * @Created by yl
 */
@Slf4j
@EnableCaching
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisConfig {

    @Resource
    private RedisConfigProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("redis配置的节点: {}", redisProperties.getNodesInfoList());
        if (CollectionUtils.isEmpty(redisProperties.getNodesInfoList())) {
            throw new RuntimeException("redis nodesInfo is empty");
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
            LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(configuration);
            return lettuceConnectionFactory;
        } else {
            //cluster模式
            RedisClusterConfiguration configuration = new RedisClusterConfiguration();
            configuration.setClusterNodes(redisProperties.getNodesInfoList());
            configuration.setMaxRedirects(redisProperties.getNodesInfoList().size());
            if (!StringUtils.isEmpty(password)) {
                configuration.setPassword(RedisPassword.of(password));
            }
            // factory.setShareNativeConnection(false);//是否允许多个线程操作共用同一个缓存连接，默认true，false时每个操作都将开辟新的连接
            return new LettuceConnectionFactory(configuration, getPoolConfig());
        }

    }

    @Bean
    public LettucePoolingClientConfiguration getPoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(redisProperties.getMaxTotal() != null ? redisProperties.getMaxTotal() : GenericObjectPoolConfig.DEFAULT_MAX_TOTAL);
        poolConfig.setMaxWaitMillis(Objects.isNull(redisProperties.getMaxWait()) ? -1 : redisProperties.getMaxWait());
        poolConfig.setMaxIdle(Objects.isNull(redisProperties.getMaxIdle()) ? 10 : redisProperties.getMaxIdle());
        poolConfig.setMinIdle(Objects.isNull(redisProperties.getMinIdle()) ? 0 : redisProperties.getMinIdle());
        log.info("redis最大连接数: {}", poolConfig.getMaxTotal());
        return LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                //.commandTimeout(Duration.ofMillis(1000))
                //.shutdownTimeout(Duration.ofMillis(5000))
                .build();
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

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
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * redis缓存管理器
     */
    @Bean(name = "cacheManager")
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 初始化一个RedisCacheWriter，采用fastjson序列化方式
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);
        RedisSerializationContext.SerializationPair<Object> pair = RedisSerializationContext.SerializationPair.fromSerializer(serializer);
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // 设置CacheManager的值序列化方式为json序列化
                .serializeValuesWith(pair)
                // 设置缓存有效期
                .entryTtl(Duration.ofSeconds(redisProperties.getCacheExpireTime() != null ? redisProperties.getCacheExpireTime() : 60));

        // 特定缓存的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("cache:wms:", redisCacheConfiguration.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("cache:sys", redisCacheConfiguration.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("cache:oms", redisCacheConfiguration.entryTtl(Duration.ofMillis(30)));


        return RedisCacheManager
                .builder(redisCacheWriter)
                .cacheDefaults(redisCacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * 定义redission分布式锁客户端bean
     * @return
     */
    @Primary
    @Bean
    public RedissonClient redisson()  {
        Config config = new Config();
        List<RedisNode> redisNodes =  redisProperties.getNodesInfoList();
        if(1 == redisNodes.size()) {
            //单机
            SingleServerConfig singleServerConfig = config.useSingleServer();
            singleServerConfig.setAddress(CharSequenceUtil.format("redis://{}:{}",redisNodes.get(0).getHost(),redisNodes.get(0).getPort()))
                    .setDatabase(redisProperties.getDbIndex()).setConnectionMinimumIdleSize(10);
            if(CharSequenceUtil.isNotEmpty(redisProperties.getPassword())) {
                singleServerConfig.setPassword(redisProperties.getPassword());
            }
        } else {
            //集群，需包含从节点，为保证高可用，Cluster模式一个主节点有一个从节点，一般为三主三从（Cluster集群的投票容错机制要求至少半数节点认为某个节点挂了，该节点才算是挂了，当只有两个节点时是无法进行投票的，所以说至少需要3个节点）
            ClusterServersConfig clusterServersConfig = config.useClusterServers();
            clusterServersConfig.setScanInterval(5000);
            if(CharSequenceUtil.isNotEmpty(redisProperties.getPassword())) {
                clusterServersConfig.setPassword(redisProperties.getPassword());
            }
            redisNodes.stream().forEach(redisNode -> clusterServersConfig.addNodeAddress(CharSequenceUtil.format("redis://{}:{}",redisNode.getHost(),redisNode.getPort())));
        }
        //看门狗的锁续期时间，默认30s，这里配置成15s
        config.setLockWatchdogTimeout(15000);
        return Redisson.create(config);
    }
    @Bean("myKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (target, method, objects) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(":" + method.getName() + ":" + JSONObject.toJSONString(objects));
            return MD5Util.toMD5(sb.toString());
        };
    }
}
