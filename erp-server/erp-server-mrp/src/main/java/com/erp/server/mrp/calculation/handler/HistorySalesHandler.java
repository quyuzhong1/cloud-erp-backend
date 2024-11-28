package com.erp.server.mrp.calculation.handler;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleSalesDenoisingDenoisingTypeEnum;
import com.erp.model.mrp.enums.TimePeriodEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.erp.model.mrp.enums.CfgRuleSalesDenoisingDenoisingTypeEnum.COMPLETELY;


@Component
public class HistorySalesHandler extends AbstractSkuCalculationHandler {
    @Resource
    private SalesEstimateHandler salesEstimateHandler;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return salesEstimateHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        //获取销量配置
        CfgRuleSalesQtyDTO.StrategyResultDTO salesQtyResult = cfgRuleStrategyDTO.getSalesQtyResult();
        boolean isIgnoreOutOfStock = salesQtyResult.getIsIgnoreOutOfStock();
        //开始计算去噪销量
        calculationSales(isIgnoreOutOfStock, replenishmentResultDTO, salesQtyResult.getDenoisingResults(), salesQtyResult.getDefaultDenoisingResults());
        //开始计算分时段销量和日均
        calculationTimePeriodSales(replenishmentResultDTO);
    }

    /**
     * 计算分时段销量和日均
     * @param replenishmentResultDTO 建议
     */
    private void calculationTimePeriodSales(ReplenishmentResultDTO replenishmentResultDTO) {

        //分时段销量
        List<ReplenishmentResultDTO.TimePeriodSalesDTO> timePeriodSales = new ArrayList<>();
        //分时段日均销量
        List<ReplenishmentResultDTO.TimePeriodSalesDTO> avgTimePeriodSales = new ArrayList<>();
        List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos = replenishmentResultDTO.getSalesInfos();
        LocalDate now = LocalDate.parse(replenishmentResultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate endDate = now.minusDays(1);
        for (TimePeriodEnum value : TimePeriodEnum.values()) {
            BigDecimal qty = salesInfos.stream()
                    .filter(v -> !endDate.minusDays(value.getDays()).isAfter(v.getDate()) && endDate.isAfter(v.getDate()))
                    .map(ReplenishmentResultDTO.SalesInfoDTO::getSalesQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(0, RoundingMode.CEILING);
            timePeriodSales.add(new ReplenishmentResultDTO.TimePeriodSalesDTO(value, qty));
            long count = salesInfos.stream()
                    .filter(v -> !endDate.minusDays(value.getDays()).isAfter(v.getDate()) && endDate.isAfter(v.getDate()))
                    .filter(v -> Boolean.FALSE.equals(v.getIsIgnoreOutOfStock()))
                    .filter(v -> !COMPLETELY.getCode().equals(v.getDenoisingType()))
                    .count();
            BigDecimal avgQty = new BigDecimal(0);
            if (count != 0) {
                avgQty = qty.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            }
            avgTimePeriodSales.add(new ReplenishmentResultDTO.TimePeriodSalesDTO(value, avgQty));
        }
        replenishmentResultDTO.setTimePeriodSales(timePeriodSales);
        replenishmentResultDTO.setAvgTimePeriodSales(avgTimePeriodSales);
    }

    /**
     * 计算销量
     * @param isIgnoreOutOfStock      是否断货排除
     * @param replenishmentResultDTO  建议
     * @param denoisingResults        sku去噪规则
     * @param defaultDenoisingResults 默认去噪规则
     */
    private void calculationSales(boolean isIgnoreOutOfStock, ReplenishmentResultDTO replenishmentResultDTO, List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> denoisingResults, List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> defaultDenoisingResults) {

        List<ReplenishmentResultDTO.SalesInfoDTO> salesInfoDTOS = new ArrayList<>();
        List<ReplenishmentResultDTO.SalesInfoDTO> salesInfoList = new ArrayList<>();
        LocalDate nowDate = LocalDate.parse(replenishmentResultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate startDate = nowDate.minusDays(361);
        LocalDate endDate = nowDate.minusDays(1);
        while (!startDate.isAfter(endDate)) {
            LocalDate date = startDate;
            ReplenishmentResultDTO.SalesInfoDTO salesInfoDTO = new ReplenishmentResultDTO.SalesInfoDTO();
            salesInfoDTO.setId(IdWorker.getIdStr());
            salesInfoDTO.setDate(date);
            //获取符合的最大优先级销量去噪规则 (序号越小优先级越大)
            //获取符合的最大优先级销量去噪规则 (序号越小优先级越大)
            CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO denoisingResult = denoisingResults.stream()
                    .filter(v -> !v.getStartDate().isAfter(date) && !v.getEndDate().isBefore(date))
                    .max(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO::getIndex))
                    .orElse(defaultDenoisingResults.stream()
                            .filter(v -> !v.getStartDate().isAfter(date) && !v.getEndDate().isBefore(date))
                            .max(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO::getIndex))
                            .orElse(null));
            int originalSalesQty = Optional.ofNullable(replenishmentResultDTO.getHistorySalesList().get(date)).orElse(0);
            int originalInventQty = Optional.ofNullable(replenishmentResultDTO.getHistoryInventoryList().get(date)).orElse(0);
            // 断货排除 ＞ 销量去噪
            if (Boolean.TRUE.equals(isIgnoreOutOfStock) && originalSalesQty == 0 && originalInventQty == 0) {
                //存在真实断货
                salesInfoDTO.setIsIgnoreOutOfStock(true);
                salesInfoDTO.setSalesQty(new BigDecimal(0));
                salesInfoList.add(salesInfoDTO);
            } else {
                salesInfoDTO.setIsIgnoreOutOfStock(false);
                if (ObjectUtils.isEmpty(denoisingResult)) {
                    salesInfoDTO.setSalesQty(new BigDecimal(originalSalesQty));
                } else {
                    CfgRuleSalesDenoisingDenoisingTypeEnum code = CfgRuleSalesDenoisingDenoisingTypeEnum.getEnumByCode(denoisingResult.getDenoisingType());
                    BigDecimal salesQty = new BigDecimal(0);
                    switch (Objects.requireNonNull(code)) {
                        case PERCENTAGE:
                            salesQty = new BigDecimal(originalSalesQty)
                                    .multiply(BigDecimal.valueOf(denoisingResult.getEffectiveValue()))
                                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                            break;
                        case FIXED_VALUE:
                            salesQty = BigDecimal.valueOf(denoisingResult.getEffectiveValue());
                            break;
                        default:
                            break;
                    }
                    salesInfoDTO.setSalesQty(salesQty);
                    salesInfoDTO.setDenoisingType(denoisingResult.getDenoisingType());
                    salesInfoList.add(salesInfoDTO);
                }
            }
            salesInfoDTOS.add(salesInfoDTO);
            startDate = startDate.plusDays(1);
        }
        replenishmentResultDTO.setSalesInfos(salesInfoDTOS);
        replenishmentResultDTO.setSalesInfoList(salesInfoList);
    }
}
