package com.erp.server.mrp.calculation.handler;

import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.RecentTimePeriodEnum.*;

@Component
public class SalesEstimateHandler extends AbstractSkuCalculationHandler {
    @Resource
    private OverseasUsableHandler overseasUsableHandler;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return overseasUsableHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults = cfgRuleStrategyDTO.getSalesQtyResult().getFormulaResults();
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults = cfgRuleStrategyDTO.getSalesQtyResult().getDefaultFormulaResults();
        List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates = new ArrayList<>();
        //计算天数
        int days = cfgRuleStrategyDTO.getSettings()
                .stream().filter(v -> v.getKey().equals(CfgSettingEnum.CALCULATION_DAYS.getCode()))
                .map(CfgSettingDTO::getDataJson)
                .map(Integer::parseInt)
                .findFirst().orElse(0);
        LocalDate basicCalcDate = LocalDate.parse(replenishmentResultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        for (int i = 0; i < days; i++) {
            LocalDate calcDate = basicCalcDate.plusDays(i);
            //获取最大优先级的规则 优先取 sku 固定规则，其次sku动态规则，其次sku默认规则，取不到则取系统动态规则，其次系统默认规则
            CfgRuleSalesQtyDTO.StrategyFormulaResultDTO formulaResult = formulaResults.stream()
                    .filter(v -> !ObjectUtils.isEmpty(v.getStartDate()) && !ObjectUtils.isEmpty(v.getEndDate()) &&
                            !v.getStartDate().isAfter(calcDate) && !v.getEndDate().isBefore(calcDate))
                    .min(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getPriority)
                            .thenComparing(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getIndex).reversed()))
                    .orElseGet(() -> formulaResults.stream()
                            .filter(v -> CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(v.getType()))
                            .findFirst().orElse(defaultFormulaResults.stream()
                                    .filter(v -> !ObjectUtils.isEmpty(v.getStartDate()) && !ObjectUtils.isEmpty(v.getEndDate()) &&
                                            !v.getStartDate().isAfter(calcDate) && !v.getEndDate().isBefore(calcDate))
                                    .min(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getPriority)
                                            .thenComparing(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getIndex).reversed()))
                                    .orElseGet(() -> defaultFormulaResults.stream()
                                            .filter(v -> CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(v.getType()))
                                            .findFirst().orElse(null))));
            if (ObjectUtils.isEmpty(formulaResult)) {
                continue;
            }
            BigDecimal saleQty = getSaleQty(replenishmentResultDTO, formulaResult, basicCalcDate);
            ReplenishmentResultDTO.SalesEstimateDTO salesEstimateDTO = new ReplenishmentResultDTO.SalesEstimateDTO(calcDate, saleQty,
                    calcDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            salesEstimates.add(salesEstimateDTO);
        }
        replenishmentResultDTO.setSalesEstimates(salesEstimates);
        //开始计算分时段销量和日均预估
        calculationTimePeriodSalesEstimates(replenishmentResultDTO, basicCalcDate);
    }

    /**
     * 计算分时段销量预估
     *
     * @param replenishmentResult 参数
     * @param basicCalcDate       计算日期
     */
    private void calculationTimePeriodSalesEstimates(ReplenishmentResultDTO replenishmentResult, LocalDate basicCalcDate) {
        List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates = replenishmentResult.getSalesEstimates();
        List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> timePeriodSalesEstimates = new ArrayList<>();
        List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> avgTimePeriodSalesEstimates = new ArrayList<>();
        // 计算备货期
        List<BigDecimal> stockingData = salesEstimates.stream()
                .filter(v -> !basicCalcDate.isAfter(v.getDate()) && basicCalcDate.plusDays(replenishmentResult.getReplenishmentDetail().getStockUpDefaultDays()).isAfter(v.getDate()))
                .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                .collect(Collectors.toList());
        BigDecimal stockingSales = stockingData.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        timePeriodSalesEstimates.add(new ReplenishmentResultDTO.TimePeriodSalesEstimateDTO(STOCKING_DATE, stockingSales));
        avgTimePeriodSalesEstimates.add(new ReplenishmentResultDTO.TimePeriodSalesEstimateDTO(STOCKING_DATE,
                stockingSales.divide(BigDecimal.valueOf(replenishmentResult.getReplenishmentDetail().getStockUpDefaultDays()), 2, RoundingMode.HALF_UP)));
        // 计算当前月
        getSalesByTime(salesEstimates, timePeriodSalesEstimates, avgTimePeriodSalesEstimates, basicCalcDate, basicCalcDate.with(TemporalAdjusters.lastDayOfMonth()), CURRENT_MONTH);
        // 计算下月
        // 获取下个月的第一天
        LocalDate firstDayOfNextMonth = basicCalcDate.plusMonths(1).withDayOfMonth(1);
        // 获取下个月的最后一天
        getSalesByTime(salesEstimates, timePeriodSalesEstimates, avgTimePeriodSalesEstimates, firstDayOfNextMonth, firstDayOfNextMonth.with(TemporalAdjusters.lastDayOfMonth()), NEXT_MONTH);
        //计算下下月
        // 获取下下个月的第一天
        LocalDate firstDayOfFollowingMonth = basicCalcDate.plusMonths(2).withDayOfMonth(1);
        getSalesByTime(salesEstimates, timePeriodSalesEstimates, avgTimePeriodSalesEstimates, firstDayOfFollowingMonth, firstDayOfFollowingMonth.with(TemporalAdjusters.lastDayOfMonth()), FOLLOWING_MONTH);
        replenishmentResult.setTimePeriodSalesEstimates(timePeriodSalesEstimates);
        replenishmentResult.setAvgTimePeriodSalesEstimates(avgTimePeriodSalesEstimates);
    }


    /**
     * 分时段销量
     *
     * @param salesEstimates              销量预估
     * @param timePeriodSalesEstimates    分时段预估销量
     * @param avgTimePeriodSalesEstimates 分时段预估日销量
     * @param startDate                   开始时间
     * @param endDate                     结束时间
     * @param recentTimePeriodEnum        枚举
     */
    private void getSalesByTime(List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates, List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> timePeriodSalesEstimates,
                                List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> avgTimePeriodSalesEstimates, LocalDate startDate,
                                LocalDate endDate, RecentTimePeriodEnum recentTimePeriodEnum) {
        BigDecimal followingSales = salesEstimates.stream()
                .filter(v -> !startDate.isAfter(v.getDate()) && !endDate.isBefore(v.getDate()))
                .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        timePeriodSalesEstimates.add(new ReplenishmentResultDTO.TimePeriodSalesEstimateDTO(recentTimePeriodEnum, followingSales));
        avgTimePeriodSalesEstimates.add(new ReplenishmentResultDTO.TimePeriodSalesEstimateDTO(recentTimePeriodEnum, followingSales.divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate)), 2, RoundingMode.HALF_UP)));
    }

    /**
     * 获取预估销量
     *
     * @param salesInfos    建议历史销量
     * @param formulaResult 销量计算参数
     * @param basicCalcDate 计算时间
     */
    private BigDecimal getSaleQty(ReplenishmentResultDTO replenishmentResult, CfgRuleSalesQtyDTO.StrategyFormulaResultDTO formulaResult, LocalDate basicCalcDate) {
        BigDecimal saleQty;
        if (CfgRuleSalesFormulaTypeEnum.FIXED.getCode().equals(formulaResult.getType())) {
            saleQty = MathUtil.valueOf(formulaResult.getFixedValue());
        } else if (CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode().equals(formulaResult.getType())) {
            saleQty = getDynamicSaleQty(formulaResult.getPercentJsonDTO(), replenishmentResult, basicCalcDate);
        } else {
            if (CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getCode().equals(formulaResult.getDefaultType())) {
                saleQty = getDynamicSaleQty(formulaResult.getPercentJsonDTO(), replenishmentResult, basicCalcDate);
            } else {
                saleQty = MathUtil.valueOf(formulaResult.getFixedValue());
            }
        }
        return saleQty;
    }

    private BigDecimal getDynamicSaleQty(CfgRuleSalesFormulaDTO.PercentJsonDTO dto, ReplenishmentResultDTO replenishmentResult, LocalDate basicCalcDate) {

        List<SalesForecastCalculatorDTO> salesDataList = new ArrayList<>();
        LocalDate localDate = basicCalcDate.minusDays(1);
        List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos = replenishmentResult.getSalesInfos();
        Map<TimePeriodEnum, BigDecimal> timePeriodMap = replenishmentResult.getAvgTimePeriodSales()
                .stream().collect(Collectors.toMap(ReplenishmentResultDTO.TimePeriodSalesDTO::getCode, ReplenishmentResultDTO.TimePeriodSalesDTO::getQty, (o1, o2) -> o1));
        // 3天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.THREE), MathUtil.valueOf(dto.getThreeDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 3)));
        // 7天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.SEVEN), MathUtil.valueOf(dto.getSevenDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 7)));
        // 14天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.FOURTEEN), MathUtil.valueOf(dto.getFourteenDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 14)));
        // 30天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.THIRTY), MathUtil.valueOf(dto.getThirtyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 30)));
        // 60天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.SIXTY), MathUtil.valueOf(dto.getSixtyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 60)));
        // 90天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.NINETY), MathUtil.valueOf(dto.getNinetyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 90)));
        // 180天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.ONE_HUNDRED_AND_EIGHTY), MathUtil.valueOf(dto.getOneHundredEightyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 180)));
        // 270天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.TWO_HUNDRED_AND_SEVENTY), MathUtil.valueOf(dto.getTwoHundredSeventyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 270)));
        // 360天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.THREE_HUNDRED_AND_SIXTY), MathUtil.valueOf(dto.getThreeHundredSixtyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 360)));
        // 计算预估日销量
        return SalesForecastCalculatorDTO.calculateForecastedSales(salesDataList);
    }

    private boolean getIsExcluded(List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos, LocalDate basicCalcDate, int days) {
        return salesInfos.stream()
                .filter(v -> !basicCalcDate.minusDays(days).isAfter(v.getDate()) && basicCalcDate.isAfter(v.getDate()))
                .allMatch(v -> Boolean.TRUE.equals(v.getIsIgnoreOutOfStock()) || CfgRuleSalesDenoisingDenoisingTypeEnum.COMPLETELY.getCode().equals(v.getDenoisingType()));
    }

}
