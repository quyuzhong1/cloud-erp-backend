package com.erp.server.mrp.calculation.handler;

import com.common.business.config.DocNoGenHelper;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.mrp.enums.CfgSettingEnum;
import com.erp.model.wms.enums.ExecutionTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.BusinessNoTypeEnum.CODE_FHJY;

@Component
public class DeliverySuggestHandler extends AbstractSkuCalculationHandler {
    @Resource
    private PurchaseSuggestHandler purchaseSuggestHandler;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return purchaseSuggestHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        List<ReplenishmentResultDTO.RptOutOfStockDTO> rptOutOfStocks = replenishmentResultDTO.getRptOutOfStocks();
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = cfgRuleStrategyDTO.getStockUpResult();
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult = stockUpResult.getLogisticsResult();
        //计算天数
        int days = cfgRuleStrategyDTO.getSettings()
                .stream().filter(v -> v.getKey().equals(CfgSettingEnum.CALCULATION_DAYS.getCode()))
                .map(CfgSettingDTO::getDataJson)
                .map(Integer::parseInt)
                .findFirst().orElse(Integer.MAX_VALUE);
        // 根据新品/常规品获取默认补货系数
        BigDecimal stockingRatio = CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode().equals(replenishmentResultDTO.getReplenishmentDetail().getSkuType()) ? stockUpResult.getStockingRatio() : stockUpResult.getNewStockingRatio();
        List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates = replenishmentResultDTO.getSalesEstimates();
        List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResults = stockUpResult.getStockingRatioResults();
        LocalDate now = LocalDate.parse(replenishmentResultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        //处理连续断货数据
        List<LocalDate> dates = rptOutOfStocks.stream().map(ReplenishmentResultDTO.RptOutOfStockDTO::getStartDate).distinct().collect(Collectors.toList());
        List<ReplenishmentResultDTO.DeliverySuggestDTO> deliverySuggests = dates.parallelStream()
                .map(localDate -> {
                    String code = docNoGenHelper.generateCode(CODE_FHJY);
                    ReplenishmentResultDTO.DeliverySuggestDTO suggestDTO = ReplenishmentResultDTO.DeliverySuggestDTO.buildDeliverySuggestDTO(code, logisticsResult, replenishmentResultDTO.getReplenishmentDetail().getDetailId(), ExecutionTypeEnum.AUTO.getCode());
                    if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                            || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                        //建议发货日期（本地发FBA）= 断货日期 -（本地发FBA时效 + FBA入库时间 + 本地仓发货频率 + FBA安全天数）；若建议发货日期＜当前日期，取当前日期
                        //建议发货日期（本地发海外）= 断货日期 -（本地发海外时效 + 海外仓入库时间 + 本地仓发货频率 + 海外仓安全天数）；若建议发货日期＜当前日期，取当前日期
                        LocalDate suggestDeliveryDate = localDate.minusDays(logisticsResult.getLogisticsDays())
                                .minusDays(stockUpResult.getInstockDays())
                                .minusDays(logisticsResult.getLogisticsCycleDays())
                                .minusDays(stockUpResult.getSafeDays());
                        suggestDeliveryDate = suggestDeliveryDate.isBefore(now) ? now : suggestDeliveryDate;
                        suggestDTO.setSuggestDeliveryDate(suggestDeliveryDate);
                        //预计可售日期（本地发FBA）= 建议发货日 + 本地发FBA时效 + FBA入库时间
                        //预计可售日期（本地发海外）= 建议发货日 + 本地发海外时效 + 海外仓入库时间
                        LocalDate estimateSalesDate = suggestDeliveryDate.plusDays(logisticsResult.getLogisticsDays()).plusDays(stockUpResult.getInstockDays());
                        suggestDTO.setEstimateSalesDate(estimateSalesDate);
                        //建议发货量= 预估日销*备货系数(累加 本地仓发货时效 + 本地仓发货频率 + FBA安全天数) - ( FBA库存 + FBA在途 )
                        //建议发货量= 预估日销*备货系数(累加 本地仓发货时效 + 本地仓发货频率 + 海外备货安全天数) - ( 海外仓可用 + 海外仓在途)
                        int calculationDays = logisticsResult.getLogisticsDays() + logisticsResult.getLogisticsCycleDays() + stockUpResult.getSafeDays();
                        calculationDays = Math.min(calculationDays, days);
                        int suggestDeliveryQty = getSuggestDeliveryQty(calculationDays, salesEstimates, stockingRatioResults, stockingRatio, now);
                        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                            suggestDeliveryQty = suggestDeliveryQty - replenishmentResultDTO.getReplenishmentDetail().getFbaUsableQty() - replenishmentResultDTO.getReplenishmentDetail().getFbaInTransitQty();
                        } else {
                            suggestDeliveryQty = suggestDeliveryQty - replenishmentResultDTO.getReplenishmentDetail().getOverseasUsableQty() - replenishmentResultDTO.getReplenishmentDetail().getOverseasInTransitQty();
                        }
                        suggestDTO.setSuggestDeliveryQty(Math.max(0, suggestDeliveryQty));
                    }
                    if (CfgRulePlatformTypeEnum.B2B.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                            || CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                        //建议备货日期（本地备货）= 断货日期 - 本地备货安全天数；若建议发货日期＜当前日期，取当前日期。试算时，以“建议备货日” 到货
                        LocalDate suggestDeliveryDate = localDate.minusDays(stockUpResult.getSafeDays());
                        suggestDeliveryDate = suggestDeliveryDate.isBefore(now) ? now : suggestDeliveryDate;
                        suggestDTO.setSuggestDeliveryDate(suggestDeliveryDate);
                        //建议发货量= 预估日销 * 备货系数 (累加本地备货安全天数)  - 本地可用
                        int calculationDays = Math.min(stockUpResult.getSafeDays(), days);
                        suggestDTO.setSuggestDeliveryQty(Math.max(0, getSuggestDeliveryQty(calculationDays, salesEstimates, stockingRatioResults, stockingRatio, now) - replenishmentResultDTO.getReplenishmentDetail().getLocalUsableQty()));
                    }
                    return suggestDTO;
                }).collect(Collectors.toList());
        replenishmentResultDTO.setDeliverySuggests(deliverySuggests);
    }


    /**
     * 获取建议发货量
     *
     * @param calculationDays      计算天数
     * @param salesEstimates       预估销量
     * @param stockingRatioResults 补货系数
     * @param now                  计算开始日期
     */
    private int getSuggestDeliveryQty(int calculationDays, List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates, List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResults, BigDecimal stockingRatio, LocalDate now) {
        BigDecimal totalSaleQty = BigDecimal.ZERO;
        for (int i = 0; i <= calculationDays; i++) {
            LocalDate date = now.plusDays(i);
            //获取销量
            BigDecimal saleQty = salesEstimates.parallelStream()
                    .filter(v -> v.getDate().equals(date))
                    .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            BigDecimal ratio = stockingRatioResults.parallelStream()
                    .filter(v -> !v.getStartDate().isAfter(date) && !v.getEndDate().isBefore(date))
                    .max(Comparator.comparing(CfgRuleStockingRatioDTO.StockingRatioResultDTO::getIndex))
                    .map(CfgRuleStockingRatioDTO.StockingRatioResultDTO::getStockingRatio)
                    .orElse(stockingRatio);
            totalSaleQty = totalSaleQty.add(saleQty.multiply(ratio));
        }
        return totalSaleQty.setScale(2, RoundingMode.CEILING).intValue();
    }

}
