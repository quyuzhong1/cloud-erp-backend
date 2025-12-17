package com.erp.server.wms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Classname: RedisConfigProperties
 * @Description: redis配置
 * @CreateTime: 2023-04-24  18:43
 * @Author: zhangchunlin
 */
@ConfigurationProperties(prefix = InventoryRedisConfigProperties.PREFIX)
@RefreshScope
@Component
public class InventoryRedisConfigProperties {

    public static final String PREFIX = "inventoryredis";

    private Integer cacheExpireTime;
    //随机数
    private Integer randomRange;
    private String password;
    private Integer timeoutInMillis;
    private Integer readTimeoutInMillis;
    private Integer dbIndex;
    private Integer maxTotal;
    //#连接池最大连接数（使用负值表示没有限制）
    private Integer maxActive;
    //#连接池中的最大空闲连接
    private Integer maxIdle;
    //#连接池最大阻塞等待时间（使用负值表示没有限制）
    private Integer maxWait;
    //#连接池中的最小空闲连接
    private Integer minIdle;
    //#支持单节点和集群
    private List<Map<String, String>> nodesInfo;

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Integer getCacheExpireTime() {
        return cacheExpireTime;
    }

    public void setCacheExpireTime(Integer cacheExpireTime) {
        this.cacheExpireTime = cacheExpireTime;
    }

    public Integer getTimeoutInMillis() {
        return timeoutInMillis;
    }

    public void setTimeoutInMillis(Integer timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
    }

    public Integer getReadTimeoutInMillis() {
        return readTimeoutInMillis;
    }

    public void setReadTimeoutInMillis(Integer readTimeoutInMillis) {
        this.readTimeoutInMillis = readTimeoutInMillis;
    }

    public Integer getRandomRange() {
        return randomRange;
    }

    public void setRandomRange(Integer randomRange) {
        this.randomRange = randomRange;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(Integer dbIndex) {
        this.dbIndex = dbIndex;
    }

    public List<Map<String, String>> getNodesInfo() {
        return nodesInfo;
    }

    public void setNodesInfo(List<Map<String, String>> nodesInfo) {
        this.nodesInfo = nodesInfo;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public List<RedisNode> getNodesInfoList() {
        if (CollectionUtils.isEmpty(nodesInfo)) {
            throw new RuntimeException("redis nodes is empty,please config it in config center");
        }
        List<RedisNode> list = new ArrayList<>();
        nodesInfo.forEach(map -> list.add(new RedisNode(map.get("ip"), Integer.parseInt(map.get("port")))));
        return list;
    }

    @Override
    public String toString() {
        return "RedisProperties{" +
                "cacheExpireTime=" + cacheExpireTime +
                ", randomRange=" + randomRange +
                ", password='" + password + '\'' +
                ", timeoutInMillis=" + timeoutInMillis +
                ", readTimeoutInMillis=" + readTimeoutInMillis +
                ", dbIndex=" + dbIndex +
                ", maxTotal=" + maxTotal +
                ", nodesInfo=" + nodesInfo +
                '}';
    }
}
