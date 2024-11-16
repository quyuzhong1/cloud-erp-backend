package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Component
public class OverseasUsableHandler extends AbstractSkuCalculationHandler {
    @Resource
    private OverseasInTransitHandler overseasInTransitHandler;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return overseasInTransitHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return false;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        Set<String> usable = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getOverseasUsable());
        if (CollectionUtils.isEmpty(usable)){
            replenishmentResultDTO.getReplenishmentDetail().setFbaUsableQty(0);
        }
        int qty = inventoryService.getOverseasUsable(replenishmentResultDTO, usable, cfgRuleStrategyDTO);
        replenishmentResultDTO.getReplenishmentDetail().setOverseasUsableQty(qty);
    }
}
