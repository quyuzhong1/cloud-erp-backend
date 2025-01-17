package com.erp.server.mrp.calculation.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.calculation.factory.CfgSettingFactory;
import com.erp.server.mrp.calculation.factory.PlatformCalculationFactory;
import com.erp.server.mrp.calculation.handler.StockingTimeHandler;
import com.erp.server.mrp.calculation.strategy.CfgRuleSettingStrategy;
import com.erp.server.mrp.calculation.strategy.platform.PlatformCalculationStrategy;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.erp.server.mrp.es.service.OutStockHistorySalesEsService;
import com.erp.server.mrp.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BasicReplenishmentDataService {

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
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
    @Resource
    private CfgSettingFactory cfgSettingFactory;
    @Resource
    private CfgRuleStockUpService cfgRuleStockUpService;

    @Resource
    private CfgRuleStockingRatioService cfgRuleStockingRatioService;
    @Resource
    private CfgRuleLogisticsDetailService cfgRuleLogisticsDetailService;
    @Resource
    private CfgRuleSalesFormulaService cfgRuleSalesFormulaService;
    @Resource
    private CfgRuleSalesDenoisingService cfgRuleSalesDenoisingService;

    @Resource
    private CfgRuleLogisticsService cfgRuleLogisticsService;
    @Resource
    private StockingTimeHandler stockingTimeHandler;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private ReplenishmentTaskService replenishmentTaskService;
    @Resource
    private PlatformCalculationFactory platformCalculationFactory;
    @Resource
    private OrderHistorySalesEsService orderHistorySalesEsService;
    @Resource
    private OutStockHistorySalesEsService outStockHistorySalesEsService;
    @Resource
    private CfgRuleOrderStrategyService cfgRuleOrderStrategyService;

    /**
     * 增量变动建议补货基础数据
     */
    public void initReplenishmentSku(CfgRulePlatformTypeEnum type, List<String> platformList) {

        if (CollectionUtils.isEmpty(platformList)) {
            return;
        }
        //获取所有店铺
        ApiResult<List<ShopInfoEntity>> allShopResult = shopInfoFeign.list();
        if (!allShopResult.isSuccess()) {
            throw new ServiceException(allShopResult.getMsg());
        }
        List<ShopInfoEntity> shopInfoList = allShopResult.getData().stream()
                .filter(v -> platformList.contains(v.getDictPlatform()))
                .collect(Collectors.toList());
        Map<String, ShopInfoEntity> shopInfoMap = shopInfoList.stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, v -> v, (o1, o2) -> o1));
        List<CfgRuleSalesQtyEntity> defaultCfgRuleSalesQty = cfgRuleSalesQtyService.listDefaultCfgRuleSalesQty(type.getCode());
        //查询店铺有销量的sku
        Map<String, Set<String>> shopSkuMap;
        String salesQtyType = defaultCfgRuleSalesQty.get(0).getSalesQtyType();
        if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(salesQtyType)) {
            shopSkuMap = orderHistorySalesEsService.listSkuByShopId(shopInfoMap.keySet());
        } else {
            shopSkuMap = outStockHistorySalesEsService.listSkuByShopId(shopInfoMap.keySet());
        }
        List<String> skuIds = shopSkuMap.values()
                .stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, String> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuNo, (o1, o2) -> o1));
        //获取已生成补货基础数据得信息
        List<ReplenishmentSuggestionEntity> replenishmentSuggestion = replenishmentSuggestionService.listAllSkuAndShop(type.getCode());
        Map<String, ReplenishmentSuggestionEntity> oldReplenishmentMap = replenishmentSuggestion.stream()
                .collect(Collectors.toMap(v -> v.getSkuId() + ":" + v.getShopId(), v -> v, (o1, o2) -> o1));
        List<ReplenishmentSuggestionEntity> suggestions = shopSkuMap.entrySet().stream()
                .map(v -> CompletableFuture.supplyAsync(
                        () -> {
                            List<ReplenishmentSuggestionEntity> suggestionLists = new ArrayList<>();
                            for (String skuId : v.getValue()) {
                                ReplenishmentSuggestionEntity entity = Optional.ofNullable(oldReplenishmentMap.get(skuId + ":" + v.getKey())).orElse(new ReplenishmentSuggestionEntity());
                                String skuNo = skuMap.get(skuId);
                                // 过滤掉已生成建议且 sku_no 未发生变化的 SKU，或未启用的平台
                                if (ObjectUtils.isEmpty(skuNo) || (!ObjectUtils.isEmpty(entity.getId()) && entity.getSkuNo().equals(skuNo))) {
                                    continue;
                                }
                                ShopInfoEntity shopInfo = shopInfoMap.get(v.getKey());
                                entity.setShopId(shopInfo.getId());
                                entity.setArea(shopInfo.getDictAreaCode());
                                entity.setFbaWarehouseId(shopInfo.getWarehouseId());
                                entity.setCountry(shopInfo.getDictCountryCode());
                                entity.setPlatformType(type.getCode());
                                entity.setPlatform(shopInfo.getDictPlatform());
                                entity.setSkuId(skuId);
                                entity.setSkuNo(skuNo);
                                entity.setReplenishmentType(ReplenishmentTypeEnum.NORMAL.getCode());
                                suggestionLists.add(entity);
                            }
                            return suggestionLists;
                        },
                        threadPoolTaskExecutor))
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)  // 等待每个 CompletableFuture 完成
                .flatMap(List::stream)
                .collect(Collectors.toList());
        replenishmentSuggestionService.saveOrUpdateBatch(suggestions);
    }


    /**
     * 全量计算明细数据
     */
    public void calculationDetail(String platformType, LocalDate calculationDate) {
        ApiResult<List<ShopInfoEntity>> allShopResult = shopInfoFeign.list();
        if (!allShopResult.isSuccess()) {
            throw new ServiceException(allShopResult.getMsg());
        }
        Map<String, List<String>> shopIdByPlatform = allShopResult.getData().stream()
                .collect(Collectors.groupingBy(ShopInfoEntity::getDictPlatform, Collectors.mapping(ShopInfoEntity::getId, Collectors.toList())));
        List<CfgSettingDTO> settings = cfgSettingService.listAllSetting();
        //获取备货默认配置
        CfgRuleStockUpEntity defaultStockUp = cfgRuleStockUpService.getDefaultByPlatform(platformType);
        List<CfgRuleStockingRatioEntity> defaultStockingRatioList = cfgRuleStockingRatioService.listByStockUpIdList(Collections.singletonList(defaultStockUp.getId()));
        List<CfgRuleLogisticsEntity> defaultLogisticsList = cfgRuleLogisticsService.listByStockUpIdList(Collections.singletonList(defaultStockUp.getId()));
        List<String> defaultLogisticsIds = defaultLogisticsList.stream().map(CfgRuleLogisticsEntity::getId).collect(Collectors.toList());
        List<CfgRuleLogisticsDetailEntity> defaultLogisticsDetailList = cfgRuleLogisticsDetailService.listByMainIdList(defaultLogisticsIds);
        //获取销量默认配置
        List<CfgRuleSalesQtyEntity> defaultCfgRuleSalesQty = cfgRuleSalesQtyService.listDefaultCfgRuleSalesQty(platformType);
        List<String> defaultSalesQtyIds = defaultCfgRuleSalesQty.stream().map(CfgRuleSalesQtyEntity::getId).collect(Collectors.toList());
        List<CfgRuleSalesFormulaEntity> defaultFormulaList = cfgRuleSalesFormulaService.listBySalesQtyIdList(defaultSalesQtyIds);
        List<CfgRuleSalesDenoisingEntity> defaultDenoisingList = cfgRuleSalesDenoisingService.listBySalesQtyIdList(defaultSalesQtyIds);
        //获取库存配置
        CfgRuleSettingStrategy<CfgRuleCommonDTO.StrategyDTO, List<CfgRuleCommonDTO.StrategyResultDTO>> inventoryStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_INVENTORY.getCode());
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = inventoryStrategy.process(new CfgRuleCommonDTO.StrategyDTO(platformType));
        ReplenishmentInventoryDTO inventoryDTO = inventoryService.getAllInventoryQty(inventoryResult, platformType, calculationDate);
        //获取策略配置
        List<CfgRuleOrderStrategyEntity> list = cfgRuleOrderStrategyService.list();
        CfgRuleOrderStrategyDTO.StrategyResultDTO orderResult = CollUtil.isEmpty(list) ? null : CfgRuleOrderStrategyDTO.StrategyResultDTO.buildStrategyResultDTO(list.get(0));
        //获取建议配置
        CfgRuleSettingStrategy<CfgRuleCommonDTO.StrategyDTO, List<CfgRuleCommonDTO.StrategyResultDTO>> suggestedStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_SUGGESTED_AMOUNT.getCode());
        List<CfgRuleCommonDTO.StrategyResultDTO> suggestResult = suggestedStrategy.process(new CfgRuleCommonDTO.StrategyDTO(platformType));
        List<ReplenishmentResultDTO> suggestions = replenishmentSuggestionService.listAllCalculationData(platformType, defaultCfgRuleSalesQty.get(0).getSalesQtyType(), defaultCfgRuleSalesQty.get(0).getOrderType(), calculationDate);
        //以sku为维度分组
        Map<String, List<ReplenishmentResultDTO>> listMap = suggestions.stream()
                .collect(Collectors.groupingBy(v -> v.getReplenishment().getSkuId()));
        //查询所有需要计算得数据
        for (List<ReplenishmentResultDTO> resultDTOS : listMap.values()) {
            CompletableFuture.runAsync(() -> {
                List<String> ids = resultDTOS.stream()
                        .map(v -> v.getReplenishment().getId())
                        .collect(Collectors.toList());
                try {
                    replenishmentTaskService.updateStatus(ids, SyncStatusEnum.IN_SYNC.getCode());
                    for (ReplenishmentResultDTO dto : resultDTOS) {
                        ReplenishmentResultDTO.BasicDTO entity = dto.getReplenishment();
                        ReplenishmentResultDTO.DetailDTO detail = dto.getReplenishmentDetail();
                        dto.setInventoryDTO(inventoryDTO);
                        dto.setShopIdByPlatform(shopIdByPlatform);
                        //初始化配置
                        CfgRuleStrategyDTO cfgRuleStrategy = new CfgRuleStrategyDTO();
                        //获取销量配置
                        CfgRuleSalesQtyEntity defaultSalesQty = defaultCfgRuleSalesQty.stream()
                                .filter(v -> v.getType().equals(detail.getSkuType()))
                                .findFirst()
                                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_SALES_NOT_EXIST, CfgRulePlatformTypeEnum.getName(entity.getPlatformType())));
                        List<CfgRuleSalesFormulaEntity> defaultFormula = defaultFormulaList.stream()
                                .filter(v -> v.getSalesQtyId().equals(defaultSalesQty.getId()))
                                .collect(Collectors.toList());
                        List<CfgRuleSalesDenoisingEntity> defaultDenoising = defaultDenoisingList.stream()
                                .filter(v -> v.getSalesQtyId().equals(defaultSalesQty.getId()))
                                .collect(Collectors.toList());
                        CfgRuleSettingStrategy<CfgRuleSalesQtyDTO.StrategyDTO, CfgRuleSalesQtyDTO.StrategyResultDTO> salesStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_SALES_QTY.getCode());
                        CfgRuleSalesQtyDTO.StrategyResultDTO salesResult = salesStrategy.process(CfgRuleSalesQtyDTO.StrategyDTO.buildStrategyDTO(entity, detail.getSkuType(), defaultSalesQty, defaultFormula, defaultDenoising));
                        cfgRuleStrategy.setSalesQtyResult(salesResult);
                        cfgRuleStrategy.setSettings(settings);

                        //获取仓库配置
                        getCfgRuleCommon(entity, cfgRuleStrategy, dto);
                        //获取备货配置
                        List<CfgRuleStockingRatioEntity> defaultStockingRatio = defaultStockingRatioList.stream()
                                .filter(v -> v.getStockUpId().equals(defaultStockUp.getId()))
                                .collect(Collectors.toList());
                        List<CfgRuleLogisticsEntity> defaultLogistics = defaultLogisticsList.stream()
                                .filter(v -> v.getStockUpId().equals(defaultStockUp.getId()))
                                .collect(Collectors.toList());
                        List<String> defaultLogisticsByPlatformIds = defaultLogistics.stream().map(CfgRuleLogisticsEntity::getId).collect(Collectors.toList());
                        List<CfgRuleLogisticsDetailEntity> logisticsDetails = defaultLogisticsDetailList.stream()
                                .filter(v -> defaultLogisticsByPlatformIds.contains(v.getMainId()))
                                .collect(Collectors.toList());
                        CfgRuleSettingStrategy<CfgRuleStockUpDTO.StrategyDTO, CfgRuleStockUpDTO.StrategyResultDTO> stockUpStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_STOCK_UP.getCode());
                        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = stockUpStrategy.process(CfgRuleStockUpDTO.StrategyDTO.buildStrategyDTO(dto, defaultStockUp, defaultStockingRatio, defaultLogistics, logisticsDetails));
                        cfgRuleStrategy.setStockUpResult(stockUpResult);
                        cfgRuleStrategy.setInventoryResult(inventoryResult);
                        cfgRuleStrategy.setSuggestAmountResult(suggestResult);
                        cfgRuleStrategy.setOrderResult(orderResult);
                        dto.setCfgRuleStrategy(cfgRuleStrategy);
                    }
                    stockingTimeHandler.handle(resultDTOS);
                    replenishmentSuggestionService.saveReplenishment(resultDTOS);
                    replenishmentTaskService.updateStatus(ids, SyncStatusEnum.SUCCESS_SYNC.getCode());
                } catch (Exception e) {
                    log.error("计算失败 sku{}, 原因{}", resultDTOS.get(0).getReplenishment().getSkuNo(), e.getMessage(), e);
                    replenishmentTaskService.updateStatus(ids, SyncStatusEnum.FAILED_SYNC.getCode(), e.getMessage());
                }
            }, threadPoolTaskExecutor);
        }
    }


    /**
     * 更新数据
     *
     * @param id 主表id
     */
    public BatchResultDTO renewData(String id) {

        return null;
    }

    /**
     * 获取库存，建议相关默认配置
     *
     * @param basicDTO        建议
     * @param cfgRuleStrategy 配置策略
     * @param resultDTO       结果
     */
    private void getCfgRuleCommon(ReplenishmentResultDTO.BasicDTO basicDTO, CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO resultDTO) {
        //获取仓库配置
        CfgRuleSettingStrategy<CfgRuleWarehouseDTO.StrategyDTO, CfgRuleWarehouseDTO.StrategyResultDTO> warehouseStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_WAREHOUSE.getCode());
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = warehouseStrategy.process(new CfgRuleWarehouseDTO.StrategyDTO(basicDTO.getPlatformType(), basicDTO.getPlatform(), basicDTO.getShopId()));
        cfgRuleStrategy.setWarehouseResult(warehouseResult);
        //获取店铺对应的本地仓，海外仓
        if (Boolean.TRUE.equals(warehouseResult.getIsEnableVirtual())) {
            List<String> localWarehouse = warehouseResult.getLocalWarehouseList()
                    .stream()
                    .filter(v -> !ObjectUtils.isEmpty(v.getVirtualWarehouseId()))
                    .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::getWarehouseId)
                    .collect(Collectors.toList());
            resultDTO.setLocalWarehouseId(localWarehouse);
        } else {
            List<String> localWarehouse = warehouseResult.getLocalWarehouseList()
                    .stream()
                    .filter(v -> ObjectUtils.isEmpty(v.getVirtualWarehouseId()))
                    .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::getWarehouseId)
                    .collect(Collectors.toList());
            resultDTO.setLocalWarehouseId(localWarehouse);
        }
        List<String> overseasWarehouse = warehouseResult.getOverseasWarehouseList()
                .stream()
                .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::getWarehouseId)
                .collect(Collectors.toList());
        resultDTO.setOverseasWarehouseId(overseasWarehouse);
    }

    /**
     * 清洗历史库存
     *
     * @param calculationDate 计算日
     * @param platformType    平台类型
     * @param cleanDay        清洗天数
     */
    public void cleanHistoryInventory(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, CfgRulePlatformTypeEnum platformType, Integer cleanDay) {
        PlatformCalculationStrategy platformCalculation = platformCalculationFactory.getPlatformCalculation(platformType.getCode());
        platformCalculation.cleanHistoryInventory(calculationDate, suggestions, cleanDay);
    }

    /**
     * 清洗历史销量
     *
     * @param calculationDate 计算日
     * @param platformType    平台类型
     * @param cleanDay        清洗天数
     */
    public void cleanHistorySalesByOrder(LocalDate calculationDate, CfgRulePlatformTypeEnum platformType, Integer cleanDay) {
        PlatformCalculationStrategy platformCalculation = platformCalculationFactory.getPlatformCalculation(platformType.getCode());
        platformCalculation.cleanHistorySalesByOrder(calculationDate, cleanDay);
    }


    /**
     * 清洗历史销量
     *
     * @param calculationDate 计算日
     * @param platformType    平台类型
     * @param cleanDay        清洗天数
     */
    public void cleanHistorySalesByOutStock(LocalDate calculationDate, CfgRulePlatformTypeEnum platformType, Integer cleanDay) {
        PlatformCalculationStrategy platformCalculation = platformCalculationFactory.getPlatformCalculation(platformType.getCode());
        platformCalculation.cleanHistorySalesByOutStock(calculationDate, cleanDay);
    }

    /**
     * 判断是否需要补货
     *
     * @param suggestions     建议id
     * @param platformType    平台类型
     * @param calculationDate 计算日
     */
    public void isReplenishment(List<ReplenishmentSuggestionEntity> suggestions, String platformType, LocalDate calculationDate) {
        String calcDate = calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        CfgRuleSalesQtyEntity cfgSalesQty = cfgRuleSalesQtyService.getDefaultCfgRuleSalesQty(platformType);
        //获取配置
        List<CfgSettingDTO> settings = cfgSettingService.listAllSetting();
        CfgSettingDTO newDaysSetting = settings.stream()
                .filter(v -> v.getKey().equals(CfgSettingEnum.NEW_DAYS.getCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_NEWS_NOT_EXIST));
        CfgSettingDTO replenishmentDaysSetting = settings.stream()
                .filter(v -> v.getKey().equals(CfgSettingEnum.REPLENISHMENT_DAYS.getCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_NEWS_NOT_EXIST));
        CfgSettingDTO.ReplenishmentDays replenishmentDays = JSON.parseObject(replenishmentDaysSetting.getDataJson(), CfgSettingDTO.ReplenishmentDays.class);
        List<List<ReplenishmentSuggestionEntity>> partition = Lists.partition(suggestions, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(suggestionList -> CompletableFuture.runAsync(() -> {
                    List<String> shopSkuIds = suggestionList.stream().map(v -> v.getShopId() + "-" + v.getSkuId()).distinct().collect(Collectors.toList());
                    Map<String, Integer> historySalesMap;
                    // 获取历史销量
                    if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(cfgSalesQty.getSalesQtyType())) {
                        historySalesMap = orderHistorySalesEsService.countQtyByShopSkuIdsAndDate(shopSkuIds, cfgSalesQty.getOrderType(), calculationDate.minusDays(replenishmentDays.getEnd()), calculationDate.minusDays(1));
                    } else {
                        // 以销售出库单出库时间计算销量
                        historySalesMap = outStockHistorySalesEsService.countQtyByShopSkuIdsAndDate(shopSkuIds, cfgSalesQty.getOrderType(), calculationDate.minusDays(replenishmentDays.getEnd()), calculationDate.minusDays(1));
                    }
                    List<String> skuIds = suggestionList.stream().map(ReplenishmentSuggestionEntity::getSkuId).distinct().collect(Collectors.toList());
                    List<SkuVO> vos = plmTaskFeign.listSkuCostByIds(skuIds);
                    List<ProductSaleEntity> productSaleList = plmTaskFeign.listProductSaleBySkuId(skuIds);
                    Map<String, SkuVO> skuVOMap = vos.stream().collect(Collectors.toMap(SkuVO::getSkuId, v -> v, (o1, o2) -> o1));
                    Map<String, ProductSaleEntity> productSaleEntityMap = productSaleList.stream().collect(Collectors.toMap(ProductSaleEntity::getSkuId, v -> v, (o1, o2) -> o1));
                    List<String> notRestockingId = new ArrayList<>();
                    List<String> suggestionIds = new ArrayList<>();
                    List<ReplenishmentSuggestionDetailEntity> details = new ArrayList<>();
                    for (ReplenishmentSuggestionEntity entity : suggestionList) {
                        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JSRQ, calculationDate);
                        SkuVO skuVO = Optional.ofNullable(skuVOMap.get(entity.getSkuId())).orElse(new SkuVO());
                        ReplenishmentSuggestionDetailEntity detail = ReplenishmentResultDTO.DetailDTO.buildNewDetail(code, calcDate, entity.getId());
                        detail.setSalesPrice(skuVO.getRetailPrice());
                        //计算是否新品
                        ProductSaleEntity sale = productSaleEntityMap.get(entity.getSkuId());
                        if (ObjectUtils.isEmpty(sale) || ObjectUtils.isEmpty(sale.getListingTime())) {
                            continue;
                        }
                        if (sale.getListingTime().plusDays(Long.parseLong(newDaysSetting.getDataJson())).isAfter(LocalDate.now())) {
                            detail.setSkuType(CfgRuleStockingRatioTypeEnum.NEW.getCode());
                        } else {
                            detail.setSkuType(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
                        }
                        //判断是否需要补货

                        boolean isOver180Days = sale.getListingTime().plusDays(replenishmentDays.getStart()).isBefore(calculationDate);
                        int saleQty = Optional.ofNullable(historySalesMap.get(entity.getId())).orElse(0);
                        details.add(detail);
                        if (Boolean.TRUE.equals(isOver180Days) && saleQty == 0 &&
                                ReplenishmentTypeEnum.NORMAL.getCode().equals(entity.getReplenishmentType())) {
                            notRestockingId.add(entity.getId());
                        } else {
                            suggestionIds.add(entity.getId());
                        }
                    }
                    replenishmentSuggestionDetailService.saveOrUpdateBatch(details);
                    replenishmentSuggestionService.batchNotRestockingReplenishment(notRestockingId, "上市超180天，360天内无销量的商品，系统自动标记暂不补货");
                    replenishmentTaskService.saveTask(suggestionIds);
                }, threadPoolTaskExecutor)).toArray(CompletableFuture[]::new)).join();

    }
}
