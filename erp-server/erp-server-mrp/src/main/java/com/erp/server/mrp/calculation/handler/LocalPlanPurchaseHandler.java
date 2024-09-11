package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.calculation.utils.TreeUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
public class LocalPlanPurchaseHandler extends AbstractSkuCalculationHandler {
    @Resource
    private TotalInventoryHandler totalInventoryHandler;
    @Resource
    private InventoryService inventoryService;
    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return totalInventoryHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        CfgRuleCommonDTO.StrategyResultDTO localPurchase = TreeUtils.findByCode(inventoryResult, CfgRuleInventoryNodeEnum.LOCAL_PURCHASE_ORDER.getCode());
        if (ObjectUtils.isEmpty(localPurchase) || CollectionUtils.isEmpty(localPurchase.getChildrenList())) {
            replenishmentResultDTO.getReplenishmentDetail().setFbaInTransitQty(0);
        }
        int qty = inventoryService.getLocalPurchase(replenishmentResultDTO, localPurchase, cfgRuleStrategyDTO);
        replenishmentResultDTO.getReplenishmentDetail().setLocalPlanPurchaseQty(qty);
    }
}
