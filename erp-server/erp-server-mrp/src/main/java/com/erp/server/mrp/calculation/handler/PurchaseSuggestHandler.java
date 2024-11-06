package com.erp.server.mrp.calculation.handler;

import com.common.business.config.DocNoGenHelper;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.wms.enums.ExecutionTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.common.business.enums.BusinessNoTypeEnum.CODE_P;

@Component
public class PurchaseSuggestHandler extends AbstractSkuCalculationHandler {

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private RecentSuggestionHandler recentSuggestionHandler;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
    @Resource
    private InventoryService inventoryService;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return recentSuggestionHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = cfgRuleStrategyDTO.getStockUpResult();
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult = stockUpResult.getLogisticsResult();
        List<ReplenishmentResultDTO.RptOutOfStockDTO> rptOutOfStocks = replenishmentResultDTO.getRptOutOfStocks();
        List<CfgRuleCommonDTO.StrategyResultDTO> suggestAmountResult = cfgRuleStrategyDTO.getSuggestAmountResult();
        String baseKey = CfgRuleCommonTypeEnum.getBaseSuggestRedisKey(replenishmentResultDTO.getReplenishment().getPlatformType());
        Set<String> purchaseVolumeAging = cfgRuleCommonService.findByKey(baseKey, suggestAmountResult, baseKey + ":" + CfgRuleSuggestedAmountNodeEnum.getPurchaseVolumeAging());
        Set<String> purchaseVolumeInventory = cfgRuleCommonService.findByKey(baseKey, suggestAmountResult, baseKey + ":" + CfgRuleSuggestedAmountNodeEnum.getPurchaseVolumeInventory());
        int agingDays = purchaseVolumeAging.stream()
                .map(v -> ReplenishmentResultDTO.DetailDTO.getAttributeValue(replenishmentResultDTO.getReplenishmentDetail(), v))
                .reduce(0, Math::addExact);
        //计算天数
        int days = cfgRuleStrategyDTO.getSettings()
                .stream().filter(v -> v.getKey().equals(CfgSettingEnum.CALCULATION_DAYS.getCode()))
                .map(CfgSettingDTO::getDataJson)
                .map(Integer::parseInt)
                .findFirst().orElse(180);
        List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResults = stockUpResult.getStockingRatioResults();
        // 根据新品/常规品获取默认补货系数
        BigDecimal stockingRatio = CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode().equals(replenishmentResultDTO.getReplenishmentDetail().getSkuType()) ? stockUpResult.getStockingRatio() : stockUpResult.getNewStockingRatio();
        List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates = replenishmentResultDTO.getSalesEstimates();
        LocalDate now = LocalDate.parse(replenishmentResultDTO.getReplenishmentDetail().getCalcDate(), DateTimeFormatter.BASIC_ISO_DATE);
        //处理连续断货数据
        List<LocalDate> dates = rptOutOfStocks.stream().map(ReplenishmentResultDTO.RptOutOfStockDTO::getStartDate).distinct().collect(Collectors.toList());
        List<ReplenishmentResultDTO.PurchaseSuggestDTO> deliverySuggests = dates.parallelStream()
                .map(localDate -> {
                    String code = docNoGenHelper.generateCode(CODE_P);
                    ReplenishmentResultDTO.PurchaseSuggestDTO suggestDTO = ReplenishmentResultDTO.PurchaseSuggestDTO.buildPurchaseSuggestDTO(code, logisticsResult, replenishmentResultDTO, ExecutionTypeEnum.AUTO.getCode());
                    if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                            || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                        //建议采购日期（本地发FBA） = 断货日期 -（审批时长 + 采购交期 + 供应商发货时效 + 质检天数 + 采购频率 + 本地发FBA时效 + FBA入库时间 + 本地仓发货频率 + FBA安全天数）
                        //建议采购日期（本地发海外） = 断货日期 -（审批时长 + 采购交期 + 供应商发货时效 + 质检天数 + 采购频率 + 本地发海外时效 + 海外仓入库时间 + 本地仓发货频率 + 海外仓安全天数）
                        LocalDate suggestDeliveryDate = localDate.minusDays(stockUpResult.getPurchaseApproveDays())
                                .minusDays(stockUpResult.getProductionDays())
                                .minusDays(stockUpResult.getSupplierDeliveryDays())
                                .minusDays(stockUpResult.getQcDays())
                                .minusDays(stockUpResult.getPurchaseCycleDays())
                                .minusDays(logisticsResult.getLogisticsDays())
                                .minusDays(stockUpResult.getInstockDays())
                                .minusDays(logisticsResult.getLogisticsCycleDays())
                                .minusDays(stockUpResult.getSafeDays());
                        suggestDeliveryDate = suggestDeliveryDate.isBefore(now) ? now : suggestDeliveryDate;
                        suggestDTO.setSuggestPurchaseDate(suggestDeliveryDate);
                        //预计可售日期 （本地发FBA）= 建议采购日 +（审批时长 + 采购交期 + 供应商发货时效 +质检天数）+（本地发FBA时效 + FBA入库时间）
                        //预计可售日期 （本地发海外）= 建议采购日 +（审批时长 + 采购交期 + 供应商发货时效 +质检天数）+（本地发海外时效 + 海外仓入库时间）
                        LocalDate estimateSalesDate = suggestDeliveryDate.plusDays(stockUpResult.getPurchaseApproveDays()).plusDays(stockUpResult.getProductionDays())
                                .plusDays(stockUpResult.getSupplierDeliveryDays()).plusDays(stockUpResult.getQcDays())
                                .plusDays(logisticsResult.getLogisticsDays()).plusDays(stockUpResult.getInstockDays());
                        suggestDTO.setEstimateSalesDate(estimateSalesDate);
                        //预计入库日期 （本地发FBA）= 建议采购日 +（审批时长 + 采购交期 + 供应商发货时效 + 质检天数）
                        //预计入库日期 （本地发海外）= 建议采购日 +（审批时长 + 采购交期 + 供应商发货时效 + 质检天数）
                        LocalDate estimateInstockDate = suggestDeliveryDate.plusDays(stockUpResult.getPurchaseApproveDays()).plusDays(stockUpResult.getProductionDays())
                                .plusDays(stockUpResult.getSupplierDeliveryDays()).plusDays(stockUpResult.getQcDays());
                        suggestDTO.setEstimateInstockDate(estimateInstockDate);
                        //建议采购量
                        LocalDate calcDate = suggestDeliveryDate.plusDays(Math.min(agingDays, days));
                        int suggestDeliveryQty = getSuggestDeliveryQty(calcDate, salesEstimates, stockingRatioResults, stockingRatio, now);
                        int inventory = inventoryService.getInventory(replenishmentResultDTO, calcDate, purchaseVolumeInventory, cfgRuleStrategyDTO.getWarehouseResult());
                        suggestDTO.setSuggestPurchaseQty(Math.max(0, suggestDeliveryQty - inventory));
                    }
                    if (CfgRulePlatformTypeEnum.B2B.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                            || CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                        //建议采购日期（本地备货） = 断货日期 -（审批时长 + 采购交期 + 供应商发货时效 + 质检天数 + 采购频率 + 本地备货安全天数）
                        LocalDate suggestDeliveryDate = getSuggestDeliveryDate(localDate, stockUpResult, now);
                        suggestDTO.setSuggestPurchaseDate(suggestDeliveryDate);
                        //建议采购量
                        LocalDate calcDate = suggestDeliveryDate.plusDays(Math.min(agingDays, days));
                        int inventory = inventoryService.getInventory(replenishmentResultDTO, calcDate, purchaseVolumeInventory, cfgRuleStrategyDTO.getWarehouseResult());
                        suggestDTO.setSuggestPurchaseQty(Math.max(0, getSuggestDeliveryQty(calcDate, salesEstimates, stockingRatioResults, stockingRatio, now) - inventory));
                        //预计入库日期 （本地备货）= 建议采购日 +（审批时长 + 采购交期 + 供应商发货时效 + 质检天数）
                        LocalDate estimateInstockDate = suggestDeliveryDate.plusDays(stockUpResult.getPurchaseApproveDays()).plusDays(stockUpResult.getProductionDays())
                                .plusDays(stockUpResult.getSupplierDeliveryDays()).plusDays(stockUpResult.getQcDays());
                        suggestDTO.setEstimateInstockDate(estimateInstockDate);
                    }
                    //采购成本 = 采购单价 * 建议采购量，取一供 ＞ 二供
                    if (!ObjectUtils.isEmpty(replenishmentResultDTO.getPurchasePrice())) {
                        suggestDTO.setPurchaseCost(replenishmentResultDTO.getPurchasePrice().multiply(BigDecimal.valueOf(suggestDTO.getSuggestPurchaseQty())));
                    }
                    return suggestDTO;
                }).collect(Collectors.toList());
        replenishmentResultDTO.setPurchaseSuggests(deliverySuggests);
    }

    /**
     * 获取建议发货日期
     *
     * @param localDate     断货日
     * @param stockUpResult 备货配置
     * @param now           计算日
     */
    private LocalDate getSuggestDeliveryDate(LocalDate localDate, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult, LocalDate now) {
        LocalDate suggestDeliveryDate = localDate.minusDays(stockUpResult.getPurchaseApproveDays())
                .minusDays(stockUpResult.getQcDays())
                .minusDays(stockUpResult.getProductionDays())
                .minusDays(stockUpResult.getPurchaseCycleDays())
                .minusDays(stockUpResult.getSupplierDeliveryDays())
                .minusDays(stockUpResult.getSafeDays());
        suggestDeliveryDate = suggestDeliveryDate.isBefore(now) ? now : suggestDeliveryDate;
        return suggestDeliveryDate;
    }

    /**
     * 获取建议发货量
     *
     * @param calculationDays      计算天数
     * @param salesEstimates       预估销量
     * @param stockingRatioResults 补货系数
     * @param now                  计算开始日期
     */
    private int getSuggestDeliveryQty(LocalDate calculationDays, List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates, List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResults, BigDecimal stockingRatio, LocalDate now) {
        BigDecimal totalSaleQty = BigDecimal.ZERO;
        while (now.isBefore(calculationDays)) {
            LocalDate date = now;
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
            now = now.plusDays(1);
        }
        return totalSaleQty.setScale(2, RoundingMode.CEILING).intValue();
    }
}
