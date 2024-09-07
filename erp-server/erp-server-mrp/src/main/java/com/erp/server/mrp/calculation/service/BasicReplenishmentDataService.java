package com.erp.server.mrp.calculation.service;

import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.mrp.enums.CfgSettingEnum;
import com.erp.model.mrp.enums.ReplenishmentTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.service.*;
import com.google.common.collect.Lists;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class BasicReplenishmentDataService {

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private CfgPlatformMappingService cfgPlatformMappingService;
    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private ReplenishmentSuggestionDetailService replenishmentSuggestionDetailService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;

    /**
     * 增量变动建议补货基础数据
     */
    public void initReplenishmentSku() {
        //获取所有已审核且存在上市时间得非费用服务类sku
        List<SkuVO> vos = plmTaskFeign.listApproveAndListingSku();
        //获取已生成补货基础数据得信息
        List<ReplenishmentSuggestionEntity> replenishmentSuggestion = replenishmentSuggestionService.listAllSkuAndShop();
        List<String> suggestionList = replenishmentSuggestion.stream().map(v -> v.getSkuId() + "|" + v.getSkuNo() + "|" + v.getShopId()).collect(Collectors.toList());
        //获取所有店铺
        ApiResult<List<ShopInfoEntity>> allShopResult = shopInfoFeign.list();
        if (!allShopResult.isSuccess()) {
            throw new ServiceException(ApiError.ERROR_1023);
        }
        List<ReplenishmentSuggestionEntity> suggestionLists = new ArrayList<>();
        List<CfgPlatformMappingEntity> mappings = cfgPlatformMappingService.listByEffective();
        for (CfgPlatformMappingEntity mapping : mappings) {
            for (ShopInfoEntity shopInfo : allShopResult.getData()) {
                ReplenishmentSuggestionEntity entity = new ReplenishmentSuggestionEntity();
                entity.setShopId(shopInfo.getId());
                entity.setCountry(shopInfo.getDictCountryCode());
                entity.setPlatformType(mapping.getType());
                entity.setPlatform(shopInfo.getDictPlatform());
                suggestionLists.add(entity);
            }
        }
        //过滤掉已生成建议基础数据且sku_no未发生变化得sku
        List<ReplenishmentSuggestionEntity> replenishmentList = suggestionLists.parallelStream().map(v -> vos.parallelStream().map(e -> {
            if (suggestionList.contains(e.getSkuId() + "|" + e.getSkuNo() + "|" + v.getShopId())) {
                return null;
            }
            ReplenishmentSuggestionEntity entity = replenishmentSuggestion.stream().filter(suggestion -> suggestion.getSkuId().equals(e.getSkuId()) && suggestion.getShopId().equals(v.getShopId()))
                    .findFirst().orElse(new ReplenishmentSuggestionEntity());
            entity.setShopId(v.getShopId());
            entity.setCountry(v.getCountry());
            entity.setPlatformType(v.getPlatformType());
            entity.setPlatform(v.getPlatform());
            entity.setSkuId(e.getSkuId());
            entity.setSkuNo(e.getSkuNo());
            entity.setReplenishmentType(ObjectUtils.isEmpty(entity.getReplenishmentType()) ? ReplenishmentTypeEnum.NORMAL.getCode() : entity.getReplenishmentType());
            return entity;
        }).filter(Objects::nonNull).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());
        replenishmentSuggestionService.saveOrUpdateBatch(replenishmentList);
        // 发送mq计算明细数据
    }


    /**
     * 全量计算明细数据
     *
     * @param calculationDate 计算日期
     */
    public void calculationDetail(LocalDate calculationDate) {
        List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList = cfgRuleSalesQtyService.list();
        CfgSettingEntity newDaysSetting = cfgSettingService.getCfgSetting(CfgSettingEnum.NEW_DAYS.getCode());
        CfgSettingEntity replenishmentDaysSetting = cfgSettingService.getCfgSetting(CfgSettingEnum.REPLENISHMENT_DAYS.getCode());
        // 获取采购单价 todo

        //查询所有需要计算得数据
        List<ReplenishmentSuggestionEntity> suggestions = replenishmentSuggestionService.listCalculationData();
        List<String> skuIds = suggestions.stream().map(ReplenishmentSuggestionEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductSaleEntity> productSaleList = plmTaskFeign.listProductSaleBySkuId(skuIds);
        List<List<ReplenishmentSuggestionEntity>> partition = Lists.partition(suggestions, 1000);
        for (List<ReplenishmentSuggestionEntity> list : partition) {
            CompletableFuture.runAsync(() -> {
                for (ReplenishmentSuggestionEntity entity : list) {
                    try {
                        //计算是否新品
                        ProductSaleEntity sale = productSaleList.stream().filter(v -> v.getSkuId().equals(entity.getSkuId())).findFirst().orElse(null);
                        if (ObjectUtils.isEmpty(sale) || ObjectUtils.isEmpty(sale.getListingTime())) {
                            continue;
                        }
                        ReplenishmentSuggestionDetailEntity detail = new ReplenishmentSuggestionDetailEntity();
                        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JSRQ);
                        detail.setCalcVersion(code);
                        detail.setMainId(entity.getId());
                        if (sale.getListingTime().plusDays(Long.parseLong(newDaysSetting.getDataJson())).isAfter(LocalDate.now())) {
                            detail.setSkuType(CfgRuleStockingRatioTypeEnum.NEW.getCode());
                        } else {
                            detail.setSkuType(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
                        }
                        //获取sku对应系统配置
                        CfgRuleSalesQtyEntity cfgRuleSalesQty = cfgRuleSalesQtyList.stream()
                                .filter(v -> v.getPlatformType().equals(entity.getPlatformType()))
                                .filter(v -> v.getType().equals(detail.getSkuType()))
                                .findFirst()
                                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_SALES_NOT_EXIST,
                                        CfgRulePlatformTypeEnum.valueOf(entity.getPlatformType()).getName() + ":" + CfgRuleStockingRatioTypeEnum.getName(detail.getSkuType())));
                        //判断是否需要补货
//                        if (cfgRuleSalesQty.getSalesQtyType())

                    } catch (Exception e) {
                        entity.setRemark(e.getMessage());
                        replenishmentSuggestionService.updateById(entity);
                    }
                }


            }, threadPoolTaskExecutor);
        }
    }


    public static void main(String[] args) {

    }
}
