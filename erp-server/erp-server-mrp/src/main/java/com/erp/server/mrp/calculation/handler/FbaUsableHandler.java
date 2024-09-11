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
import java.util.stream.Collectors;

@Component
public class FbaUsableHandler extends AbstractSkuCalculationHandler {
    @Resource
    private FbaInTransitHandler fbaInTransitHandler;
    @Resource
    private InventoryService inventoryService;
    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return fbaInTransitHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType());
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        //获取需要计算库存的FBA可用配置
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        CfgRuleCommonDTO.StrategyResultDTO usable = TreeUtils.findByCode(inventoryResult, CfgRuleInventoryNodeEnum.FBA_USABLE.getCode());
        if (ObjectUtils.isEmpty(usable) || CollectionUtils.isEmpty(usable.getChildrenList())) {
            replenishmentResultDTO.getReplenishmentDetail().setFbaUsableQty(0);
        }
        List<String> codes = usable.getChildrenList().stream()
                .filter(v -> "true".equals(v.getValue()))
                .map(CfgRuleCommonDTO.StrategyResultDTO::getCode)
                .collect(Collectors.toList());
        int qty = inventoryService.getFbaUsable(replenishmentResultDTO, codes);
        replenishmentResultDTO.getReplenishmentDetail().setFbaUsableQty(qty);
    }

}
