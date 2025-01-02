package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.calculation.service.InventoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class LocalPlanPurchaseHandler extends AbstractSkuCalculationHandler {
    @Resource
    private TotalInventoryHandler totalInventoryHandler;
    @Resource
    private InventoryService inventoryService;


    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return totalInventoryHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO dto) {
        return true;
    }

    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {
        int qty = inventoryService.getLocalPurchase(replenishmentResultDTO, replenishmentResultDTO.getCfgRuleStrategy());
        replenishmentResultDTO.getReplenishmentDetail().setLocalPlanPurchaseQty(qty);
    }

}
