package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Component
public class LocalInTransitHandler extends AbstractSkuCalculationHandler {
    @Resource
    private LocalPlanPurchaseHandler localPlanPurchaseHandler;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return localPlanPurchaseHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        String baseKey = "MRP:" + CfgRulePlatformTypeEnum.AMAZON.getCode() + ":" + CfgRuleCommonTypeEnum.INVENTORY.getCode();
        Set<String> inTransit = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getLocalInTransit());
        if (CollectionUtils.isEmpty(inTransit)) {
            replenishmentResultDTO.getReplenishmentDetail().setFbaInTransitQty(0);
        }
        int qty = inventoryService.getLocalInTransit(replenishmentResultDTO, inTransit, cfgRuleStrategyDTO);
        replenishmentResultDTO.getReplenishmentDetail().setLocalInTransitQty(qty);
    }
}
