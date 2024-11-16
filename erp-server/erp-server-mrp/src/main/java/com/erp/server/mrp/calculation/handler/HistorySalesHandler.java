package com.erp.server.mrp.calculation.handler;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
        for (ReplenishmentResultDTO.SalesInfoDTO salesInfo : replenishmentResultDTO.getSalesInfos()) {
            //获取符合的最大优先级销量去噪规则 (序号越小优先级越大)
            CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO denoisingResult = denoisingResults.stream()
                    .filter(v -> !v.getStartDate().isAfter(salesInfo.getDate()) && !v.getEndDate().isBefore(salesInfo.getDate()))
                    .max(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO::getIndex))
                    .orElse(defaultDenoisingResults.stream()
                            .filter(v -> !v.getStartDate().isAfter(salesInfo.getDate()) && !v.getEndDate().isBefore(salesInfo.getDate()))
                            .max(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO::getIndex))
                            .orElse(null));
            // 断货排除 ＞ 销量去噪
            if (isIgnoreOutOfStock && salesInfo.getOriginalInventoryQty() == 0 && salesInfo.getOriginalSalesQty() == 0) {
                //存在真实断货
                salesInfo.setSalesQty(new BigDecimal(0));
                salesInfo.setIsIgnoreOutOfStock(true);
            } else {
                salesInfo.setIsIgnoreOutOfStock(false);
                //走销量规则
                if (ObjectUtils.isEmpty(denoisingResult)) {
                    salesInfo.setSalesQty(new BigDecimal(salesInfo.getOriginalSalesQty()));
                } else {
                    CfgRuleSalesDenoisingDenoisingTypeEnum code = CfgRuleSalesDenoisingDenoisingTypeEnum.getEnumByCode(denoisingResult.getDenoisingType());
                    BigDecimal salesQty = new BigDecimal(0);
                    switch (Objects.requireNonNull(code)) {
                        case PERCENTAGE:
                            salesQty = new BigDecimal(salesInfo.getOriginalSalesQty())
                                    .multiply(BigDecimal.valueOf(denoisingResult.getEffectiveValue()))
                                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                            break;
                        case FIXED_VALUE:
                            salesQty = BigDecimal.valueOf(denoisingResult.getEffectiveValue());
                            break;
                        default:
                            break;
                    }
                    salesInfo.setSalesQty(salesQty);
                    salesInfo.setDenoisingType(denoisingResult.getDenoisingType());
                }
            }
        }
    }
}
