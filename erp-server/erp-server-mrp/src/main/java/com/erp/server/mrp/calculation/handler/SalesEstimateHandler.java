package com.erp.server.mrp.calculation.handler;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
            //获取最大优先级的规则
            CfgRuleSalesQtyDTO.StrategyFormulaResultDTO formulaResult = formulaResults.stream()
                    .filter(v -> !v.getStartDate().isAfter(calcDate) && !v.getEndDate().isBefore(calcDate))
                    .min(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getPriority)
                            .thenComparing(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getIndex).reversed()))
                    .orElseGet(() -> formulaResults.stream()
                            .filter(v -> CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(v.getType()))
                            .findFirst().orElse(null));
            if (ObjectUtils.isEmpty(formulaResult)) {
                continue;
            }
            BigDecimal saleQty = getSaleQty(replenishmentResultDTO.getSalesInfos(), formulaResult, basicCalcDate);
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
     * @param basicCalcDate 计算日期
     */
    private void calculationTimePeriodSalesEstimates(ReplenishmentResultDTO replenishmentResult, LocalDate basicCalcDate) {
        List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates = replenishmentResult.getSalesEstimates();
        List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> timePeriodSalesEstimates = new ArrayList<>();
        List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> avgTimePeriodSalesEstimates = new ArrayList<>();
        // 计算备货期
        List<BigDecimal> stockingData = salesEstimates.stream()
                .filter(v -> !basicCalcDate.isAfter(v.getDate()) && !basicCalcDate.plusDays(replenishmentResult.getReplenishmentDetail().getDeliveryDefaultDays()).isBefore(v.getDate()))
                .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                .collect(Collectors.toList());
        BigDecimal stockingSales = stockingData.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        timePeriodSalesEstimates.add(new ReplenishmentResultDTO.TimePeriodSalesEstimateDTO(STOCKING_DATE, stockingSales));
        avgTimePeriodSalesEstimates.add(new ReplenishmentResultDTO.TimePeriodSalesEstimateDTO(STOCKING_DATE,
                stockingSales.divide(BigDecimal.valueOf(replenishmentResult.getReplenishmentDetail().getDeliveryDefaultDays()), 2 , RoundingMode.HALF_UP)));
        // 计算当前月
        getSalesByTime(salesEstimates, timePeriodSalesEstimates, avgTimePeriodSalesEstimates, basicCalcDate, basicCalcDate, CURRENT_MONTH);
        // 计算下月
        // 获取下个月的第一天
        LocalDate firstDayOfNextMonth = basicCalcDate.plusMonths(1).withDayOfMonth(1);
        // 获取下个月的最后一天
        getSalesByTime(salesEstimates, timePeriodSalesEstimates, avgTimePeriodSalesEstimates, firstDayOfNextMonth, firstDayOfNextMonth, NEXT_MONTH);
        //计算下下月
        // 获取下下个月的第一天
        LocalDate firstDayOfFollowingMonth = basicCalcDate.plusMonths(2).withDayOfMonth(1);
        // 获取下下个月的最后一天
        getSalesByTime(salesEstimates, timePeriodSalesEstimates, avgTimePeriodSalesEstimates, firstDayOfNextMonth, firstDayOfFollowingMonth, FOLLOWING_MONTH);
    }

    /**
     *
     * @param salesEstimates
     * @param timePeriodSalesEstimates
     * @param avgTimePeriodSalesEstimates
     * @param firstDayOfNextMonth
     * @param firstDayOfFollowingMonth
     * @param recentTimePeriodEnum
     */
    private void getSalesByTime(List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates, List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> timePeriodSalesEstimates, List<ReplenishmentResultDTO.TimePeriodSalesEstimateDTO> avgTimePeriodSalesEstimates, LocalDate firstDayOfNextMonth, LocalDate firstDayOfFollowingMonth, RecentTimePeriodEnum recentTimePeriodEnum) {
        LocalDate lastDayOfFollowingMonth = firstDayOfNextMonth.withDayOfMonth(firstDayOfNextMonth.lengthOfMonth());
        BigDecimal followingSales = salesEstimates.stream()
                .filter(v -> !firstDayOfFollowingMonth.isAfter(v.getDate()) && !lastDayOfFollowingMonth.isBefore(v.getDate()))
                .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        timePeriodSalesEstimates.add(new ReplenishmentResultDTO.TimePeriodSalesEstimateDTO(recentTimePeriodEnum, followingSales));
        avgTimePeriodSalesEstimates.add(new ReplenishmentResultDTO.TimePeriodSalesEstimateDTO(recentTimePeriodEnum, followingSales.divide(BigDecimal.valueOf( ChronoUnit.DAYS.between(firstDayOfFollowingMonth, lastDayOfFollowingMonth)), 2 , RoundingMode.HALF_UP)));
    }

    /**
     * 获取预估销量
     *
     * @param salesInfos    建议历史销量
     * @param formulaResult 销量计算参数
     * @param basicCalcDate 计算时间
     */
    private BigDecimal getSaleQty(List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos, CfgRuleSalesQtyDTO.StrategyFormulaResultDTO formulaResult, LocalDate basicCalcDate) {
        BigDecimal saleQty;
        if (CfgRuleSalesFormulaTypeEnum.FIXED.getCode().equals(formulaResult.getType())) {
            saleQty = formulaResult.getFixedValue();
        } else if (CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode().equals(formulaResult.getType())) {
            saleQty = getDynamicSaleQty(formulaResult.getPercentJsonDTO(), salesInfos, basicCalcDate);
        } else {
            if (CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getCode().equals(formulaResult.getDefaultType())) {
                saleQty = getDynamicSaleQty(formulaResult.getPercentJsonDTO(), salesInfos, basicCalcDate);
            } else {
                saleQty = formulaResult.getFixedValue();
            }
        }
        return saleQty;
    }

    private BigDecimal getDynamicSaleQty(CfgRuleSalesFormulaDTO.PercentJsonDTO dto, List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos, LocalDate basicCalcDate) {

        List<SalesForecastCalculatorDTO> salesDataList = new ArrayList<>();
        // 3天日均
        salesDataList.add(new SalesForecastCalculatorDTO(getSaleQtyByDay(salesInfos, basicCalcDate, 3), dto.getThreeDaysRatio(), getIsExcluded(salesInfos, basicCalcDate, 3)));
        // 7天日均
        salesDataList.add(new SalesForecastCalculatorDTO(getSaleQtyByDay(salesInfos, basicCalcDate, 7), dto.getSevenDaysRatio(), getIsExcluded(salesInfos, basicCalcDate, 7)));
        // 14天日均
        salesDataList.add(new SalesForecastCalculatorDTO(getSaleQtyByDay(salesInfos, basicCalcDate, 14), dto.getFourteenDaysRatio(), getIsExcluded(salesInfos, basicCalcDate, 14)));
        // 30天日均
        salesDataList.add(new SalesForecastCalculatorDTO(getSaleQtyByDay(salesInfos, basicCalcDate, 30), dto.getThirtyDaysRatio(), getIsExcluded(salesInfos, basicCalcDate, 30)));
        // 60天日均
        salesDataList.add(new SalesForecastCalculatorDTO(getSaleQtyByDay(salesInfos, basicCalcDate, 60), dto.getSixtyDaysRatio(), getIsExcluded(salesInfos, basicCalcDate, 60)));
        // 90天日均
        salesDataList.add(new SalesForecastCalculatorDTO(getSaleQtyByDay(salesInfos, basicCalcDate, 90), dto.getNinetyDaysRatio(), getIsExcluded(salesInfos, basicCalcDate, 90)));
        // 180天日均
        salesDataList.add(new SalesForecastCalculatorDTO(getSaleQtyByDay(salesInfos, basicCalcDate, 180), dto.getOneHundredEightyDaysRatio(), getIsExcluded(salesInfos, basicCalcDate, 180)));
        // 270天日均
        salesDataList.add(new SalesForecastCalculatorDTO(getSaleQtyByDay(salesInfos, basicCalcDate, 270), dto.getTwoHundredSeventyDaysRatio(), getIsExcluded(salesInfos, basicCalcDate, 270)));
        // 360天日均
        salesDataList.add(new SalesForecastCalculatorDTO(getSaleQtyByDay(salesInfos, basicCalcDate, 360), dto.getThreeHundredSixtyDaysRatio(), getIsExcluded(salesInfos, basicCalcDate, 360)));
        // 计算预估日销量
        return SalesForecastCalculatorDTO.calculateForecastedSales(salesDataList);
    }

    private boolean getIsExcluded(List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos, LocalDate basicCalcDate, int days) {
        return salesInfos.stream()
                .filter(v -> !basicCalcDate.minusDays(days).isAfter(v.getDate()) && basicCalcDate.isAfter(v.getDate()))
                .allMatch(v -> v.getIsIgnoreOutOfStock() || CfgRuleSalesDenoisingDenoisingTypeEnum.COMPLETELY.getCode().equals(v.getDenoisingType()));
    }

    private BigDecimal getSaleQtyByDay(List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos, LocalDate basicCalcDate, int days) {
        return salesInfos.stream()
                .filter(v -> !basicCalcDate.minusDays(days).isAfter(v.getDate()) && basicCalcDate.isAfter(v.getDate()))
                .map(ReplenishmentResultDTO.SalesInfoDTO::getSalesQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
