package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.RecentSuggestionEnum;
import com.erp.model.mrp.enums.SuggestedMarkTypeEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class RecentSuggestionHandler extends AbstractSkuCalculationHandler {
    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO t, ReplenishmentResultDTO r) {
        return null;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO t, ReplenishmentResultDTO r) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO resultDTO) {
        LocalDate now = LocalDate.parse(resultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        List<ReplenishmentResultDTO.RecentSuggestionDTO> recentSuggestions = new ArrayList<>();
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = cfgRuleStrategyDTO.getStockUpResult();
        //获取最近断货日
        LocalDate startDate = resultDTO.getRptOutOfStocks()
                .stream().map(ReplenishmentResultDTO.RptOutOfStockDTO::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(null);
        int outOfStockDays = ObjectUtils.isEmpty(startDate) ? Integer.MAX_VALUE : (int) ChronoUnit.DAYS.between(now, startDate);
        if (!ObjectUtils.isEmpty(startDate)) {
            int days = outOfStockDays;
            String markType;
            if (outOfStockDays <= 0) {
                days = Math.abs(outOfStockDays);
                markType = SuggestedMarkTypeEnum.OUT_OF_STOCK.getCode();
            } else if (outOfStockDays <= resultDTO.getReplenishmentDetail().getStockUpMinDays()) {
                markType = SuggestedMarkTypeEnum.WILL_OUT_OF_STOCK.getCode();
            } else if (outOfStockDays <= resultDTO.getReplenishmentDetail().getStockUpMaxDays()) {
                markType = SuggestedMarkTypeEnum.RISKS.getCode();
            } else {
                markType = SuggestedMarkTypeEnum.NORMAL.getCode();
            }
            ReplenishmentResultDTO.RecentSuggestionDTO dto = ReplenishmentResultDTO.RecentSuggestionDTO.buildRecentSuggestion(RecentSuggestionEnum.RECENT_OUT_OF_STOCK.getCode(), days, markType, null, startDate);
            recentSuggestions.add(dto);
        }
        //获取最近发货日
        ReplenishmentResultDTO.DeliverySuggestDTO deliverySuggestDTO = resultDTO.getDeliverySuggests()
                .stream()
                .min(Comparator.comparing(ReplenishmentResultDTO.DeliverySuggestDTO::getSuggestDeliveryDate))
                .orElse(null);
        if (!ObjectUtils.isEmpty(deliverySuggestDTO)) {
            int days = (int) ChronoUnit.DAYS.between(now, deliverySuggestDTO.getSuggestDeliveryDate());
            String markType;
            if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(resultDTO.getReplenishment().getPlatformType()) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(resultDTO.getReplenishment().getPlatformType())) {
                //最短发货时长 = 本地发FBA时效 + FBA入库天数
                //最长发货时长 = 本地发FBA时效 + FBA入库天数 + 发货频率 + FBA安全天数
                //最短发货时长 = 本地发海外时效 + 海外仓入库天数
                //最长发货时长 = 本地发海外时效 + 海外仓入库天数 + 发货频率 + 海外仓安全天数
                if (outOfStockDays < 0) {
                    days = Math.abs(outOfStockDays);
                    markType = SuggestedMarkTypeEnum.OUT_OF_STOCK_PURCHASE.getCode();
                } else if (days > 0 && days <= (stockUpResult.getLogisticsResult().getLogisticsDays() + stockUpResult.getInstockDays())) {
                    markType = SuggestedMarkTypeEnum.WILL_OUT_OF_STOCK_DELIVERY.getCode();
                } else if (days <= (stockUpResult.getLogisticsResult().getLogisticsDays() + stockUpResult.getInstockDays() + stockUpResult.getLogisticsResult().getLogisticsCycleDays() + stockUpResult.getSafeDays())) {
                    markType = SuggestedMarkTypeEnum.RISKS_DELIVERY.getCode();
                } else {
                    markType = SuggestedMarkTypeEnum.NORMAL_DELIVERY.getCode();
                }
                ReplenishmentResultDTO.RecentSuggestionDTO dto = ReplenishmentResultDTO.RecentSuggestionDTO.buildRecentSuggestion(RecentSuggestionEnum.RECENT_DELIVERY.getCode(), days, markType, deliverySuggestDTO.getSuggestDeliveryQty(), deliverySuggestDTO.getSuggestDeliveryDate());
                recentSuggestions.add(dto);
            }
            if (CfgRulePlatformTypeEnum.B2B.getCode().equals(resultDTO.getReplenishment().getPlatformType()) || CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(resultDTO.getReplenishment().getPlatformType())) {
                if (outOfStockDays < 0) {
                    days = Math.abs(outOfStockDays);
                    markType = SuggestedMarkTypeEnum.OUT_OF_STOCK_PURCHASE.getCode();
                } else if (days > 0 && days <= stockUpResult.getSafeDays()) {
                    markType = SuggestedMarkTypeEnum.RISKS_DELIVERY.getCode();
                } else {
                    markType = SuggestedMarkTypeEnum.NORMAL_DELIVERY.getCode();
                }
                ReplenishmentResultDTO.RecentSuggestionDTO dto = ReplenishmentResultDTO.RecentSuggestionDTO.buildRecentSuggestion(RecentSuggestionEnum.RECENT_DELIVERY.getCode(), days, markType, deliverySuggestDTO.getSuggestDeliveryQty(), deliverySuggestDTO.getSuggestDeliveryDate());
                recentSuggestions.add(dto);
            }
        }

        //获取最近采购日
        ReplenishmentResultDTO.PurchaseSuggestDTO purchaseSuggestDTO = resultDTO.getPurchaseSuggests()
                .stream()
                .min(Comparator.comparing(ReplenishmentResultDTO.PurchaseSuggestDTO::getSuggestPurchaseDate))
                .orElse(null);
        if (!ObjectUtils.isEmpty(purchaseSuggestDTO)) {
            //最短采购时长 = 供应商发货时长 + 质检入库时长
            //最长采购时长 = 采购审批时长 + 生产周期 + 供应商发货时长 + 质检入库时长 + 采购频率
            int days = (int) ChronoUnit.DAYS.between(now, purchaseSuggestDTO.getSuggestPurchaseDate());
            String markType;
            if (outOfStockDays < 0) {
                days = Math.abs(outOfStockDays);
                markType = SuggestedMarkTypeEnum.OUT_OF_STOCK_PURCHASE.getCode();
            } else if (days > 0 && days <= (stockUpResult.getSupplierDeliveryDays() + stockUpResult.getQcDays())) {
                markType = SuggestedMarkTypeEnum.WILL_OUT_OF_STOCK_PURCHASE.getCode();
            } else if (days <= (stockUpResult.getPurchaseApproveDays() + stockUpResult.getProductionDays() + stockUpResult.getSupplierDeliveryDays() + stockUpResult.getQcDays() + stockUpResult.getPurchaseCycleDays())) {
                markType = SuggestedMarkTypeEnum.RISKS_PURCHASE.getCode();
            } else {
                markType = SuggestedMarkTypeEnum.NORMAL_PURCHASE.getCode();
            }
            ReplenishmentResultDTO.RecentSuggestionDTO dto = ReplenishmentResultDTO.RecentSuggestionDTO.buildRecentSuggestion(RecentSuggestionEnum.RECENT_PURCHASE.getCode(), days, markType, purchaseSuggestDTO.getSuggestPurchaseQty(), purchaseSuggestDTO.getSuggestPurchaseDate());
            recentSuggestions.add(dto);
        }
        resultDTO.setRecentSuggestions(recentSuggestions);
    }
}
