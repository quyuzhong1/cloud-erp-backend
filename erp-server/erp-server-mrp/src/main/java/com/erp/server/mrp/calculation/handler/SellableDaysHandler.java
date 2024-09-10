package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.RecentTimePeriodEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class SellableDaysHandler extends AbstractSkuCalculationHandler {
    @Resource
    private RptOutOfStockHandler rptOutOfStockHandler;
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
        List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> estimates = replenishmentResultDTO.getTimePeriodSalesEstimates();
        ReplenishmentResultDTO.TimePeriodSalesEstimateDTO salesEstimate = estimates.stream()
                .filter(v -> v.getCode().equals(RecentTimePeriodEnum.STOCKING_DATE))
                .findFirst().orElse(null);
        if (ObjectUtils.isEmpty(salesEstimate)) {
            return;
        }
        //FBA可售天数 FBA可用 / 备货期日均销量
        if (!ObjectUtils.isEmpty(detail.getFbaUsableQty())) {
            replenishmentResultDTO.getReplenishmentDetail().setFbaSellableDays(new BigDecimal(detail.getFbaUsableQty())
                    .divide(salesEstimate.getQty(), 0, RoundingMode.FLOOR).intValue());
        }
        //海外仓可售天数 海外仓总库存 / 备货期日均销量
        if (!ObjectUtils.isEmpty(detail.getOverseasUsableQty())) {
            replenishmentResultDTO.getReplenishmentDetail().setOverseasSellableDays(new BigDecimal(detail.getOverseasUsableQty())
                    .divide(salesEstimate.getQty(), 0, RoundingMode.FLOOR).intValue());
        }
        //本地可售天数 本地总库存 / 备货期日均销量
        if (!ObjectUtils.isEmpty(detail.getLocalUsableQty())) {
            replenishmentResultDTO.getReplenishmentDetail().setLocalSellableDays(new BigDecimal(detail.getLocalUsableQty())
                    .divide(salesEstimate.getQty(), 0, RoundingMode.FLOOR).intValue());
        }
        //总库存可售天数 总库存 / 备货期日均销量
        if (!ObjectUtils.isEmpty(detail.getTotalInventoryQty())) {
            replenishmentResultDTO.getReplenishmentDetail().setTotalSellableDays(new BigDecimal(detail.getTotalInventoryQty())
                    .divide(salesEstimate.getQty(), 0, RoundingMode.FLOOR).intValue());
        }
    }
}
