package com.erp.server.oms.service.strategy;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.server.oms.config.DataSourceQueryConfig;
import com.erp.server.oms.mapper.SoB2cMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * DORIS数据源查询策略
 * 先从DORIS查询ID列表，再从PostgreSQL查询完整数据
 * 包含重试机制和数据一致性校验
 *
 * @author Optimized by Claude
 * @since 2026-02-05
 */
@Slf4j
@Component
public class DorisQueryStrategy implements DataSourceQueryStrategy {

    @Resource
    private SoB2cMapper soB2cMapper;

    @Resource
    private DataSourceQueryConfig queryConfig;

    /**
     * DORIS数据源标识
     */
    private static final String DORIS_DATA_SOURCE = DynamicDataSourceTypeEnum.DORIS.getCode();

    /**
     * PostgreSQL数据源标识
     */
    private static final String POSTGRES_DATA_SOURCE = DynamicDataSourceTypeEnum.POSTGRES.getCode();

    /**
     * 默认SQL的key
     */
    private static final String DEFAULT_SQL_KEY = "default";

    /**
     * 外部表名称（PostgreSQL中的DORIS外部表）
     */
    private static final String FOREIGN_TABLE_NAME = "foreign_third_warehouse_delivery";

    /**
     * 原始表名称
     */
    private static final String ORIGINAL_TABLE_NAME = "erp_wms.third_warehouse_delivery";

    @Override
    public IPage<SoB2cDTO.ListDTO> executePaging(Page<SoB2cDTO.ListDTO> page, SoB2cDTO.PagingParamDTO params) {
        log.info("使用DORIS策略执行分页查询，当前页：{}，每页大小：{}", page.getCurrent(), page.getSize());

        String defaultSql = params.getSqlMap().get(DEFAULT_SQL_KEY);
        // 转换为PostgreSQL的SQL（替换表名）
        String pgSql = convertToPostgresSql(defaultSql);

        // 执行带重试的双数据源查询
        IPage<SoB2cDTO.ListDTO> pageData = executeWithRetry(page, params, defaultSql, pgSql);

        return pageData;
    }

    @Override
    public boolean support(String dataSourceType) {
        return DORIS_DATA_SOURCE.equals(dataSourceType);
    }

    /**
     * 执行带重试机制的查询
     *
     * @param page 分页参数
     * @param params 查询参数
     * @param dorisSql DORIS的SQL
     * @param pgSql PostgreSQL的SQL
     * @return 查询结果
     */
    private IPage<SoB2cDTO.ListDTO> executeWithRetry(Page<SoB2cDTO.ListDTO> page,
                                                       SoB2cDTO.PagingParamDTO params,
                                                       String dorisSql,
                                                       String pgSql) {
        int retryCount = 0;
        int maxRetryCount = queryConfig.getDorisMaxRetryCount();

        while (retryCount < maxRetryCount) {
            try {
                // 第一步：从DORIS查询ID列表
                IPage<SoB2cDTO.ListDTO> dorisResult = queryIdsFromDoris(page, params, dorisSql);

                // 如果DORIS没有数据，直接返回空结果
                if (CollUtil.isEmpty(dorisResult.getRecords())) {
                    log.info("DORIS查询结果为空，直接返回");
                    return dorisResult;
                }

                // 第二步：用ID列表从PostgreSQL查询完整数据
                IPage<SoB2cDTO.ListDTO> pgResult = queryDetailFromPostgres(
                        dorisResult,
                        params,
                        pgSql
                );

                // 第三步：验证数据一致性
                if (validateDataConsistency(dorisResult, pgResult)) {
                    log.info("DORIS和PostgreSQL数据一致性校验通过，返回结果");
                    return pgResult;
                }

                log.warn("DORIS和PostgreSQL数据不一致，重试次数：{}/{}", retryCount + 1, maxRetryCount);
                retryCount++;

            } catch (Exception e) {
                log.error("DORIS策略查询异常，重试次数：{}/{}", retryCount + 1, maxRetryCount, e);
                retryCount++;
            }
        }

        // 重试失败后，降级为直接从PostgreSQL查询
        log.warn("DORIS策略重试{}次后仍失败，降级为直接从PostgreSQL查询", maxRetryCount);
        return fallbackToPostgres(page, params, pgSql);
    }

    /**
     * 从DORIS查询ID列表
     *
     * @param page 分页参数
     * @param params 查询参数
     * @param dorisSql DORIS的SQL
     * @return 包含ID的查询结果
     */
    private IPage<SoB2cDTO.ListDTO> queryIdsFromDoris(Page<SoB2cDTO.ListDTO> page,
                                                        SoB2cDTO.PagingParamDTO params,
                                                        String dorisSql) {
        log.debug("步骤1：从DORIS查询ID列表");

        // 切换到DORIS数据源
        switchDataSource(DORIS_DATA_SOURCE);

        // 设置查询参数
        params.setDynamicDataSource(DORIS_DATA_SOURCE);
        params.getSqlMap().put(DEFAULT_SQL_KEY, dorisSql);
        params.setOnlyQueryId(1); // 只查询ID

        // 执行查询
        IPage<SoB2cDTO.ListDTO> result = soB2cMapper.paging(page, params, null);

        log.debug("DORIS查询到{}条记录", result.getRecords().size());
        return result;
    }

    /**
     * 从PostgreSQL查询完整数据
     *
     * @param dorisResult DORIS查询结果（包含ID列表）
     * @param params 查询参数
     * @param pgSql PostgreSQL的SQL
     * @return 完整数据查询结果
     */
    private IPage<SoB2cDTO.ListDTO> queryDetailFromPostgres(IPage<SoB2cDTO.ListDTO> dorisResult,
                                                              SoB2cDTO.PagingParamDTO params,
                                                              String pgSql) {
        log.debug("步骤2：从PostgreSQL查询完整数据");

        // 提取ID列表
        String idList = dorisResult.getRecords().stream()
                .map(SoB2cDTO.ListDTO::getId)
                .collect(Collectors.joining("','", "'", "'"));

        // 构建带ID过滤的SQL
        String pgSqlWithIds = pgSql + " and sb2c.id in (" + idList + ")";

        // 切换到PostgreSQL数据源
        switchDataSource(POSTGRES_DATA_SOURCE);

        // 设置查询参数
        params.setDynamicDataSource(POSTGRES_DATA_SOURCE);
        params.getSqlMap().put(DEFAULT_SQL_KEY, pgSqlWithIds);

        // 创建新的分页对象，不进行count查询，使用DORIS的分页信息
        int dorisRecordCount = dorisResult.getRecords().size();
        Page<SoB2cDTO.ListDTO> pgPage = new Page<>(1, -1, dorisRecordCount, false);

        // 执行查询
        IPage<SoB2cDTO.ListDTO> pgResult = soB2cMapper.paging(pgPage, params, null);

        // 恢复DORIS的分页信息
        if (CollUtil.isNotEmpty(pgResult.getRecords())) {
            pgResult.setCurrent(dorisResult.getCurrent());
            pgResult.setPages(dorisResult.getPages());
            pgResult.setSize(dorisResult.getSize());
            pgResult.setTotal(dorisResult.getTotal());
        }

        log.debug("PostgreSQL查询到{}条记录", pgResult.getRecords().size());
        return pgResult;
    }

    /**
     * 验证DORIS和PostgreSQL数据的一致性
     *
     * @param dorisResult DORIS查询结果
     * @param pgResult PostgreSQL查询结果
     * @return 是否一致
     */
    private boolean validateDataConsistency(IPage<SoB2cDTO.ListDTO> dorisResult,
                                             IPage<SoB2cDTO.ListDTO> pgResult) {
        if (CollUtil.isEmpty(pgResult.getRecords())) {
            log.warn("PostgreSQL查询结果为空，数据不一致");
            return false;
        }

        int dorisCount = dorisResult.getRecords().size();
        int pgCount = pgResult.getRecords().size();

        if (dorisCount != pgCount) {
            log.warn("数据量不一致：DORIS={}，PostgreSQL={}", dorisCount, pgCount);
            return false;
        }

        return true;
    }

    /**
     * 降级处理：直接从PostgreSQL查询
     *
     * @param page 分页参数
     * @param params 查询参数
     * @param pgSql PostgreSQL的SQL
     * @return 查询结果
     */
    private IPage<SoB2cDTO.ListDTO> fallbackToPostgres(Page<SoB2cDTO.ListDTO> page,
                                                         SoB2cDTO.PagingParamDTO params,
                                                         String pgSql) {
        log.info("执行降级策略：直接从PostgreSQL查询");

        switchDataSource(POSTGRES_DATA_SOURCE);

        params.setDynamicDataSource(POSTGRES_DATA_SOURCE);
        params.getSqlMap().put(DEFAULT_SQL_KEY, pgSql);

        return soB2cMapper.paging(page, params, null);
    }

    /**
     * 切换数据源
     *
     * @param dataSourceType 数据源类型
     */
    private void switchDataSource(String dataSourceType) {
        DynamicDataSourceContextHolder.poll();

        if (DORIS_DATA_SOURCE.equals(dataSourceType)) {
            DynamicDataSourceThreadLocal.set(DynamicDataSourceTypeEnum.DORIS);
            DynamicDataSourceContextHolder.push(DORIS_DATA_SOURCE);
            log.debug("切换到DORIS数据源");
        } else if (POSTGRES_DATA_SOURCE.equals(dataSourceType)) {
            DynamicDataSourceThreadLocal.set(DynamicDataSourceTypeEnum.POSTGRES);
            DynamicDataSourceContextHolder.push(POSTGRES_DATA_SOURCE);
            log.debug("切换到PostgreSQL数据源");
        }
    }

    /**
     * 将DORIS的SQL转换为PostgreSQL的SQL
     * 主要是替换表名
     *
     * @param dorisSql DORIS的SQL
     * @return PostgreSQL的SQL
     */
    private String convertToPostgresSql(String dorisSql) {
        if (dorisSql == null) {
            return null;
        }
        return dorisSql.replace(ORIGINAL_TABLE_NAME, FOREIGN_TABLE_NAME);
    }
}
