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
public class OverseasInTransitHandler extends AbstractSkuCalculationHandler {
    @Resource
    private OverseasPlanDeliveryHandler overseasPlanDeliveryHandler;

    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
    @Resource
    private InventoryService inventoryService;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return overseasPlanDeliveryHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                || Boolean.TRUE.equals(cfgRuleStrategyDTO.getWarehouseResult().getIsEnableOverseas());
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        Set<String> codes = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasInTransit());
        if (CollectionUtils.isEmpty(codes)) {
            replenishmentResultDTO.getReplenishmentDetail().setOverseasInTransitQty(0);
        }
        List<String> code = new ArrayList<>(codes);
        int qty = inventoryService.getOverseasInTransit(replenishmentResultDTO, code.get(0), cfgRuleStrategyDTO);
        replenishmentResultDTO.getReplenishmentDetail().setOverseasInTransitQty(qty);
    }
}
