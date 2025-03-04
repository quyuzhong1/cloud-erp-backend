package com.erp.server.mrp.calculation.strategy.sales;

import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.*;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractSalesEstimateStrategy implements SalesEstimateStrategy {


    @Override
    public void process(ReplenishmentResultDTO dto) {
        LocalDate basicCalcDate = LocalDate.parse(dto.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        CfgRuleStrategyDTO cfgRuleStrategyDTO = dto.getCfgRuleStrategy();
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults = cfgRuleStrategyDTO.getSalesQtyResult().getFormulaResults();
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults = cfgRuleStrategyDTO.getSalesQtyResult().getDefaultFormulaResults();
        List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates = new ArrayList<>();
        //计算天数
        int days = cfgRuleStrategyDTO.getSettings()
                .stream().filter(v -> v.getKey().equals(CfgSettingEnum.CALCULATION_DAYS.getCode()))
                .map(CfgSettingDTO::getDataJson)
                .map(Integer::parseInt)
                .findFirst().orElse(0);
        for (int i = 0; i < days; i++) {
            LocalDate calcDate = basicCalcDate.plusDays(i);
            //获取最大优先级的规则 优先取 sku 固定规则，其次sku动态规则，其次sku默认规则，取不到则取系统动态规则，其次系统默认规则
            CfgRuleSalesQtyDTO.StrategyFormulaResultDTO formulaResult = getStrategyFormulaResultDTO(formulaResults, calcDate, defaultFormulaResults);
            if (ObjectUtils.isEmpty(formulaResult)) {
                continue;
            }
            BigDecimal saleQty = getSaleQty(dto.getSalesInfos(), dto.getAvgTimePeriodSales(), formulaResult, basicCalcDate);
            ReplenishmentResultDTO.SalesEstimateDTO salesEstimateDTO = ReplenishmentResultDTO.SalesEstimateDTO.buildSalesEstimateDTO(calcDate, saleQty, formulaResult);
            salesEstimates.add(salesEstimateDTO);
        }
        dto.setSalesEstimates(salesEstimates);
    }



    /**
     * 获取日销量规则
     *
     * @param formulaResults        sku日销量规则
     * @param calcDate              计算日
     * @param defaultFormulaResults 默认日销量规则
     */
    private static CfgRuleSalesQtyDTO.StrategyFormulaResultDTO getStrategyFormulaResultDTO(List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults, LocalDate calcDate, List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults) {
        return formulaResults.stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getStartDate()) && !ObjectUtils.isEmpty(v.getEndDate()) &&
                        !v.getStartDate().isAfter(calcDate) && !v.getEndDate().isBefore(calcDate))
                .min(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getPriority)
                        .thenComparing(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getIndex).reversed()))
                .orElse(formulaResults.stream()
                        .filter(v -> CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(v.getType()))
                        .findFirst().orElse(getSysDynamic(calcDate, defaultFormulaResults)));
    }

    /**
     * 获取系统动态规则
     * @param defaultFormulaResults 系统销量规则
     * @param calcDate 当前计算日
     */
    private static CfgRuleSalesQtyDTO.StrategyFormulaResultDTO getSysDynamic(LocalDate calcDate, List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults) {
        return defaultFormulaResults.stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getStartDate()) && !ObjectUtils.isEmpty(v.getEndDate()) &&
                        !v.getStartDate().isAfter(calcDate) && !v.getEndDate().isBefore(calcDate))
                .min(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getPriority)
                        .thenComparing(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getIndex).reversed()))
                .orElse(getSysDefault(defaultFormulaResults));
    }


    /**
     * 获取系统默认规则
     * @param defaultFormulaResults 系统销量规则
     */
    private static CfgRuleSalesQtyDTO.StrategyFormulaResultDTO getSysDefault(List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults) {
        return defaultFormulaResults.stream()
                .filter(v -> CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(v.getType()))
                .findFirst().orElse(null);
    }

    /**
     * 获取预估销量
     *
     * @param salesInfos    建议历史销量
     * @param formulaResult 销量计算参数
     * @param basicCalcDate 计算时间
     */
    public static BigDecimal getSaleQty(List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos, List<ReplenishmentResultDTO.TimePeriodSalesDTO> avgTimePeriodSales, CfgRuleSalesQtyDTO.StrategyFormulaResultDTO formulaResult, LocalDate basicCalcDate) {
        BigDecimal saleQty;
        if (CfgRuleSalesFormulaTypeEnum.FIXED.getCode().equals(formulaResult.getType())) {
            saleQty = MathUtil.valueOf(formulaResult.getFixedValue());
        } else if (CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode().equals(formulaResult.getType())) {
            saleQty = getDynamicSaleQty(formulaResult.getPercentJsonDTO(), salesInfos, avgTimePeriodSales, basicCalcDate);
        } else {
            if (CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getCode().equals(formulaResult.getDefaultType())) {
                saleQty = getDynamicSaleQty(formulaResult.getPercentJsonDTO(), salesInfos, avgTimePeriodSales, basicCalcDate);
            } else {
                saleQty = MathUtil.valueOf(formulaResult.getFixedValue());
            }
        }
        return saleQty;
    }

    /**
     * 计算动态规则销量
     *
     * @param dto                动态规则
     * @param salesInfos         销量
     * @param avgTimePeriodSales 去噪销量日均
     * @param basicCalcDate      计算日
     */
    private static BigDecimal getDynamicSaleQty(CfgRuleSalesFormulaDTO.PercentJsonDTO dto, List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos, List<ReplenishmentResultDTO.TimePeriodSalesDTO> avgTimePeriodSales, LocalDate basicCalcDate) {

        List<SalesForecastCalculatorDTO> salesDataList = new ArrayList<>();
        LocalDate localDate = basicCalcDate.minusDays(1);
        Map<TimePeriodEnum, BigDecimal> timePeriodMap = avgTimePeriodSales
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

    /**
     * 判断是否排除权重
     * @param salesInfos    销量
     * @param basicCalcDate 计算日
     * @param days          天数
     */
    private static boolean getIsExcluded(List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos, LocalDate basicCalcDate, int days) {
        return salesInfos.stream()
                .filter(v -> !basicCalcDate.minusDays(days).isAfter(v.getDate()) && basicCalcDate.isAfter(v.getDate()))
                .allMatch(v -> Boolean.TRUE.equals(v.getIsIgnoreOutOfStock()) || CfgRuleSalesDenoisingDenoisingTypeEnum.COMPLETELY.getCode().equals(v.getDenoisingType()));
    }

}
