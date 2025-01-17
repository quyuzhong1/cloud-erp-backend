package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class StockingDateSalesHandler extends AbstractSkuCalculationHandler {

    @Resource
    private OverseasUsableHandler overseasUsableHandler;

    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return overseasUsableHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO dto) {
        return true;
    }

    @Override
    public void doHandle(ReplenishmentResultDTO dto, List<ReplenishmentResultDTO> r) {
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = dto.getCfgRuleStrategy().getStockUpResult();
        LocalDate date = LocalDate.parse(dto.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate calculationDays = date.plusDays(dto.getReplenishmentDetail().getStockUpDefaultDays());
        BigDecimal totalSaleQty = BigDecimal.ZERO;
        while (date.isBefore(calculationDays)) {
            LocalDate finalSuggestDeliveryDate = date;
            //获取销量
            BigDecimal saleQty = dto.getSalesEstimates().stream()
                    .filter(v -> v.getDate().equals(finalSuggestDeliveryDate))
                    .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            BigDecimal ratio = getStockingRatio(dto.getReplenishmentDetail().getSkuType(),stockUpResult, finalSuggestDeliveryDate);
            totalSaleQty = totalSaleQty.add(saleQty.multiply(ratio));
            date = date.plusDays(1);
        }
        dto.setShopDemandQty(totalSaleQty.setScale(2, RoundingMode.CEILING).intValue());
    }

    private static BigDecimal getStockingRatio(String skuType, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult, LocalDate finalSuggestDeliveryDate) {
        boolean isNew = CfgRuleStockingRatioTypeEnum.NEW.getCode().equals(skuType);
        //获取建议配置明细备货失效
        return stockUpResult.getRefStockingRatioResults().stream()
                .filter(v -> !v.getStartDate().isAfter(finalSuggestDeliveryDate) && !v.getEndDate().isBefore(finalSuggestDeliveryDate))
                .max(Comparator.comparing(CfgRuleStockingRatioDTO.StockingRatioResultDTO::getIndex))
                .map(CfgRuleStockingRatioDTO.StockingRatioResultDTO::getStockingRatio)
                //获取建议配置默认备货系数
                .orElse(Optional.ofNullable(stockUpResult.getRefStockingRatio())
                        //获取默认配置明细备货系数
                        .orElse(stockUpResult.getStockingRatioResults().stream()
                                .filter(v -> !v.getStartDate().isAfter(finalSuggestDeliveryDate) && !v.getEndDate().isBefore(finalSuggestDeliveryDate))
                                .max(Comparator.comparing(CfgRuleStockingRatioDTO.StockingRatioResultDTO::getIndex))
                                .map(CfgRuleStockingRatioDTO.StockingRatioResultDTO::getStockingRatio)
                                //获取默认配置默认系数
                                .orElse(isNew ? stockUpResult.getNewStockingRatio() : stockUpResult.getStockingRatio())))

                ;
    }
}
