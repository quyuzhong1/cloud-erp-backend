package com.erp.server.mrp.calculation.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.calculation.factory.CfgSettingFactory;
import com.erp.server.mrp.calculation.handler.StockingTimeHandler;
import com.erp.server.mrp.calculation.strategy.CfgRuleSettingStrategy;
import com.erp.server.mrp.mapper.InventoryMapper;
import com.erp.server.mrp.service.*;
import com.google.common.collect.Lists;
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
    @Resource
    private CfgSettingFactory cfgSettingFactory;
    @Resource
    private SalesService salesService;
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
    private FbaHistoryInventoryService fbaHistoryInventoryService;
    @Resource
    private OverseasHistoryInventoryService overseasHistoryInventoryService;
    @Resource
    private LocalHistoryInventoryService localHistoryInventoryService;
    @Resource
    private SalesInfoService salesInfoService;
    @Resource
    private InventoryMapper inventoryMapper;

    /**
     * 增量变动建议补货基础数据
     */
    public void initReplenishmentSku(LocalDate calculationDate, String id) {
        calculationDate = ObjectUtils.isEmpty(calculationDate) ? LocalDate.now() : calculationDate;
        //清洗每日库存到历史表
        List<FbaInventoryEntity> inventoryEntities = inventoryMapper.getAllFbaHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.FBA_INVENTORY, calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE)));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            fbaHistoryInventoryService.saveTodayInventory(inventoryEntities, calculationDate);
        }
        List<OverseasInventoryEntity> overseasHistoryInventory = inventoryMapper.getAllOverseasHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.OVERSEAS_INVENTORY, calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE)));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            overseasHistoryInventoryService.saveTodayInventory(overseasHistoryInventory, calculationDate);
        }
        List<InventoryEntity> localHistoryInventory = inventoryMapper.getAllLocalHistoryInventory(SnapshotTableEnum.getTableName(SnapshotTableEnum.INVENTORY, calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE)));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            localHistoryInventoryService.saveTodayInventory(localHistoryInventory, calculationDate);
        }
        if (!ObjectUtils.isEmpty(id)) {
            cleanHistorySalesAndInventory(calculationDate, id);
            return;
        }
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
                //跳过店铺不是当前循环平台的数据
                if (!mapping.getPlatform().equals(shopInfo.getDictPlatform())) {
                    continue;
                }
                ReplenishmentSuggestionEntity entity = new ReplenishmentSuggestionEntity();
                entity.setShopId(shopInfo.getId());
                entity.setArea(shopInfo.getDictAreaCode());
                entity.setFbaWarehouseId(shopInfo.getWarehouseId());
                entity.setCountry(shopInfo.getDictCountryCode());
                entity.setPlatformType(PlatformMappingTypeEnum.getEnum(mapping.getType()).getPlatformType().getCode());
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
            entity.setArea(v.getArea());
            entity.setFbaWarehouseId(v.getFbaWarehouseId());
            entity.setCountry(v.getCountry());
            entity.setPlatformType(v.getPlatformType());
            entity.setPlatform(v.getPlatform());
            entity.setSkuId(e.getSkuId());
            entity.setSkuNo(e.getSkuNo());
            entity.setReplenishmentType(ObjectUtils.isEmpty(entity.getReplenishmentType()) ? ReplenishmentTypeEnum.NORMAL.getCode() : entity.getReplenishmentType());
            return entity;
        }).filter(Objects::nonNull).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());
        replenishmentSuggestionService.saveOrUpdateBatch(replenishmentList);
        cleanHistorySalesAndInventory(calculationDate, null);
    }


    /**
     * 全量计算明细数据
     *
     */
    public void calculationDetail(List<String> suggestionIds) {
        List<CfgSettingDTO> settings = cfgSettingService.listAllSetting();
        //获取备货默认配置
        List<CfgRuleStockUpEntity> defaultStockUpList = cfgRuleStockUpService.getDefaultCfgRuleStockUp();
        List<String> defaultStockUpIds = defaultStockUpList.stream().map(CfgRuleStockUpEntity::getId).collect(Collectors.toList());
        List<CfgRuleStockingRatioEntity> defaultStockingRatioList = cfgRuleStockingRatioService.listByStockUpIdList(defaultStockUpIds);
        List<CfgRuleLogisticsEntity> defaultLogisticsList = cfgRuleLogisticsService.listByStockUpIdList(defaultStockUpIds);
        List<String> defaultLogisticsIds = defaultLogisticsList.stream().map(CfgRuleLogisticsEntity::getId).collect(Collectors.toList());
        List<CfgRuleLogisticsDetailEntity> defaultLogisticsDetailList = cfgRuleLogisticsDetailService.listByMainIdList(defaultLogisticsIds);
        //获取销量默认配置
        List<CfgRuleSalesQtyEntity> defaultCfgRuleSalesQty = cfgRuleSalesQtyService.getDefaultCfgRuleSalesQty();
        List<String> defaultSalesQtyIds = defaultCfgRuleSalesQty.stream().map(CfgRuleSalesQtyEntity::getId).collect(Collectors.toList());
        List<CfgRuleSalesFormulaEntity> defaultFormulaList = cfgRuleSalesFormulaService.listBySalesQtyIdList(defaultSalesQtyIds);
        List<CfgRuleSalesDenoisingEntity> defaultDenoisingList = cfgRuleSalesDenoisingService.listBySalesQtyIdList(defaultSalesQtyIds);
        //查询所有需要计算得数据
        List<ReplenishmentResultDTO> suggestions = replenishmentSuggestionService.listAllCalculationData(suggestionIds);
        List<List<ReplenishmentResultDTO>> partition = Lists.partition(suggestions, 1000);
        for (List<ReplenishmentResultDTO> list : partition) {
            CompletableFuture.runAsync(() -> {
                for (ReplenishmentResultDTO dto : list) {
                    try {
                        ReplenishmentResultDTO.DetailDTO detail = dto.getReplenishmentDetail();
                        ReplenishmentResultDTO.BasicDTO entity = dto.getReplenishment();
                        //初始化配置
                        CfgRuleStrategyDTO cfgRuleStrategy = new CfgRuleStrategyDTO();
                        //获取销量配置
                        CfgRuleSalesQtyEntity defaultSalesQty = defaultCfgRuleSalesQty.stream()
                                .filter(v -> v.getPlatformType().equals(entity.getPlatformType()))
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
                        CfgRuleSettingStrategy<CfgRuleWarehouseDTO.StrategyDTO, CfgRuleWarehouseDTO.StrategyResultDTO> warehouseStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_WAREHOUSE.getCode());
                        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = warehouseStrategy.process(new CfgRuleWarehouseDTO.StrategyDTO(entity.getPlatformType(), entity.getPlatform(), entity.getShopId()));
                        cfgRuleStrategy.setWarehouseResult(warehouseResult);
                        //获取店铺对应的本地仓，海外仓
                        List<String> localWarehouse = warehouseResult.getLocalWarehouseList()
                                .stream()
                                .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::getWarehouseId)
                                .collect(Collectors.toList());
                        dto.setLocalWarehouseId(localWarehouse);
                        List<String> overseasWarehouse = warehouseResult.getOverseasWarehouseList()
                                .stream()
                                .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::getWarehouseId)
                                .collect(Collectors.toList());
                        dto.setOverseasWarehouseId(overseasWarehouse);
                        //获取库存配置
                        CfgRuleSettingStrategy<CfgRuleCommonDTO.StrategyDTO, List<CfgRuleCommonDTO.StrategyResultDTO>> inventoryStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_INVENTORY.getCode());
                        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = inventoryStrategy.process(new CfgRuleCommonDTO.StrategyDTO(entity.getPlatformType()));
                        cfgRuleStrategy.setInventoryResult(inventoryResult);
                        //获取建议配置
                        CfgRuleSettingStrategy<CfgRuleCommonDTO.StrategyDTO, List<CfgRuleCommonDTO.StrategyResultDTO>> suggestedStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_SUGGESTED_AMOUNT.getCode());
                        List<CfgRuleCommonDTO.StrategyResultDTO> suggestResult = suggestedStrategy.process(new CfgRuleCommonDTO.StrategyDTO(entity.getPlatformType()));
                        cfgRuleStrategy.setSuggestAmountResult(suggestResult);
                        // todo 海外仓备货存在问题
                        //获取备货配置
                        CfgRuleStockUpEntity defaultStockUp = defaultStockUpList.stream()
                                .filter(v -> v.getPlatformType().equals(entity.getPlatformType()))
                                .findFirst()
                                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_STOCK_UP_NOT_EXIST, CfgRulePlatformTypeEnum.getName(entity.getPlatformType())));
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
                        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = stockUpStrategy.process(CfgRuleStockUpDTO.StrategyDTO.buildStrategyDTO(entity, detail.getSkuType(), defaultStockUp, defaultStockingRatio, defaultLogistics, logisticsDetails));
                        cfgRuleStrategy.setStockUpResult(stockUpResult);
                        stockingTimeHandler.handle(cfgRuleStrategy, dto);
                        replenishmentSuggestionService.saveReplenishment(cfgRuleStrategy, dto);
                    } catch (Exception e) {
                        replenishmentSuggestionService.updateRemark(dto.getReplenishment().getId(), e.getMessage());
                    }
                }
            }, threadPoolTaskExecutor);
        }
    }


    private void cleanHistorySalesAndInventory(LocalDate calculationDate, String id) {
        //查询所有需要计算得数据
        List<ReplenishmentSuggestionEntity> suggestions = replenishmentSuggestionService.listCalculationData(id);
        List<CfgRuleSalesQtyEntity> defaultCfgRuleSalesQty = cfgRuleSalesQtyService.getDefaultCfgRuleSalesQty();
        List<CfgSettingDTO> settings = cfgSettingService.listAllSetting();
        Map<String, List<ReplenishmentResultDTO.SalesInfoAllDTO>> salesByPlatformType = new HashMap<>();
        String calculation = calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        for (CfgRuleSalesQtyEntity cfgRuleSalesQty : defaultCfgRuleSalesQty) {
            List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllList;
            // 以销售订单订单创建时间计算销量
            if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(cfgRuleSalesQty.getSalesQtyType())) {
                salesInfoAllList = salesService.listAllSalesBySob2c(calculation);
            } else {
                // 以销售出库单出库时间计算销量
                salesInfoAllList = salesService.listAllSalesBySoOutStock(calculation);
            }
            salesByPlatformType.put(cfgRuleSalesQty.getPlatformType() + ":" + cfgRuleSalesQty.getType(), salesInfoAllList);
        }
        List<String> skuIds = suggestions.stream().map(ReplenishmentSuggestionEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> vos = plmTaskFeign.listSkuCostByIds(skuIds);
        List<ProductSaleEntity> productSaleList = plmTaskFeign.listProductSaleBySkuId(skuIds);
        CfgSettingDTO newDaysSetting = settings.stream()
                .filter(v -> v.getKey().equals(CfgSettingEnum.NEW_DAYS.getCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_NEWS_NOT_EXIST));
        CfgSettingDTO replenishmentDaysSetting = settings.stream()
                .filter(v -> v.getKey().equals(CfgSettingEnum.REPLENISHMENT_DAYS.getCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_NEWS_NOT_EXIST));
        List<String> suggestionIds = suggestions.parallelStream()
                .map(entity -> {
                    ReplenishmentSuggestionDetailEntity detail = new ReplenishmentSuggestionDetailEntity();
                    //计算是否新品
                    ProductSaleEntity sale = productSaleList.stream().filter(v -> v.getSkuId().equals(entity.getSkuId())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(sale) || ObjectUtils.isEmpty(sale.getListingTime())) {
                        return null;
                    }
                    SkuVO skuVO = vos.stream().filter(v -> v.getSkuId().equals(entity.getSkuId())).findFirst().orElse(new SkuVO());
                    detail.setSalesPrice(skuVO.getRetailPrice());
                    String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JSRQ, calculationDate);
                    detail.setId(IdWorker.getIdStr());
                    detail.setCalcVersion(code);
                    detail.setCalcDate(calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE));
                    detail.setMainId(entity.getId());
                    if (sale.getListingTime().plusDays(Long.parseLong(newDaysSetting.getDataJson())).isAfter(LocalDate.now())) {
                        detail.setSkuType(CfgRuleStockingRatioTypeEnum.NEW.getCode());
                    } else {
                        detail.setSkuType(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
                    }
                    List<SalesInfoEntity> salesInfoList = new ArrayList<>();
                    //清洗历史销量及库存
                    if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(entity.getPlatformType())) {
                        salesInfoList = getFbaHistorySales(salesByPlatformType.get(CfgRulePlatformTypeEnum.AMAZON.getCode() + ":" + detail.getSkuType()), entity, detail);
                    } else if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(entity.getPlatformType())) {
                        //todo 后期做
                    } else if (CfgRulePlatformTypeEnum.B2B.getCode().equals(entity.getPlatformType()) ||
                            CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(entity.getPlatformType())) {
                        //todo 后期做
                    }
                    
                    //判断是否需要补货
                    CfgSettingDTO.ReplenishmentDays replenishmentDays = JSON.parseObject(replenishmentDaysSetting.getDataJson(), CfgSettingDTO.ReplenishmentDays.class);
                    boolean isOver180Days = sale.getListingTime().plusDays(replenishmentDays.getStart()).isAfter(calculationDate);
                    int saleQty = salesInfoList
                            .stream()
                            .filter(v -> !calculationDate.minusDays(replenishmentDays.getEnd()).isAfter(v.getDate()) && !calculationDate.isBefore(v.getDate()))
                            .map(SalesInfoEntity::getOriginalSalesQty)
                            .reduce(0, Math::addExact);
                    replenishmentSuggestionDetailService.save(detail);
                    if (Boolean.TRUE.equals(isOver180Days) && saleQty == 0) {
                        replenishmentSuggestionService.notRestockingReplenishment(entity.getId(), "上市超180天，360天内无销量的商品，系统自动标记暂不补货");
                        return null;
                    } else {
                        salesInfoService.saveBatch(salesInfoList);
                        return entity.getId();
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        //计算数据
        calculationDetail(suggestionIds);
    }

    /**
     * 计算历史销量库存
     *
     * @param salesInfoAllDTOS 销量数据
     * @param entity           主表
     * @param detail           明细
     */
    private List<SalesInfoEntity> getFbaHistorySales(List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllDTOS, ReplenishmentSuggestionEntity entity, ReplenishmentSuggestionDetailEntity detail) {
        Map<LocalDate, Integer> salesMap = salesInfoAllDTOS.stream()
                .filter(v -> v.getSkuId().equals(entity.getSkuId()))
                .filter(v -> v.getShopId().equals(entity.getShopId()))
                .collect(Collectors.toMap(ReplenishmentResultDTO.SalesInfoAllDTO::getDate, ReplenishmentResultDTO.SalesInfoAllDTO::getOriginalSalesQty, Integer::sum));

        //获取历史库存
        List<FbaHistoryInventoryEntity> list = fbaHistoryInventoryService.listBySkuNo(entity.getSkuNo(), entity.getFbaWarehouseId());
        String calcDate = detail.getCalcDate();
        LocalDate endDate = LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate startDate = LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE).minusDays(360);
        List<LocalDate> dateList = new ArrayList<>();
        // 遍历每一天
        while (startDate.isBefore(endDate)) {
            dateList.add(startDate);
            startDate = startDate.plusDays(1);
        }
        Map<LocalDate, Integer> localDateMap = list.stream()
                .collect(Collectors.toMap(FbaHistoryInventoryEntity::getBillDate, FbaHistoryInventoryEntity::getFulfillableQty, Integer::sum));
        //拆分为时间list
        List<SalesInfoEntity> salesInfo = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            SalesInfoEntity info = new SalesInfoEntity();
            info.setReplenishmentDetailId(detail.getId());
            info.setDate(localDate);
            info.setOriginalInventoryQty(Optional.ofNullable(localDateMap.get(localDate)).orElse(0));
            info.setOriginalSalesQty(Optional.ofNullable(salesMap.get(localDate)).orElse(0));
            info.setCalcVersion(detail.getCalcVersion());
            salesInfo.add(info);
        }
        return salesInfo;
    }
}
