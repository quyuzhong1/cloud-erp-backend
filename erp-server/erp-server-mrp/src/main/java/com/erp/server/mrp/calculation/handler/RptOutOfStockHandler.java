package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.CfgSettingDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgSettingEnum;
import com.erp.server.mrp.service.RealOutOfStockService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class RptOutOfStockHandler extends AbstractSkuCalculationHandler {
    @Resource
    private RealSellableDaysHandler realSellableDaysHandler;
    @Resource
    private RealOutOfStockService realOutOfStockService;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return realSellableDaysHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        int days = cfgRuleStrategyDTO.getSettings()
                .stream().filter(v -> v.getKey().equals(CfgSettingEnum.CALCULATION_DAYS.getCode()))
                .map(CfgSettingDTO::getDataJson)
                .map(Integer::parseInt)
                .findFirst().orElse(0);
        LocalDate basicCalcDate = LocalDate.parse(replenishmentResultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        //反推计算真实断货
        LocalDate realStartDate = realOutOfStockService.getRealStartDate(replenishmentResultDTO.getReplenishmentDetail().getDetailId(), basicCalcDate);
        List<ReplenishmentResultDTO.RptOutOfStockDTO> rptOutOfStocks = new ArrayList<>();
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
            BigDecimal balanceInventory = BigDecimal.valueOf(replenishmentResultDTO.getReplenishmentDetail().getFbaUsableQty());
            //结余库存 = 前日结余库存 - 预估销量 + 到货库存
            for (int i = 0; i <= days; i++) {
                LocalDate calcDate = basicCalcDate.plusDays(i);
                //获取到货库存
                Integer fbaInTransit = Optional.ofNullable(replenishmentResultDTO.getFbaInTransitDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.FbaInTransitDetailDTO::getInTransitQty)
                        .reduce(0, Math::addExact);
                Integer fbaDelivery =  Optional.ofNullable(replenishmentResultDTO.getFbaDeliveryDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getQty)
                        .reduce(0, Math::addExact);
                Integer overseasInTransit = Optional.ofNullable(replenishmentResultDTO.getOverseasInTransitDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.OverseasInTransitDetailDTO::getInTransitQty)
                        .reduce(0, Math::addExact);
                Integer overseasDelivery = Optional.ofNullable(replenishmentResultDTO.getOverseasDeliveryDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getQty)
                        .reduce(0, Math::addExact);
                Integer localInTransit = Optional.ofNullable(replenishmentResultDTO.getLocalInTransitDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.LocalInTransitDetailDTO::getQty)
                        .reduce(0, Math::addExact);
                Integer localDelivery = Optional.ofNullable(replenishmentResultDTO.getLocalPurchaseDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::getQty)
                        .reduce(0, Math::addExact);
                BigDecimal salesQty = Optional.ofNullable(replenishmentResultDTO.getSalesEstimates()).orElse(Collections.emptyList())
                        .stream()
                        .filter(v -> v.getDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
                balanceInventory = balanceInventory.add(new BigDecimal(fbaInTransit)).add(new BigDecimal(fbaDelivery))
                        .add(new BigDecimal(overseasInTransit)).add(new BigDecimal(overseasDelivery)).add(new BigDecimal(localInTransit))
                        .add(new BigDecimal(localDelivery)).subtract(salesQty);
                realStartDate = getRealStartDate(replenishmentResultDTO.getSalesPrice(), balanceInventory, realStartDate, rptOutOfStocks, calcDate, salesQty);
            }
        }

        if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
            BigDecimal balanceInventory = BigDecimal.valueOf(replenishmentResultDTO.getReplenishmentDetail().getOverseasUsableQty());
            //结余库存 = 前日结余库存 - 预估销量 + 到货库存
            for (int i = 0; i <= days; i++) {
                LocalDate calcDate = LocalDate.parse(replenishmentResultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE).plusDays(i);
                Integer overseasInTransit = Optional.ofNullable(replenishmentResultDTO.getOverseasInTransitDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.OverseasInTransitDetailDTO::getInTransitQty)
                        .reduce(0, Math::addExact);
                Integer overseasDelivery = Optional.ofNullable(replenishmentResultDTO.getOverseasDeliveryDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.EstimatedDeliveryDetailDTO::getQty)
                        .reduce(0, Math::addExact);
                Integer localInTransit = Optional.ofNullable(replenishmentResultDTO.getLocalInTransitDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.LocalInTransitDetailDTO::getQty)
                        .reduce(0, Math::addExact);
                Integer localDelivery = Optional.ofNullable(replenishmentResultDTO.getLocalPurchaseDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::getQty)
                        .reduce(0, Math::addExact);
                BigDecimal salesQty = Optional.ofNullable(replenishmentResultDTO.getSalesEstimates()).orElse(Collections.emptyList())
                        .stream()
                        .filter(v -> v.getDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
                balanceInventory = balanceInventory.add(new BigDecimal(overseasInTransit)).add(new BigDecimal(overseasDelivery)).add(new BigDecimal(localInTransit))
                        .add(new BigDecimal(localDelivery)).subtract(salesQty);
                realStartDate = getRealStartDate(replenishmentResultDTO.getSalesPrice(), balanceInventory, realStartDate, rptOutOfStocks, calcDate, salesQty);
            }
        }
        if (CfgRulePlatformTypeEnum.B2B.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                || CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
            BigDecimal balanceInventory = BigDecimal.valueOf(replenishmentResultDTO.getReplenishmentDetail().getLocalUsableQty());
            //结余库存 = 前日结余库存 - 预估销量 + 到货库存
            for (int i = 0; i <= days; i++) {
                LocalDate calcDate = LocalDate.parse(replenishmentResultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE).plusDays(i);
                Integer localInTransit = Optional.ofNullable(replenishmentResultDTO.getLocalInTransitDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.LocalInTransitDetailDTO::getQty)
                        .reduce(0, Math::addExact);
                Integer localDelivery = Optional.ofNullable(replenishmentResultDTO.getLocalPurchaseDetails()).orElse(Collections.emptyList())
                        .parallelStream()
                        .filter(v -> v.getEstimateSalesDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.EstimatedPurchaseDetailDTO::getQty)
                        .reduce(0, Math::addExact);
                BigDecimal salesQty = Optional.ofNullable(replenishmentResultDTO.getSalesEstimates()).orElse(Collections.emptyList())
                        .stream()
                        .filter(v -> v.getDate().equals(calcDate))
                        .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
                balanceInventory = balanceInventory.add(new BigDecimal(localInTransit)).add(new BigDecimal(localDelivery)).subtract(salesQty);
                realStartDate = getRealStartDate(replenishmentResultDTO.getSalesPrice(), balanceInventory, realStartDate, rptOutOfStocks, calcDate, salesQty);
            }
        }
        replenishmentResultDTO.setRptOutOfStocks(rptOutOfStocks);
    }

    /**
     * 获取断货报告
     * @param salesPrice 销售价
     * @param balanceInventory 结余库存
     * @param realStartDate 真实断货开始时间
     * @param rptOutOfStocks 断货报告
     * @param calcDate 计算时间
     * @param salesQty 销售数量
     */
    private static LocalDate getRealStartDate(BigDecimal salesPrice, BigDecimal balanceInventory, LocalDate realStartDate, List<ReplenishmentResultDTO.RptOutOfStockDTO> rptOutOfStocks, LocalDate calcDate, BigDecimal salesQty) {
        if (balanceInventory.compareTo(BigDecimal.ZERO) <= 0) {
            if (null == realStartDate) {
                realStartDate = calcDate;
                rptOutOfStocks.add(new ReplenishmentResultDTO.RptOutOfStockDTO(calcDate, calcDate, calcDate, salesQty, salesQty.multiply(Optional.ofNullable(salesPrice).orElse(BigDecimal.ZERO))));
            }else {
                rptOutOfStocks.add(new ReplenishmentResultDTO.RptOutOfStockDTO(calcDate, realStartDate, calcDate, salesQty, salesQty.multiply(Optional.ofNullable(salesPrice).orElse(BigDecimal.ZERO))));
            }
        }else {
            realStartDate = null;
        }
        return realStartDate;
    }
}
