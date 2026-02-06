package com.erp.server.oms.service.strategy;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoB2cDTO;

/**
 * 数据源查询策略接口
 * 用于处理不同数据源的分页查询逻辑
 *
 * @author Optimized by Claude
 * @since 2026-02-05
 */
public interface DataSourceQueryStrategy {

    /**
     * 执行分页查询
     *
     * @param page 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<SoB2cDTO.ListDTO> executePaging(Page<SoB2cDTO.ListDTO> page, SoB2cDTO.PagingParamDTO params);

    /**
     * 判断是否支持该数据源类型
     *
     * @param dataSourceType 数据源类型
     * @return 是否支持
     */
    boolean support(String dataSourceType);
}
