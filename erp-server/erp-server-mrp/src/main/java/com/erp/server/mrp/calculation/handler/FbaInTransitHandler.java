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
import java.util.List;

@Component
public class FbaInTransitHandler extends AbstractSkuCalculationHandler {
    @Resource
    private FbaPlanDeliveryHandler fbaPlanDeliveryHandler;
    @Resource
    private InventoryService inventoryService;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return fbaPlanDeliveryHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType());
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        //获取需要计算库存的FBA在途配置
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        CfgRuleCommonDTO.StrategyResultDTO inTransit = TreeUtils.findByCode(inventoryResult, CfgRuleInventoryNodeEnum.FBA_IN_TRANSIT.getCode());
        if (ObjectUtils.isEmpty(inTransit) || CollectionUtils.isEmpty(inTransit.getChildrenList())) {
            replenishmentResultDTO.getReplenishmentDetail().setFbaInTransitQty(0);
        }
        String code = inTransit.getChildrenList().stream()
                .filter(v -> "true".equals(v.getValue()))
                .map(CfgRuleCommonDTO.StrategyResultDTO::getCode)
                .findFirst().orElse(null);
        int qty = inventoryService.getFbaInTransit(replenishmentResultDTO, code, cfgRuleStrategyDTO.getStockUpResult());
        replenishmentResultDTO.getReplenishmentDetail().setFbaInTransitQty(qty);
    }
}
