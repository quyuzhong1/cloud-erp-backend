package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.calculation.utils.TreeUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum.*;

@Component
public class TotalInventoryHandler extends AbstractSkuCalculationHandler {
    @Resource
    private SellableDaysHandler sellableDaysHandler;
    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return sellableDaysHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        int totalQty = 0;
        CfgRuleCommonDTO.StrategyResultDTO inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        CfgRuleCommonDTO.StrategyResultDTO totalResult = TreeUtils.findByCode(inventoryResult, TOTAL_INVENTORY.getCode());
        //计算FBA的库存
        totalQty = getFBATotalQty(replenishmentResultDTO, totalResult, totalQty);
        //计算海外仓的库存
        totalQty = getTotalQty(replenishmentResultDTO, totalResult, totalQty);
        //计算本地的库存
        totalQty = getLocalTotalQty(replenishmentResultDTO, totalResult, totalQty);
        replenishmentResultDTO.getReplenishmentDetail().setTotalInventoryQty(totalQty);
    }

    private static int getLocalTotalQty(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleCommonDTO.StrategyResultDTO totalResult, int totalQty) {
        CfgRuleCommonDTO.StrategyResultDTO localResult = totalResult.getChildrenList().stream()
                .filter(v -> TOTAL_LOCAL_INVENTORY.getCode().equals(v.getCode()))
                .findFirst()
                .orElse(null);
        if (!ObjectUtils.isEmpty(localResult)) {
            List<String> codes = localResult.getChildrenList().stream().filter(v -> "true".equals(v.getValue())).map(CfgRuleCommonDTO.StrategyResultDTO::getCode).collect(Collectors.toList());
            if (codes.contains(LOCAL_USABLE.getCode())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getLocalUsableQty();
            }
            if (codes.contains(LOCAL_IN_TRANSIT.getCode())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getLocalInTransitQty();
            }
            if (codes.contains(LOCAL_PURCHASE_PLAN.getCode())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getLocalPlanPurchaseQty();
            }
        }
        return totalQty;
    }

    private static int getTotalQty(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleCommonDTO.StrategyResultDTO totalResult, int totalQty) {
        CfgRuleCommonDTO.StrategyResultDTO overseasResult = totalResult.getChildrenList().stream()
                .filter(v -> TOTAL_OVERSEAS_INVENTORY.getCode().equals(v.getCode()))
                .findFirst()
                .orElse(null);
        if (!ObjectUtils.isEmpty(overseasResult)) {
            List<String> codes = overseasResult.getChildrenList().stream().filter(v -> "true".equals(v.getValue())).map(CfgRuleCommonDTO.StrategyResultDTO::getCode).collect(Collectors.toList());
            if (codes.contains(OVERSEAS_USABLE.getCode())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getOverseasUsableQty();
            }
            if (codes.contains(OVERSEAS_IN_TRANSIT.getCode())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getOverseasInTransitQty();
            }
            if (codes.contains(OVERSEAS_ESTIMATED_DELIVERY.getCode())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getOverseasPlanDeliveryQty();
            }
        }
        return totalQty;
    }

    private static int getFBATotalQty(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleCommonDTO.StrategyResultDTO totalResult, int totalQty) {
        CfgRuleCommonDTO.StrategyResultDTO fbaResult = totalResult.getChildrenList().stream()
                .filter(v -> TOTAL_FBA_INVENTORY.getCode().equals(v.getCode()))
                .findFirst()
                .orElse(null);
        if (!ObjectUtils.isEmpty(fbaResult)) {
            List<String> codes = fbaResult.getChildrenList().stream().filter(v -> "true".equals(v.getValue())).map(CfgRuleCommonDTO.StrategyResultDTO::getCode).collect(Collectors.toList());
            if (codes.contains(FBA_USABLE.getCode())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getFbaUsableQty();
            }
            if (codes.contains(FBA_IN_TRANSIT.getCode())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getFbaInTransitQty();
            }
            if (codes.contains(FBA_ESTIMATED_DELIVERY.getCode())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getFbaPlanDeliveryQty();
            }
        }
        return totalQty;
    }
}
