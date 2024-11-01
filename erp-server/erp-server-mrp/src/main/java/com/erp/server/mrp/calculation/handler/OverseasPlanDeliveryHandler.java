package com.erp.server.mrp.calculation.handler;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;
import com.erp.model.wms.enums.DeliveryPlanTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class OverseasPlanDeliveryHandler extends AbstractSkuCalculationHandler {
    @Resource
    private LocalUsableHandler localUsableHandler;
    @Resource
    private InventoryService inventoryService;
    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return localUsableHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                || Boolean.TRUE.equals(cfgRuleStrategyDTO.getWarehouseResult().getIsEnableOverseas());

    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        int qty = inventoryService.getOverseasPlanDelivery(replenishmentResultDTO, cfgRuleStrategyDTO);
        replenishmentResultDTO.getReplenishmentDetail().setOverseasPlanDeliveryQty(qty);
    }
}