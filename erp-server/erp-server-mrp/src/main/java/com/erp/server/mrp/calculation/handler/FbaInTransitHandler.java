package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
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
public class FbaInTransitHandler extends AbstractSkuCalculationHandler {
    @Resource
    private FbaPlanDeliveryHandler fbaPlanDeliveryHandler;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;


    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return fbaPlanDeliveryHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO replenishmentResultDTO) {
        return CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType());
    }

    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {
        //获取需要计算库存的FBA在途配置
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = replenishmentResultDTO.getCfgRuleStrategy().getInventoryResult();
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        Set<String> codes = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getFbaInTransit());
        if (CollectionUtils.isEmpty(codes)) {
            replenishmentResultDTO.getReplenishmentDetail().setFbaInTransitQty(0);
        }
        List<String> code = new ArrayList<>(codes);
        replenishmentResultDTO.getReplenishmentDetail().setCfgFbaInTransit(code.get(0));
        int qty = inventoryService.getFbaInTransit(replenishmentResultDTO, code.get(0), replenishmentResultDTO.getCfgRuleStrategy().getStockUpResult());
        replenishmentResultDTO.getReplenishmentDetail().setFbaInTransitQty(qty);
    }
}
