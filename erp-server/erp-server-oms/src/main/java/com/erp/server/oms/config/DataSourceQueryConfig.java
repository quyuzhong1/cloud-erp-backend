package com.erp.server.oms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 数据源查询配置
 * 用于配置双数据源查询的相关参数
 *
 * @author Optimized by Claude
 * @since 2026-02-05
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "erp.datasource.query")
public class DataSourceQueryConfig {

    /**
     * DORIS查询最大重试次数
     * 默认值：3
     */
    private int dorisMaxRetryCount = 3;

    /**
     * 是否启用DORIS双数据源查询策略
     * 默认值：true
     */
    private boolean dorisStrategyEnabled = true;

    /**
     * 查询超时时间（毫秒）
     * 默认值：30000（30秒）
     */
    private long queryTimeout = 30000;

    /**
     * 是否启用查询性能监控
     * 默认值：true
     */
    private boolean performanceMonitorEnabled = true;

    /**
     * 慢查询阈值（毫秒）
     * 超过此阈值的查询会被记录为慢查询
     * 默认值：3000（3秒）
     */
    private long slowQueryThreshold = 3000;

    /**
     * 是否启用查询结果缓存
     * 默认值：false
     */
    private boolean cacheEnabled = false;

    /**
     * 缓存过期时间（秒）
     * 默认值：300（5分钟）
     */
    private int cacheExpireSeconds = 300;
}
