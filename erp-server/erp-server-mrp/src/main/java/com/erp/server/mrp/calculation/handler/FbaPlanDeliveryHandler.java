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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class FbaPlanDeliveryHandler extends AbstractSkuCalculationHandler {
    @Resource
    private HistorySalesHandler historySalesHandler;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
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
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> estimatedDeliveryDetails = new ArrayList<>();
        //获取需要计算库存的FBA预计发货配置
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();

        //发货计划_手动新增

        Set<String> codes = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getFbaDeliveryPlanByManual());
        if (!CollectionUtils.isEmpty(codes)) {
            List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> fbaPlanDelivery = inventoryService.getFbaPlanDelivery(replenishmentResultDTO, codes, cfgRuleStrategyDTO.getStockUpResult());
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
