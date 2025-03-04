package com.erp.server.mrp.calculation.strategy.sales;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.SalesEstimateTypeEnum;

/**
 * 销量预估策略
 */

public interface SalesEstimateStrategy {

    default boolean isMatch(String type) {
        return getType().getCode().equals(type);
    }

    /**
     * 平台
     *
     * @return {@link SalesEstimateTypeEnum}
     */
    SalesEstimateTypeEnum getType();


    /**
     * 处理逻辑
     *
     * @param dto 参数
     */
    void process(ReplenishmentResultDTO dto);
}
