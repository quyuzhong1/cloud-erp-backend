package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.RecentTimePeriodEnum;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Component
public class SellableDaysHandler extends AbstractSkuCalculationHandler {
    @Resource
    private RptOutOfStockHandler rptOutOfStockHandler;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return rptOutOfStockHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        ReplenishmentResultDTO.DetailDTO detail = replenishmentResultDTO.getReplenishmentDetail();
        List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> estimates = replenishmentResultDTO.getAvgTimePeriodSalesEstimates();
        String baseKey = CfgRuleCommonTypeEnum.getBaseInventoryRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = cfgRuleStrategyDTO.getInventoryResult();
        ReplenishmentResultDTO.TimePeriodSalesEstimateDTO salesEstimate = estimates.stream()
                .filter(v -> v.getCode().equals(RecentTimePeriodEnum.STOCKING_DATE))
                .findFirst().orElse(null);
        if (ObjectUtils.isEmpty(salesEstimate)) {
            return;
        }
        BigDecimal estimateQty = (salesEstimate.getQty().compareTo(BigDecimal.ZERO) == 0) ? BigDecimal.ONE : salesEstimate.getQty();
        //FBA可售天数 FBA可用 / 备货期日均销量
        if (!ObjectUtils.isEmpty(detail.getFbaUsableQty())) {
            replenishmentResultDTO.getReplenishmentDetail().setFbaSellableDays(new BigDecimal(detail.getFbaUsableQty())
                    .divide(estimateQty, 0, RoundingMode.FLOOR).intValue());
        }
        //海外仓可售天数 海外仓总库存 / 备货期日均销量
        if (Boolean.TRUE.equals(cfgRuleStrategyDTO.getWarehouseResult().getIsEnableOverseas())) {
            Set<String> overseasResult = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getTotalOverseasInventory());
            int totalOverseasQty = 0;
            for (String code : overseasResult) {
                if (CfgRuleInventoryNodeEnum.TOTAL_OVERSEAS_USABLE.getCode().equals(code)) {
                    totalOverseasQty += ObjectUtils.isEmpty(detail.getOverseasUsableQty()) ? 0 : detail.getOverseasUsableQty();
                } else if (CfgRuleInventoryNodeEnum.TOTAL_OVERSEAS_IN_TRANSIT.getCode().equals(code)) {
                    totalOverseasQty += ObjectUtils.isEmpty(detail.getOverseasInTransitQty()) ? 0 : detail.getOverseasInTransitQty();
                } else if (CfgRuleInventoryNodeEnum.TOTAL_OVERSEAS_ESTIMATED_DELIVERY.getCode().equals(code)) {
                    totalOverseasQty += ObjectUtils.isEmpty(detail.getOverseasPlanDeliveryQty()) ? 0 : detail.getOverseasPlanDeliveryQty();
                }
            }
            replenishmentResultDTO.getReplenishmentDetail().setOverseasSellableDays(new BigDecimal(totalOverseasQty)
                    .divide(estimateQty, 0, RoundingMode.FLOOR).intValue());
        }
        int totalLocalQty = 0;
        Set<String> localResult = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getTotalLocalInventory());
        for (String code : localResult) {
            if (CfgRuleInventoryNodeEnum.TOTAL_LOCAL_USABLE.getCode().equals(code)) {
                totalLocalQty += ObjectUtils.isEmpty(detail.getLocalUsableQty()) ? 0 : detail.getLocalUsableQty();
            } else if (CfgRuleInventoryNodeEnum.TOTAL_LOCAL_IN_TRANSIT.getCode().equals(code)) {
                totalLocalQty += ObjectUtils.isEmpty(detail.getLocalInTransitQty()) ? 0 : detail.getLocalInTransitQty();
            } else if (CfgRuleInventoryNodeEnum.TOTAL_LOCAL_ESTIMATED_DELIVERY.getCode().equals(code)) {
                totalLocalQty += ObjectUtils.isEmpty(detail.getLocalPlanPurchaseQty()) ? 0 : detail.getLocalPlanPurchaseQty();
            }
        }
        replenishmentResultDTO.getReplenishmentDetail().setLocalSellableDays(new BigDecimal(totalLocalQty)
                .divide(estimateQty, 0, RoundingMode.FLOOR).intValue());
        //总库存可售天数 总库存 / 备货期日均销量
        if (!ObjectUtils.isEmpty(detail.getTotalInventoryQty())) {
            replenishmentResultDTO.getReplenishmentDetail().setTotalSellableDays(new BigDecimal(detail.getTotalInventoryQty())
                    .divide(estimateQty, 0, RoundingMode.FLOOR).intValue());
        }
    }
}
