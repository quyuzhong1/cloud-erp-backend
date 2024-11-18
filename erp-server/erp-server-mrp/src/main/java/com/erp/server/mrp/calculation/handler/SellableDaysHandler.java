package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleInventoryNodeEnum;
import com.erp.model.mrp.enums.RecentTimePeriodEnum;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

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
            setSellableDays(detail.getFbaUsableQty(), estimateQty, replenishmentResultDTO.getReplenishmentDetail()::setFbaSellableDays);
        }
        //海外仓可售天数 海外仓总库存 / 备货期日均销量
        if (Boolean.TRUE.equals(cfgRuleStrategyDTO.getWarehouseResult().getIsEnableOverseas())) {
            Set<String> overseasResult = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getTotalOverseasInventory());
            int totalOverseasQty = 0;
            totalOverseasQty = getTotalOverseasQty(overseasResult, totalOverseasQty, detail);
            setSellableDays(totalOverseasQty, estimateQty, replenishmentResultDTO.getReplenishmentDetail()::setOverseasSellableDays);
        }
        int totalLocalQty = 0;
        Set<String> localResult = cfgRuleCommonService.findByKey(baseKey, inventoryResult, baseKey + ":" + CfgRuleInventoryNodeEnum.getTotalLocalInventory());
        totalLocalQty = getTotalLocalQty(localResult, totalLocalQty, detail);
        setSellableDays(totalLocalQty, estimateQty, replenishmentResultDTO.getReplenishmentDetail()::setLocalSellableDays);
        //总库存可售天数 总库存 / 备货期日均销量
        if (!ObjectUtils.isEmpty(detail.getTotalInventoryQty())) {
            setSellableDays(detail.getTotalInventoryQty(), estimateQty, replenishmentResultDTO.getReplenishmentDetail()::setTotalSellableDays);
        }
    }

    /**
     * 获取本地仓总数量
     * @param localResult 配置
     * @param totalLocalQty 总数量
     * @param detail 参数
     */
    private static int getTotalLocalQty(Set<String> localResult, int totalLocalQty, ReplenishmentResultDTO.DetailDTO detail) {
        for (String code : localResult) {
            if (CfgRuleInventoryNodeEnum.TOTAL_LOCAL_USABLE.getCode().equals(code)) {
                totalLocalQty += ObjectUtils.isEmpty(detail.getLocalUsableQty()) ? 0 : detail.getLocalUsableQty();
            } else if (CfgRuleInventoryNodeEnum.TOTAL_LOCAL_IN_TRANSIT.getCode().equals(code)) {
                totalLocalQty += ObjectUtils.isEmpty(detail.getLocalInTransitQty()) ? 0 : detail.getLocalInTransitQty();
            } else if (CfgRuleInventoryNodeEnum.TOTAL_LOCAL_ESTIMATED_DELIVERY.getCode().equals(code)) {
                totalLocalQty += ObjectUtils.isEmpty(detail.getLocalPlanPurchaseQty()) ? 0 : detail.getLocalPlanPurchaseQty();
            }
        }
        return totalLocalQty;
    }

    /**
     * 获取海外总数量
     * @param overseasResult 配置
     * @param totalOverseasQty 总数量
     * @param detail 参数
     */
    private static int getTotalOverseasQty(Set<String> overseasResult, int totalOverseasQty, ReplenishmentResultDTO.DetailDTO detail) {
        for (String code : overseasResult) {
            if (CfgRuleInventoryNodeEnum.TOTAL_OVERSEAS_USABLE.getCode().equals(code)) {
                totalOverseasQty += ObjectUtils.isEmpty(detail.getOverseasUsableQty()) ? 0 : detail.getOverseasUsableQty();
            } else if (CfgRuleInventoryNodeEnum.TOTAL_OVERSEAS_IN_TRANSIT.getCode().equals(code)) {
                totalOverseasQty += ObjectUtils.isEmpty(detail.getOverseasInTransitQty()) ? 0 : detail.getOverseasUsableQty();
            } else if (CfgRuleInventoryNodeEnum.TOTAL_OVERSEAS_ESTIMATED_DELIVERY.getCode().equals(code)) {
                totalOverseasQty += ObjectUtils.isEmpty(detail.getOverseasPlanDeliveryQty()) ? 0 : detail.getOverseasUsableQty();
            }
        }
        return totalOverseasQty;
    }

    private void setSellableDays(int totalQty, BigDecimal estimateQty, IntConsumer setSellableDaysMethod) {
        setSellableDaysMethod.accept(new BigDecimal(totalQty).divide(estimateQty, 0, RoundingMode.FLOOR).intValue());
    }

}
