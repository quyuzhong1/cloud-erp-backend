package com.erp.server.mrp.calculation.strategy.platform;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.entity.SalesInfoEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;

import java.util.List;

/**
 * 平台数据计算
 */
public interface PlatformCalculationStrategy {

    /**
     * 根据平台获取历史销量
     * @param cfgRuleSalesQty 销量配置
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> calculationHistorySale(String calcDate, CfgRuleSalesQtyEntity cfgRuleSalesQty);

//    List<SalesInfoEntity> getHistoryInventory();

    default boolean isMatch(String platform) {
        return getPlatform().getCode().equals(platform);
    }

    /**
     * 平台
     * @return {@link CfgRulePlatformTypeEnum}
     */
    CfgRulePlatformTypeEnum getPlatform();
}
