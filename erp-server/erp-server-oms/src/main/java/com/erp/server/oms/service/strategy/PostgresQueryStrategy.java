package com.erp.server.oms.service.strategy;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.server.oms.mapper.SoB2cMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * PostgreSQL数据源查询策略
 * 直接从PostgreSQL查询数据
 *
 * @author Optimized by Claude
 * @since 2026-02-05
 */
@Slf4j
@Component
public class PostgresQueryStrategy implements DataSourceQueryStrategy {

    @Resource
    private SoB2cMapper soB2cMapper;

    /**
     * PostgreSQL数据源标识
     */
    private static final String POSTGRES_DATA_SOURCE = DynamicDataSourceTypeEnum.POSTGRES.getCode();

    @Override
    public IPage<SoB2cDTO.ListDTO> executePaging(Page<SoB2cDTO.ListDTO> page, SoB2cDTO.PagingParamDTO params) {
        log.info("使用PostgreSQL策略执行分页查询，当前页：{}，每页大小：{}", page.getCurrent(), page.getSize());

        // 直接从PostgreSQL查询
        IPage<SoB2cDTO.ListDTO> result = soB2cMapper.paging(page, params, null);

        log.debug("PostgreSQL查询到{}条记录，总数：{}", result.getRecords().size(), result.getTotal());

        return result;
    }

    @Override
    public boolean support(String dataSourceType) {
        // 支持PostgreSQL或者空数据源（默认）
        return POSTGRES_DATA_SOURCE.equals(dataSourceType) || dataSourceType == null || dataSourceType.isEmpty();
    }
}
