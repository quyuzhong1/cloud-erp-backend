package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum.*;

@Component
public class TotalInventoryHandler extends AbstractSkuCalculationHandler {
    @Resource
    private SellableDaysHandler sellableDaysHandler;

    @Resource
    private CfgRuleCommonService cfgRuleCommonService;

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
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        //计算FBA的库存
        totalQty = getFBATotalQty(replenishmentResultDTO, inventoryResult, baseKey, totalQty);
        //计算海外仓的库存
        totalQty = getTotalQty(replenishmentResultDTO,cfgRuleStrategyDTO.getWarehouseResult().getIsEnableOverseas(), inventoryResult, baseKey, totalQty);
        //计算本地的库存
        totalQty = getLocalTotalQty(replenishmentResultDTO, inventoryResult, baseKey, totalQty);
        replenishmentResultDTO.getReplenishmentDetail().setTotalInventoryQty(totalQty);
    }

    private int getLocalTotalQty(ReplenishmentResultDTO replenishmentResultDTO, List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, int totalQty) {
        Set<String> localResult = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getTotalLocalInventory());
        if (!ObjectUtils.isEmpty(localResult)) {
            if (localResult.contains(TOTAL_LOCAL_USABLE.getCode()) && !ObjectUtils.isEmpty(replenishmentResultDTO.getReplenishmentDetail().getLocalUsableQty())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getLocalUsableQty();
            }
            if (localResult.contains(TOTAL_LOCAL_IN_TRANSIT.getCode()) && !ObjectUtils.isEmpty(replenishmentResultDTO.getReplenishmentDetail().getLocalInTransitQty())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getLocalInTransitQty();
            }
            if (localResult.contains(TOTAL_LOCAL_ESTIMATED_DELIVERY.getCode()) && !ObjectUtils.isEmpty(replenishmentResultDTO.getReplenishmentDetail().getLocalPlanPurchaseQty())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getLocalPlanPurchaseQty();
            }
        }
        return totalQty;
    }

    private int getTotalQty(ReplenishmentResultDTO replenishmentResultDTO,Boolean isEnableOverseas, List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, int totalQty) {
        Set<String> overseasResult = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getTotalOverseasInventory());
        if (!ObjectUtils.isEmpty(overseasResult) && Boolean.TRUE.equals(isEnableOverseas)) {
            if (overseasResult.contains(TOTAL_OVERSEAS_USABLE.getCode()) && !ObjectUtils.isEmpty(replenishmentResultDTO.getReplenishmentDetail().getOverseasUsableQty())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getOverseasUsableQty();
            }
            if (overseasResult.contains(TOTAL_OVERSEAS_IN_TRANSIT.getCode()) && !ObjectUtils.isEmpty(replenishmentResultDTO.getReplenishmentDetail().getOverseasInTransitQty())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getOverseasInTransitQty();
            }
            if (overseasResult.contains(TOTAL_OVERSEAS_ESTIMATED_DELIVERY.getCode()) && !ObjectUtils.isEmpty(replenishmentResultDTO.getReplenishmentDetail().getOverseasPlanDeliveryQty())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getOverseasPlanDeliveryQty();
            }
        }
        return totalQty;
    }

    private int getFBATotalQty(ReplenishmentResultDTO replenishmentResultDTO, List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult, String baseKey, int totalQty) {
        Set<String> fbaResult = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getTotalFbaInventory());
        if (!ObjectUtils.isEmpty(fbaResult)) {
            if (fbaResult.contains(TOTAL_FBA_USABLE.getCode()) && !ObjectUtils.isEmpty(replenishmentResultDTO.getReplenishmentDetail().getFbaUsableQty())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getFbaUsableQty();
            }
            if (fbaResult.contains(TOTAL_FBA_IN_TRANSIT.getCode()) && !ObjectUtils.isEmpty(replenishmentResultDTO.getReplenishmentDetail().getFbaInTransitQty())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getFbaInTransitQty();
            }
            if (fbaResult.contains(TOTAL_FBA_ESTIMATED_DELIVERY.getCode()) && !ObjectUtils.isEmpty(replenishmentResultDTO.getReplenishmentDetail().getFbaPlanDeliveryQty())) {
                totalQty += replenishmentResultDTO.getReplenishmentDetail().getFbaPlanDeliveryQty();
            }
        }
        return totalQty;
    }
}
