package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.calculation.service.InventoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class LocalInTransitHandler extends AbstractSkuCalculationHandler {
    @Resource
    private LocalPlanPurchaseHandler localPlanPurchaseHandler;
    @Resource
    private InventoryService inventoryService;


    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return localPlanPurchaseHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO dto) {
        return true;
    }

    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {
        int qty = inventoryService.getLocalInTransit(replenishmentResultDTO, replenishmentResultDTO.getCfgRuleStrategy(), r);
        replenishmentResultDTO.getReplenishmentDetail().setLocalInTransitQty(qty);
    }
}
