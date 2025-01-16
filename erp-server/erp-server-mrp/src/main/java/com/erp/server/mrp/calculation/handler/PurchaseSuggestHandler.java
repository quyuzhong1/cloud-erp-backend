package com.erp.server.mrp.calculation.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.config.DocNoGenHelper;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSuggestedAmountNodeEnum;
import com.erp.model.mrp.enums.CfgSettingEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.wms.enums.ExecutionTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.convert.PurchaseSuggestConverter;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
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
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return recentSuggestionHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO dto) {
        return true;
    }

    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {
        //无补货数据则无需新增采购建议
        if (CollUtil.isEmpty(replenishmentResultDTO.getDeliverySuggests())) {
            return;
        }

        CfgRuleStrategyDTO cfgRuleStrategyDTO = replenishmentResultDTO.getCfgRuleStrategy();
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = cfgRuleStrategyDTO.getStockUpResult();
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult = stockUpResult.getLogisticsResult();
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
        //发货信息
        Map<LocalDate, List<ReplenishmentResultDTO.DeliverySuggestDTO>> deliveryMap = replenishmentResultDTO.getDeliverySuggests().stream().collect(Collectors.groupingBy(ReplenishmentResultDTO.DeliverySuggestDTO::getSuggestPurchaseDate));
        List<ReplenishmentResultDTO.PurchaseSuggestDTO> purchaseSuggests = deliveryMap.entrySet().parallelStream()
                .flatMap(entry -> {

                    List<ReplenishmentResultDTO.DeliverySuggestDTO> value = entry.getValue();

                    List<String> deliverySuggestIdList = value.stream().map(ReplenishmentResultDTO.DeliverySuggestDTO::getId).distinct().collect(Collectors.toList());
                    //查询关联的发货计划
                    ReplenishmentResultDTO.PurchaseSuggestDTO parentSuggestDTO = ReplenishmentResultDTO.PurchaseSuggestDTO.buildPurchaseSuggestDTO(logisticsResult, replenishmentResultDTO, ExecutionTypeEnum.AUTO.getCode(),deliverySuggestIdList);
                    //根据suggestDTO判断是否需要拆分生成多条建议
                    List<ReplenishmentResultDTO.PurchaseSuggestDTO> purchaseSuggestList = generateMultipleSuggest(replenishmentResultDTO, parentSuggestDTO);
                    //建议发货量合计
                    Integer totalSuggestQty = value.stream().map(ReplenishmentResultDTO.DeliverySuggestDTO::getSuggestDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);

                    for (ReplenishmentResultDTO.PurchaseSuggestDTO suggestDTO : purchaseSuggestList) {
                        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())
                                || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(replenishmentResultDTO.getReplenishment().getPlatformType())) {
                            String code = docNoGenHelper.generateCode(CODE_P);
                            suggestDTO.setCode(code);
                            suggestDTO.setId(IdWorker.getIdStr());
                            suggestDTO.setSuggestPurchaseDate(entry.getKey());
                            //预计可售日期 （本地发FBA）= 建议采购日 +（审批时长 + 采购交期 + 供应商发货时效 +质检天数）+（本地发FBA时效 + FBA入库时间）
                            //预计可售日期 （本地发海外）= 建议采购日 +（审批时长 + 采购交期 + 供应商发货时效 +质检天数）+（本地发海外时效 + 海外仓入库时间）
                            LocalDate estimateSalesDate = suggestDTO.getSuggestPurchaseDate().plusDays(stockUpResult.getPurchaseApproveDays()).plusDays(stockUpResult.getProductionDays())
                                    .plusDays(stockUpResult.getSupplierDeliveryDays()).plusDays(stockUpResult.getQcDays())
                                    .plusDays(logisticsResult.getLogisticsDays()).plusDays(stockUpResult.getInstockDays());
                            suggestDTO.setEstimateSalesDate(estimateSalesDate);
                            //预计入库日期 （本地发FBA）= 建议采购日 +（审批时长 + 采购交期 + 供应商发货时效 + 质检天数+ 采购频率）
                            //预计入库日期 （本地发海外）= 建议采购日 +（审批时长 + 采购交期 + 供应商发货时效 + 质检天数+ 采购频率）
                            LocalDate estimateInstockDate = suggestDTO.getSuggestPurchaseDate().plusDays(stockUpResult.getPurchaseApproveDays()).plusDays(stockUpResult.getProductionDays())
                                    .plusDays(stockUpResult.getSupplierDeliveryDays()).plusDays(stockUpResult.getQcDays()).plusDays(stockUpResult.getPurchaseCycleDays());
                            suggestDTO.setEstimateInstockDate(estimateInstockDate);
                            //建议采购量
                            LocalDate calcDate = suggestDTO.getSuggestPurchaseDate().plusDays(Math.min(agingDays, days));
                            //按是否存在bom重新赋值
                            int suggestDeliveryQty = CharSequenceUtil.isBlank(suggestDTO.getBomVersion()) ? totalSuggestQty : totalSuggestQty * suggestDTO.getQuantity();
                            suggestDTO.setSuggestDeliveryQty(suggestDeliveryQty);

                            int inventory = inventoryService.getInventoryByPurchaseSuggest(suggestDTO,replenishmentResultDTO, calcDate, purchaseVolumeInventory);
                            suggestDTO.setInventoryQty(inventory);
                            suggestDTO.setSuggestPurchaseQty(Math.max(0, suggestDeliveryQty - inventory));
                        }
                        //采购成本 = 采购单价 * 建议采购量，取一供 ＞ 二供
                        if (!ObjectUtils.isEmpty(replenishmentResultDTO.getPurchasePrice())) {
                            suggestDTO.setPurchaseCost(replenishmentResultDTO.getPurchasePrice().multiply(BigDecimal.valueOf(suggestDTO.getSuggestPurchaseQty())));
                        }
                    }
                    return purchaseSuggestList.stream();
                }).filter(v -> v.getSuggestPurchaseQty() > 0).collect(Collectors.toList());
        replenishmentResultDTO.setPurchaseSuggests(purchaseSuggests);
    }


    /**
     * 拆分建议数据
     * @Auther will
     * @Date 2025/1/10 09:05
     * @param replenishmentResultDTO
     * @return List<ReplenishmentResultDTO.PurchaseSuggestDTO>
     */
    private List<ReplenishmentResultDTO.PurchaseSuggestDTO> generateMultipleSuggest(ReplenishmentResultDTO replenishmentResultDTO,ReplenishmentResultDTO.PurchaseSuggestDTO suggestDTO) {
        List<BomChildrenSkuDTO> bomSkuList = replenishmentResultDTO.getBomSkuList();
        //无bom则返回
        if (CollUtil.isEmpty(bomSkuList)) {
            return Collections.singletonList(suggestDTO);
        }
        //无配置或者不拆分也直接返回
        CfgRuleOrderStrategyDTO.StrategyResultDTO orderResult = replenishmentResultDTO.getCfgRuleStrategy().getOrderResult();
        if (ObjectUtil.isEmpty(orderResult) || Boolean.TRUE.equals(!orderResult.getIsSplit())) {
            return Collections.singletonList(suggestDTO);
        }
        List<ReplenishmentResultDTO.PurchaseSuggestDTO> purchaseSuggests = new ArrayList<>();
        for (BomChildrenSkuDTO bomSku : bomSkuList) {
            ReplenishmentResultDTO.PurchaseSuggestDTO childSuggestDTO = PurchaseSuggestConverter.INSTANCE.copyPurchaseSuggest(suggestDTO);
            childSuggestDTO.setSkuId(bomSku.getSkuId());
            childSuggestDTO.setSkuNo(bomSku.getSkuNo());
            childSuggestDTO.setParentSkuId(bomSku.getParentSkuId());
            childSuggestDTO.setQuantity(bomSku.getQuantity());
            childSuggestDTO.setBomVersion(bomSku.getBomVersion());
            purchaseSuggests.add(childSuggestDTO);
        }
        return purchaseSuggests;
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
