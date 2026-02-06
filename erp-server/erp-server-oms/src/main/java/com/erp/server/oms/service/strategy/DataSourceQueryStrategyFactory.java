package com.erp.server.oms.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据源查询策略工厂
 * 根据数据源类型选择合适的查询策略
 *
 * @author Optimized by Claude
 * @since 2026-02-05
 */
@Slf4j
@Component
public class DataSourceQueryStrategyFactory {

    @Resource
    private DorisQueryStrategy dorisQueryStrategy;

    @Resource
    private PostgresQueryStrategy postgresQueryStrategy;

    /**
     * 所有可用的策略列表
     */
    private List<DataSourceQueryStrategy> strategies;

    /**
     * 初始化策略列表
     */
    @PostConstruct
    public void init() {
        strategies = new ArrayList<>();
        strategies.add(dorisQueryStrategy);
        strategies.add(postgresQueryStrategy);
        log.info("数据源查询策略工厂初始化完成，共加载{}个策略", strategies.size());
    }

    /**
     * 根据数据源类型获取对应的查询策略
     *
     * @param dataSourceType 数据源类型
     * @return 查询策略
     */
    public DataSourceQueryStrategy getStrategy(String dataSourceType) {
        for (DataSourceQueryStrategy strategy : strategies) {
            if (strategy.support(dataSourceType)) {
                log.debug("选择策略：{}，数据源类型：{}", strategy.getClass().getSimpleName(), dataSourceType);
                return strategy;
            }
        }

        // 默认返回PostgreSQL策略
        log.warn("未找到匹配的策略，数据源类型：{}，使用默认PostgreSQL策略", dataSourceType);
        return postgresQueryStrategy;
    }
}
