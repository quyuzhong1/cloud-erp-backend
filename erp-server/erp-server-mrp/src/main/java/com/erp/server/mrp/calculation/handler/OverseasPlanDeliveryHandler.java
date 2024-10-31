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
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
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
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = new ArrayList<>();
        //获取需要计算库存的FBA预计发货配置
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        //补货计划
        Set<String> replenishmentPlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasReplenishmentPlan());
        if (!CollectionUtils.isEmpty(replenishmentPlan)) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> planDelivery = inventoryService.getReplenishmentPlan(replenishmentResultDTO, replenishmentPlan, cfgRuleStrategyDTO.getStockUpResult(),
                    ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY);
            if (!CollectionUtils.isEmpty(planDelivery)) {
                estimatedDeliveryDetails.addAll(planDelivery);
            }
        }
        //发货计划_补货计划下推
        Set<String> replenishmentDeliveryPlan = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasDeliveryPlanByReplenishment());
        if (!CollectionUtils.isEmpty(replenishmentDeliveryPlan)) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> planDelivery = inventoryService.getPlanDelivery(replenishmentResultDTO, replenishmentDeliveryPlan, cfgRuleStrategyDTO.getStockUpResult(),
                    SourceTypeEnum.REPLENISHMENT_PLAN.getCode(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY, DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode());
            if (!CollectionUtils.isEmpty(planDelivery)) {
                estimatedDeliveryDetails.addAll(planDelivery);
            }
        }
        //发货计划_手动新增
        Set<String> codes = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasDeliveryPlanByManual());
        if (!CollectionUtils.isEmpty(codes)) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> planDelivery = inventoryService.getPlanDelivery(replenishmentResultDTO, codes, cfgRuleStrategyDTO.getStockUpResult(),
                    SourceTypeEnum.DELIVERY_PLAN.getCode(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY, DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode());
            if (!CollectionUtils.isEmpty(planDelivery)) {
                estimatedDeliveryDetails.addAll(planDelivery);
            }
        }
        replenishmentResultDTO.setOverseasDeliveryDetails(estimatedDeliveryDetails);
        Integer qty = estimatedDeliveryDetails.stream()
                .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getQty)
                .reduce(0, Math::addExact);
        replenishmentResultDTO.getReplenishmentDetail().setOverseasPlanDeliveryQty(qty);
    }
}