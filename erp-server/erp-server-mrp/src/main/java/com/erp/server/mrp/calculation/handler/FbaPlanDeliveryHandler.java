package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FbaPlanDeliveryHandler extends AbstractSkuCalculationHandler {
    @Resource
    private OverseasUsableHandler overseasUsableHandler;

    @Resource
    private InventoryService inventoryService;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return overseasUsableHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType());
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
//        //获取需要计算库存的FBA可用配置
//        CfgRuleCommonDTO.StrategyResultDTO inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
//        //补货计划
//        CfgRuleCommonDTO.StrategyResultDTO replenishmentPlan = TreeUtils.findByCode(inventoryResult, CfgRuleInventoryNodeEnum.FBA_REPLENISHMENT_PLAN.getCode());
//        if (ObjectUtils.isEmpty(replenishmentPlan) || )
//        //发货计划_补货计划下推
//        CfgRuleCommonDTO.StrategyResultDTO replenishmentDeliveryPlan = TreeUtils.findByCode(inventoryResult, CfgRuleInventoryNodeEnum.FBA_DELIVERY_PLAN_BY_REPLENISHMENT.getCode());
//        //发货计划_手动新增
//        CfgRuleCommonDTO.StrategyResultDTO deliveryPlan = TreeUtils.findByCode(inventoryResult, CfgRuleInventoryNodeEnum.FBA_DELIVERY_PLAN_BY_MANUAL.getCode());
//
//        if (ObjectUtils.isEmpty(usable) || CollectionUtils.isEmpty(usable.getChildrenList())) {
//            replenishmentResultDTO.getReplenishmentDetail().setFbaInTransitQty(0);
//        }
//        String code = usable.getChildrenList().stream()
//                .filter(v -> "true".equals(v.getValue()))
//                .map(CfgRuleCommonDTO.StrategyResultDTO::getCode)
//                .findFirst().orElse(null);
//        int qty = inventoryService.getFbaInTransit(replenishmentResultDTO, code, cfgRuleStrategyDTO.getStockUpResult());
//        replenishmentResultDTO.getReplenishmentDetail().setFbaInTransitQty(qty);
    }
}
