package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FbaInTransitHandler extends AbstractSkuCalculationHandler {
    @Resource
    private FbaPlanDeliveryHandler fbaPlanDeliveryHandler;
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
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        Set<String> codes = cfgRuleCommonService.findByKey(baseKey, replenishmentResultDTO.getCfgRuleStrategy().getInventoryResult(), baseKey + ":" + CfgRuleInventoryNodeEnum.getFbaInTransit());
        if (!CollectionUtils.isEmpty(codes)) {
            replenishmentResultDTO.getReplenishmentDetail().setCfgFbaInTransit(new ArrayList<>(codes).get(0));
        }
        ReplenishmentResultDTO.BasicDTO replenishment = replenishmentResultDTO.getReplenishment();
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = replenishmentResultDTO.getCfgRuleStrategy().getStockUpResult();
        List<ReplenishmentResultDTO.FbaInTransitDetailDTO> inTransitDetails =  replenishmentResultDTO.getInventoryDTO().getFbaInTransitList()
                        .stream().filter(v -> v.getShopId().equals(replenishment.getShopId()))
                        .filter(v -> v.getSkuId().equals(replenishment.getSkuId()))
                                .map(v -> ReplenishmentResultDTO.FbaInTransitDetailDTO.buildFbaInTransitDetailDTO(v, stockUpResult,replenishmentResultDTO.getReplenishmentDetail().getCalcVersion()))
                                        .collect(Collectors.toList());
        Integer qty = inTransitDetails.stream().map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getInTransitQty)
                .reduce(0, Math::addExact);
        replenishmentResultDTO.setFbaInTransitDetails(inTransitDetails);
        replenishmentResultDTO.getReplenishmentDetail().setFbaInTransitQty(qty);
    }
}
