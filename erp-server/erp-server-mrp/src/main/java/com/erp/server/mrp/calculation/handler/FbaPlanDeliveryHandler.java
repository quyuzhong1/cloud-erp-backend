package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.calculation.utils.TreeUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FbaPlanDeliveryHandler extends AbstractSkuCalculationHandler {
    @Resource
    private HistorySalesHandler historySalesHandler;

    @Resource
    private InventoryService inventoryService;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return historySalesHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType());
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = new ArrayList<>();
        //获取需要计算库存的FBA预计发货配置
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        //补货计划
        CfgRuleCommonDTO.StrategyResultDTO replenishmentPlan = TreeUtils.findByCode(inventoryResult, CfgRuleInventoryNodeEnum.FBA_REPLENISHMENT_PLAN.getCode());
        if (!ObjectUtils.isEmpty(replenishmentPlan) && !CollectionUtils.isEmpty(replenishmentPlan.getChildrenList())) {
            // todo
        }
        //发货计划_补货计划下推
        CfgRuleCommonDTO.StrategyResultDTO replenishmentDeliveryPlan = TreeUtils.findByCode(inventoryResult, CfgRuleInventoryNodeEnum.FBA_DELIVERY_PLAN_BY_REPLENISHMENT.getCode());
        if (!ObjectUtils.isEmpty(replenishmentDeliveryPlan) && !CollectionUtils.isEmpty(replenishmentDeliveryPlan.getChildrenList())) {
            // todo
        }
        //发货计划_手动新增
        CfgRuleCommonDTO.StrategyResultDTO deliveryPlan = TreeUtils.findByCode(inventoryResult, CfgRuleInventoryNodeEnum.FBA_DELIVERY_PLAN_BY_MANUAL.getCode());
        if (!ObjectUtils.isEmpty(deliveryPlan) && !CollectionUtils.isEmpty(deliveryPlan.getChildrenList())) {
            List<String> strategyCodes = deliveryPlan.getChildrenList().stream()
                    .filter(v -> "true".equals(v.getValue()))
                    .map(CfgRuleCommonDTO.StrategyResultDTO::getCode)
                    .distinct()
                    .collect(Collectors.toList());
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> fbaPlanDelivery = inventoryService.getFbaPlanDelivery(replenishmentResultDTO, strategyCodes, cfgRuleStrategyDTO.getStockUpResult());
            if (!CollectionUtils.isEmpty(fbaPlanDelivery)) {
                estimatedDeliveryDetails.addAll(fbaPlanDelivery);
            }
        }
        replenishmentResultDTO.setFbaDeliveryDetails(estimatedDeliveryDetails);
        Integer qty = estimatedDeliveryDetails.stream()
                .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getQty)
                .reduce(0, Math::addExact);
        replenishmentResultDTO.getReplenishmentDetail().setFbaPlanDeliveryQty(qty);
    }
}
