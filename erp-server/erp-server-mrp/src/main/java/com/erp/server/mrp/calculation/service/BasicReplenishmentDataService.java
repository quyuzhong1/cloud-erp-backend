package com.erp.server.mrp.calculation.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
    @Resource
    private CfgRuleExpireTimeService cfgRuleExpireTimeService;
    @Resource
    private CfgRuleOverseasInstockDaysService cfgRuleOverseasInstockDaysService;
    @Resource
    private CfgRuleSafeDaysService cfgRuleSafeDaysService;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
    @Resource
    private CfgRuleWarehouseService cfgRuleWarehouseService;
    @Resource
    private CfgRuleWarehouseDetailService cfgRuleWarehouseDetailService;


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
        Map<String, ShopInfoEntity> shopInfoMap = allShopResult.getData().stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, v -> v, (o1, o2) -> o1));
        Map<String, Set<String>> shopSkuMap = new HashMap<>();
        for (String platform : platformList) {
            Set<String> shopIdList = allShopResult.getData().stream()
                    .filter(v -> platform.equals(v.getDictPlatform()))
                    .map(ShopInfoEntity::getId)
                    .collect(Collectors.toSet());
            CfgRuleSalesQtyEntity defaultCfgRuleSalesQty = cfgRuleSalesQtyService.getDefaultCfgRuleSalesQty(platform);
            String salesQtyType = defaultCfgRuleSalesQty.getSalesQtyType();
            //查询店铺有销量的sku
            Map<String, Set<String>> platformSkuMap;
            if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(salesQtyType)) {
                platformSkuMap = orderHistorySalesEsService.listSkuByShopId(shopIdList);
            } else {
                platformSkuMap = outStockHistorySalesEsService.listSkuByShopId(shopIdList);
            }
            if (!CollectionUtils.isEmpty(platformSkuMap)) {
                shopSkuMap.putAll(platformSkuMap);
            }
        }
        if (CollectionUtils.isEmpty(shopSkuMap)) {
            return;
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
        List<ReplenishmentSuggestionEntity> suggestions = getReplenishmentSuggestionList(type, shopSkuMap, oldReplenishmentMap, skuMap, shopInfoMap);
        replenishmentSuggestionService.saveOrUpdateBatch(suggestions);
    }

    /**
     * 异步执行获取需要更新的数据
     *
     * @param type                类型
     * @param shopSkuMap          店铺sku
     * @param oldReplenishmentMap 历史数据
     * @param skuMap              sku
     * @param shopInfoMap         店铺
     */
    private List<ReplenishmentSuggestionEntity> getReplenishmentSuggestionList(CfgRulePlatformTypeEnum type, Map<String, Set<String>> shopSkuMap,
                                                                               Map<String, ReplenishmentSuggestionEntity> oldReplenishmentMap,
                                                                               Map<String, String> skuMap, Map<String, ShopInfoEntity> shopInfoMap) {
        return shopSkuMap.entrySet().stream()
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
        //获取所有的时效配置
        List<CfgRuleExpireTimeEntity> cfgRuleExpireTimeList = cfgRuleExpireTimeService.list();
        //获取所有的物流时效配置
        List<CfgRuleLogisticsEntity> cfgRuleLogisticsList = cfgRuleLogisticsService.list();
        //获取物流时效明细配置
        List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetailList = cfgRuleLogisticsDetailService.list();
        //获取所有的海外仓入库天数明细
        List<CfgRuleOverseasInstockDaysEntity> cfgRuleOverseasInStockDaysList = cfgRuleOverseasInstockDaysService.list();
        //获取备货配置
        List<CfgRuleStockUpEntity> cfgRuleStockUpList = cfgRuleStockUpService.list();
        //获取备货系数
        List<CfgRuleStockingRatioEntity> cfgRuleStockingRatioList = cfgRuleStockingRatioService.list();
        //获取安全天数
        List<CfgRuleSafeDaysEntity> cfgRuleSafeDaysList = cfgRuleSafeDaysService.list();
        //获取销量配置
        List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList = cfgRuleSalesQtyService.list();
        //获取去噪配置
        List<CfgRuleSalesDenoisingEntity> cfgRuleSalesDenoisingList = cfgRuleSalesDenoisingService.list();
        //获取销量计算配置
        List<CfgRuleSalesFormulaEntity> cfgRuleSalesFormulaList = cfgRuleSalesFormulaService.list();
        //获取库存配置
        List<CfgRuleCommonDTO.StrategyResultDTO> cfgRuleInventory = cfgRuleCommonService.getCfgRuleCommon(CfgRuleCommonTypeEnum.INVENTORY.getCode());
        //获取建议配置
        List<CfgRuleCommonDTO.StrategyResultDTO> cfgRuleSuggest = cfgRuleCommonService.getCfgRuleCommon(CfgRuleCommonTypeEnum.SUGGEST.getCode());
        //获取仓库配置
        CfgRuleWarehouseEntity cfgRuleWarehouse = cfgRuleWarehouseService.getOne(Wrappers.emptyWrapper());
        //获取仓库明细配置
        List<CfgRuleWarehouseDetailEntity> cfgRuleWarehouseDetailList = cfgRuleWarehouseDetailService.listByMainIdList(Collections.singletonList(cfgRuleWarehouse.getId()));
        //获取策略配置
        List<CfgRuleOrderStrategyEntity> list = cfgRuleOrderStrategyService.list();
        CfgRuleOrderStrategyDTO.StrategyResultDTO orderResult = CollUtil.isEmpty(list) ? null : CfgRuleOrderStrategyDTO.StrategyResultDTO.buildStrategyResultDTO(list.get(0));
        //获取全部类型库存数据
        ReplenishmentInventoryDTO inventoryDTO = inventoryService.getAllInventoryQty(cfgRuleInventory, calculationDate);

        List<ReplenishmentResultDTO> suggestions = replenishmentSuggestionService.listAllCalculationData(platformType, cfgRuleSalesQtyList, calculationDate);
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
                        cfgRuleStrategy.setSettings(settings);
                        cfgRuleStrategy.setOrderResult(orderResult);
                        cfgRuleStrategy.setInventoryResult(cfgRuleInventory);
                        cfgRuleStrategy.setSuggestAmountResult(cfgRuleSuggest);
                        //获取仓库配置
                        getCfgRuleCommon(entity, cfgRuleStrategy, dto, cfgRuleWarehouse, cfgRuleWarehouseDetailList);
                        //获取时效配置
                        CfgRuleSettingStrategy<CfgRuleExpireTimeDTO.StrategyDTO, CfgRuleExpireTimeDTO.StrategyResultDTO> expireStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_EXPIRE_TIME.getCode());
                        CfgRuleExpireTimeDTO.StrategyResultDTO expireTimeResult = expireStrategy.process(CfgRuleExpireTimeDTO.StrategyDTO.buildStrategyDTO(dto, cfgRuleExpireTimeList, cfgRuleLogisticsList,
                                cfgRuleLogisticsDetailList, cfgRuleOverseasInStockDaysList));
                        cfgRuleStrategy.setExpireTimeResult(expireTimeResult);
                        //获取备货配置
                        CfgRuleSettingStrategy<CfgRuleStockUpDTO.StrategyDTO, CfgRuleStockUpDTO.StrategyResultDTO> stockUpStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_STOCK_UP.getCode());
                        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = stockUpStrategy.process(CfgRuleStockUpDTO.StrategyDTO.buildStrategyDTO(dto, cfgRuleStockUpList, cfgRuleStockingRatioList, cfgRuleSafeDaysList));
                        cfgRuleStrategy.setStockUpResult(stockUpResult);
                        //获取销量配置
                        CfgRuleSettingStrategy<CfgRuleSalesQtyDTO.StrategyDTO, CfgRuleSalesQtyDTO.StrategyResultDTO> salesStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_SALES_QTY.getCode());
                        CfgRuleSalesQtyDTO.StrategyResultDTO salesResult = salesStrategy.process(CfgRuleSalesQtyDTO.StrategyDTO.buildStrategyDTO(entity, detail.getSkuType(), cfgRuleSalesQtyList, cfgRuleSalesDenoisingList, cfgRuleSalesFormulaList));
                        cfgRuleStrategy.setSalesQtyResult(salesResult);
                        dto.setCfgRuleStrategy(cfgRuleStrategy);
                    }
                    stockingTimeHandler.handle(resultDTOS);
                    replenishmentSuggestionService.saveReplenishment(resultDTOS);
                    replenishmentTaskService.updateStatus(ids, SyncStatusEnum.SUCCESS_SYNC.getCode());
                } catch (Exception e) {
                    log.error("计算失败 sku{},平台 {}，店铺Id {}, 原因{}", resultDTOS.get(0).getReplenishment().getSkuNo(),resultDTOS.get(0).getReplenishment().getPlatform(), resultDTOS.get(0).getReplenishment().getShopId(),e.getMessage(), e);
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
     * @param basicDTO                   建议
     * @param cfgRuleStrategy            配置策略
     * @param resultDTO                  结果
     * @param cfgRuleWarehouse           仓库配置
     * @param cfgRuleWarehouseDetailList 仓库明细配置
     */
    private void getCfgRuleCommon(ReplenishmentResultDTO.BasicDTO basicDTO, CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO resultDTO,
                                  CfgRuleWarehouseEntity cfgRuleWarehouse, List<CfgRuleWarehouseDetailEntity> cfgRuleWarehouseDetailList) {
        //获取仓库配置
        CfgRuleSettingStrategy<CfgRuleWarehouseDTO.StrategyDTO, CfgRuleWarehouseDTO.StrategyResultDTO> warehouseStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_WAREHOUSE.getCode());
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = warehouseStrategy.process(new CfgRuleWarehouseDTO.StrategyDTO(basicDTO.getPlatformType(), basicDTO.getPlatform(),
                basicDTO.getShopId(), cfgRuleWarehouse, cfgRuleWarehouseDetailList));
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
                .filter(obj -> CharSequenceUtil.equals(basicDTO.getPlatform(),obj.getDictPlatform()) || ObjUtil.isEmpty(obj.getChannelIdJson()) || obj.getChannelIdJson().contains(basicDTO.getPlatform()))
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
     * @param calculationDate 计算日
     */
    public void isReplenishment(List<ReplenishmentSuggestionEntity> suggestions, LocalDate calculationDate) {
        String calcDate = calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        Map<String, CfgRuleSalesQtyEntity> cfgSalesQtyMap = getCfgRuleSalesQtyMap();
        //获取配置
        List<CfgSettingDTO> settings = cfgSettingService.listAllSetting();
        CfgSettingDTO newDaysSetting = settings.stream()
                .filter(v -> v.getKey().equals(CfgSettingEnum.NEW_DAYS.getCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.REPLENISHMENT_NEW_PRODUCT_RULE_CONFIG_NOT_EXIST));
        CfgSettingDTO replenishmentDaysSetting = settings.stream()
                .filter(v -> v.getKey().equals(CfgSettingEnum.REPLENISHMENT_DAYS.getCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.REPLENISHMENT_NEW_PRODUCT_RULE_CONFIG_NOT_EXIST));
        CfgSettingDTO.ReplenishmentDays replenishmentDays = JSON.parseObject(replenishmentDaysSetting.getDataJson(), CfgSettingDTO.ReplenishmentDays.class);
        Map<String, List<ReplenishmentSuggestionEntity>> platformSuggestionMap = suggestions.stream()
                .collect(Collectors.groupingBy(ReplenishmentSuggestionEntity::getPlatform));
        CompletableFuture.allOf(platformSuggestionMap.entrySet().stream()
                .map(suggestionMap -> CompletableFuture.runAsync(() -> {
                    List<String> shopSkuIds = suggestionMap.getValue().stream().map(v -> v.getShopId() + "-" + v.getSkuId()).distinct().collect(Collectors.toList());
                    CfgRuleSalesQtyEntity cfgSalesQty = cfgSalesQtyMap.get(suggestionMap.getKey());
                    Map<String, Integer> historySalesMap = getHistorySalesMap(calculationDate, cfgSalesQty, shopSkuIds, replenishmentDays);
                    List<String> skuIds = suggestionMap.getValue().stream().map(ReplenishmentSuggestionEntity::getSkuId).distinct().collect(Collectors.toList());
                    Map<String, SkuVO> skuVOMap = getSkuVOMap(skuIds);
                    Map<String, ProductSaleEntity> productSaleEntityMap = getProductSaleMap(skuIds);
                    List<String> notRestockingId = new ArrayList<>();
                    List<String> suggestionIds = new ArrayList<>();
                    List<String> restockingIds = new ArrayList<>();
                    List<ReplenishmentSuggestionDetailEntity> details = new ArrayList<>();
                    for (ReplenishmentSuggestionEntity entity : suggestionMap.getValue()) {
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
                        details.add(detail);
                        //判断是否需要补货
                        boolean isOver180Days = sale.getListingTime().plusDays(replenishmentDays.getStart()).isBefore(calculationDate);
                        int saleQty = Optional.ofNullable(historySalesMap.get(entity.getShopId() + "-" + entity.getSkuId())).orElse(0);
                        details.add(detail);
                        if (Boolean.TRUE.equals(isOver180Days) && saleQty == 0 &&
                                ReplenishmentTypeEnum.NORMAL.getCode().equals(entity.getReplenishmentType())) {
                            notRestockingId.add(entity.getId());
                        } else {
                            suggestionIds.add(entity.getId());
                            if (ReplenishmentTypeEnum.NOT_RESTOCKING.getCode().equals(entity.getReplenishmentType())) {
                                restockingIds.add(entity.getId());
                            }
                        }
                    }
                    replenishmentSuggestionDetailService.saveOrUpdateBatch(details);
                    replenishmentSuggestionService.batchRestockingReplenishment(restockingIds, "");
                    replenishmentSuggestionService.batchNotRestockingReplenishment(notRestockingId, "上市超180天，360天内无销量的商品，系统自动标记暂不补货");
                    replenishmentTaskService.saveTask(suggestionIds);
                }, threadPoolTaskExecutor)).toArray(CompletableFuture[]::new)).join();
    }

    /**
     * 获取sku销售信息
     *
     * @param skuIds sku
     */
    private Map<String, ProductSaleEntity> getProductSaleMap(List<String> skuIds) {
        List<ProductSaleEntity> productSaleList = plmTaskFeign.listProductSaleBySkuId(skuIds);
        return productSaleList.stream().collect(Collectors.toMap(ProductSaleEntity::getSkuId, v -> v, (o1, o2) -> o1));
    }

    /**
     * 获取sku数据
     *
     * @param skuIds skuId
     */
    private Map<String, SkuVO> getSkuVOMap(List<String> skuIds) {
        List<SkuVO> vos = plmTaskFeign.listSkuCostByIds(skuIds);
        return vos.stream().collect(Collectors.toMap(SkuVO::getSkuId, v -> v, (o1, o2) -> o1));
    }

    /**
     * 获取全部销量配置
     */
    private Map<String, CfgRuleSalesQtyEntity> getCfgRuleSalesQtyMap() {
        List<CfgRuleSalesQtyEntity> cfgSalesQtyList = cfgRuleSalesQtyService.list();
        return cfgSalesQtyList.stream().collect(Collectors.toMap(CfgRuleSalesQtyEntity::getPlatform, v -> v, (o1, o2) -> o1));
    }

    /**
     * 获取历史销量统计
     *
     * @param calculationDate   计算日
     * @param cfgSalesQty       销量配置
     * @param shopSkuIds        店铺skuId
     * @param replenishmentDays 补货开始结束实际
     */
    private Map<String, Integer> getHistorySalesMap(LocalDate calculationDate,
                                                    CfgRuleSalesQtyEntity cfgSalesQty, List<String> shopSkuIds,
                                                    CfgSettingDTO.ReplenishmentDays replenishmentDays) {
        Map<String, Integer> historySalesMap;
        // 获取历史销量
        if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(cfgSalesQty.getSalesQtyType())) {
            historySalesMap = orderHistorySalesEsService.countQtyByShopSkuIdsAndDate(shopSkuIds, cfgSalesQty.getOrderType(), calculationDate.minusDays(replenishmentDays.getEnd()), calculationDate.minusDays(1));
        } else {
            // 以销售出库单出库时间计算销量
            historySalesMap = outStockHistorySalesEsService.countQtyByShopSkuIdsAndDate(shopSkuIds, cfgSalesQty.getOrderType(), calculationDate.minusDays(replenishmentDays.getEnd()), calculationDate.minusDays(1));
        }
        return historySalesMap;
    }
}
