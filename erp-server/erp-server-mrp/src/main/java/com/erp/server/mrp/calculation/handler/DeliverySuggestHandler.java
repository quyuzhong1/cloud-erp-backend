package com.erp.server.mrp.calculation.handler;

import com.common.business.config.DocNoGenHelper;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.wms.enums.ExecutionTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.common.business.enums.BusinessNoTypeEnum.CODE_FHJY;

@Component
public class DeliverySuggestHandler extends AbstractSkuCalculationHandler {
    @Resource
    private PurchaseSuggestHandler purchaseSuggestHandler;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
    @Resource
    private InventoryService inventoryService;

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
        List<CfgRuleCommonDTO.StrategyResultDTO> suggestAmountResult = cfgRuleStrategyDTO.getSuggestAmountResult();
        String baseKey = CfgRuleCommonTypeEnum.getBaseSuggestRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        Set<String> deliveryVolumeAging = cfgRuleCommonService.findByKey(baseKey, suggestAmountResult, baseKey + ":" + CfgRuleSuggestedAmountNodeEnum.getDeliveryVolumeAging());
        Set<String> deliveryVolumeInventory = cfgRuleCommonService.findByKey(baseKey, suggestAmountResult, baseKey + ":" + CfgRuleSuggestedAmountNodeEnum.getDeliveryVolumeInventory());
        int agingDays = deliveryVolumeAging.stream()
                .map(v -> ReplenishmentResultDTO.DetailDTO.getAttributeValue(replenishmentResultDTO.getReplenishmentDetail(), v))
                .reduce(0, Math::addExact);
        //计算天数
        int days = cfgRuleStrategyDTO.getSettings()
                .stream().filter(v -> v.getKey().equals(CfgSettingEnum.CALCULATION_DAYS.getCode()))
                .map(CfgSettingDTO::getDataJson)
                .map(Integer::parseInt)
                .findFirst().orElse(180);
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
                        //建议发货量
                        LocalDate calcDate = suggestDeliveryDate.plusDays(Math.min(agingDays, days));
                        int suggestDeliveryQty = getSuggestDeliveryQty(calcDate, salesEstimates, stockingRatioResults, stockingRatio, now);
                        int inventory = inventoryService.getInventory(replenishmentResultDTO, now, calcDate, deliveryVolumeInventory, cfgRuleStrategyDTO.getWarehouseResult());
                        suggestDTO.setSuggestDeliveryQty(Math.max(0, suggestDeliveryQty - inventory));
                    }
                    if (CfgRulePlatformTypeEnum.B2B.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                            || CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                        //建议备货日期（本地备货）= 断货日期 - 本地备货安全天数；若建议发货日期＜当前日期，取当前日期。试算时，以“建议备货日” 到货
                        LocalDate suggestDeliveryDate = localDate.minusDays(stockUpResult.getSafeDays());
                        suggestDeliveryDate = suggestDeliveryDate.isBefore(now) ? now : suggestDeliveryDate;
                        suggestDTO.setSuggestDeliveryDate(suggestDeliveryDate);
                        //建议发货量
                        LocalDate calcDate = suggestDeliveryDate.plusDays(Math.min(agingDays, days));
                        int inventory = inventoryService.getInventory(replenishmentResultDTO, now, calcDate, deliveryVolumeInventory, cfgRuleStrategyDTO.getWarehouseResult());
                        suggestDTO.setSuggestDeliveryQty(Math.max(0, getSuggestDeliveryQty(calcDate, salesEstimates, stockingRatioResults, stockingRatio, now) - inventory));
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
     * @param suggestDeliveryDate  计算开始日期
     */
    private int getSuggestDeliveryQty(LocalDate calculationDays, List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates, List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResults, BigDecimal stockingRatio, LocalDate suggestDeliveryDate) {
        BigDecimal totalSaleQty = BigDecimal.ZERO;
        while (suggestDeliveryDate.isBefore(calculationDays)) {
            LocalDate finalSuggestDeliveryDate = suggestDeliveryDate;
            //获取销量
            BigDecimal saleQty = salesEstimates.parallelStream()
                    .filter(v -> v.getDate().equals(finalSuggestDeliveryDate))
                    .map(ReplenishmentResultDTO.SalesEstimateDTO::getSalesQty)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            BigDecimal ratio = stockingRatioResults.parallelStream()
                    .filter(v -> !v.getStartDate().isAfter(finalSuggestDeliveryDate) && !v.getEndDate().isBefore(finalSuggestDeliveryDate))
                    .max(Comparator.comparing(CfgRuleStockingRatioDTO.StockingRatioResultDTO::getIndex))
                    .map(CfgRuleStockingRatioDTO.StockingRatioResultDTO::getStockingRatio)
                    .orElse(stockingRatio);
            totalSaleQty = totalSaleQty.add(saleQty.multiply(ratio));
            suggestDeliveryDate = suggestDeliveryDate.plusDays(1);
        }
        return totalSaleQty.setScale(2, RoundingMode.CEILING).intValue();
    }

}
