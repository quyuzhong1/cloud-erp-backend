package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.RecentTimePeriodEnum;
import com.erp.server.mrp.calculation.factory.SalesEstimateFactory;
import com.erp.server.mrp.calculation.strategy.sales.SalesEstimateStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.RecentTimePeriodEnum.*;

@Component
public class SalesEstimateHandler extends AbstractSkuCalculationHandler {
    @Resource
    private StockingDateSalesHandler stockingDateSalesHandler;
    @Resource
    private SalesEstimateFactory salesEstimateFactory;

    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return stockingDateSalesHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO r) {
        return true;
    }

    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {
        LocalDate basicCalcDate = LocalDate.parse(replenishmentResultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        CfgRuleStrategyDTO cfgRuleStrategyDTO = replenishmentResultDTO.getCfgRuleStrategy();
        SalesEstimateStrategy salesEstimate = salesEstimateFactory.getSalesEstimate(cfgRuleStrategyDTO.getSalesQtyResult().getSalesEstimateType());
        salesEstimate.process(replenishmentResultDTO);
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
        avgTimePeriodSalesEstimates.add(new ReplenishmentResultDTO.TimePeriodSalesEstimateDTO(recentTimePeriodEnum, followingSales.divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate) + 1), 2, RoundingMode.HALF_UP)));
    }



}
