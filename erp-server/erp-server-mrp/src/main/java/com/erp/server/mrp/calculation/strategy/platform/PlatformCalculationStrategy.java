package com.erp.server.mrp.calculation.strategy.platform;

import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;

import java.time.LocalDate;
import java.util.List;

/**
 * 平台数据计算
 */
public interface PlatformCalculationStrategy {

    default boolean isMatch(String platform) {
        return getPlatform().getCode().equals(platform);
    }

    /**
     * 平台
     *
     * @return {@link CfgRulePlatformTypeEnum}
     */
    CfgRulePlatformTypeEnum getPlatform();

    /**
     * 清洗历史库存
     *
     * @param calculationDate 计算日
     * @param suggestions
     * @param cleanDay        清洗天数
     */
    void cleanHistoryInventory(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay);

    /**
     * 清洗历史销量
     *
     * @param calculationDate 计算日
     * @param suggestions     建议数据
     * @param cleanDay        清洗天数
     */
    void cleanHistorySalesByOrder(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay);

    /**
     * 清洗历史销量
     *
     * @param calculationDate 计算日
     * @param suggestions     建议数据
     * @param cleanDay        清洗天数
     */
    void cleanHistorySalesByOutStock(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay);
}
