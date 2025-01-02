package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class OverseasPlanDeliveryHandler extends AbstractSkuCalculationHandler {
    @Resource
    private LocalUsableHandler localUsableHandler;
    @Resource
    private InventoryService inventoryService;

    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return localUsableHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO replenishmentResultDTO) {
        return CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                || Boolean.TRUE.equals(replenishmentResultDTO.getCfgRuleStrategy().getWarehouseResult().getIsEnableOverseas());
    }

    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {
        int qty = inventoryService.getOverseasPlanDelivery(replenishmentResultDTO, replenishmentResultDTO.getCfgRuleStrategy());
        replenishmentResultDTO.getReplenishmentDetail().setOverseasPlanDeliveryQty(qty);
    }
}