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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.SnapshotTableEnum.FBA_INVENTORY;

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
    private InventoryService inventoryService;
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
    private InventoryMapper inventoryMapper;

    /**
     * 增量变动建议补货基础数据
     */
    public void initReplenishmentSku(LocalDate calculationDate) {
        calculationDate = ObjectUtils.isEmpty(calculationDate) ? LocalDate.now() : calculationDate;
        //清洗每日库存到历史表
        List<FbaInventoryEntity> inventoryEntities = inventoryMapper.getAllFbaHistoryInventory(SnapshotTableEnum.getTableName(FBA_INVENTORY, calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE)));
        if (!CollectionUtils.isEmpty(inventoryEntities)) {
            fbaHistoryInventoryService.saveTodayInventory(inventoryEntities, calculationDate);
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
        calculationDetail(calculationDate);
    }


    /**
     * 全量计算明细数据
     *
     * @param calculationDate 计算日期
     */
    public void calculationDetail(LocalDate calculationDate) {
        List<CfgSettingDTO> settings = cfgSettingService.listAllSetting();
        CfgSettingDTO newDaysSetting = settings.stream()
                .filter(v -> v.getKey().equals(CfgSettingEnum.NEW_DAYS.getCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_NEWS_NOT_EXIST));
        CfgSettingDTO replenishmentDaysSetting = settings.stream()
                .filter(v -> v.getKey().equals(CfgSettingEnum.REPLENISHMENT_DAYS.getCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_NEWS_NOT_EXIST));
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
        List<ReplenishmentSuggestionEntity> suggestions = replenishmentSuggestionService.listCalculationData();
        List<String> skuIds = suggestions.stream().map(ReplenishmentSuggestionEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> vos = plmTaskFeign.listSkuCostByIds(skuIds);
        List<ProductSaleEntity> productSaleList = plmTaskFeign.listProductSaleBySkuId(skuIds);
        List<List<ReplenishmentSuggestionEntity>> partition = Lists.partition(suggestions, 1000);
        for (List<ReplenishmentSuggestionEntity> list : partition) {
            CompletableFuture.runAsync(() -> {
                for (ReplenishmentSuggestionEntity entity : list) {
                    try {
                        ReplenishmentResultDTO replenishmentResult = new ReplenishmentResultDTO();
                        replenishmentResult.setReplenishment(ReplenishmentResultDTO.BasicDTO.buildBasicDTO(entity));
                        ReplenishmentResultDTO.DetailDTO detail = new ReplenishmentResultDTO.DetailDTO();
                        //计算是否新品
                        ProductSaleEntity sale = productSaleList.stream().filter(v -> v.getSkuId().equals(entity.getSkuId())).findFirst().orElse(null);
                        if (ObjectUtils.isEmpty(sale) || ObjectUtils.isEmpty(sale.getListingTime())) {
                            continue;
                        }
                        SkuVO skuVO = vos.stream().filter(v -> v.getSkuId().equals(entity.getSkuId())).findFirst().orElse(new SkuVO());
                        replenishmentResult.setSalesPrice(skuVO.getRetailPrice());
                        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JSRQ, calculationDate);
                        detail.setDetailId(IdWorker.getIdStr());
                        detail.setCalcVersion(code);
                        detail.setCalcDate(calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE));
                        detail.setMainId(entity.getId());
                        if (sale.getListingTime().plusDays(Long.parseLong(newDaysSetting.getDataJson())).isAfter(LocalDate.now())) {
                            detail.setSkuType(CfgRuleStockingRatioTypeEnum.NEW.getCode());
                        } else {
                            detail.setSkuType(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
                        }
                        replenishmentResult.setReplenishmentDetail(detail);
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
                        //清洗历史销量及库存
                        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(entity.getPlatformType())) {
                            getFbaHistorySales(salesResult, replenishmentResult);
                        } else if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(entity.getPlatformType())) {
                            //todo 后期做
                        } else if (CfgRulePlatformTypeEnum.B2B.getCode().equals(entity.getPlatformType()) ||
                                CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(entity.getPlatformType())) {
                            //todo 后期做
                        }
                        //判断是否需要补货
                        CfgSettingDTO.ReplenishmentDays replenishmentDays = JSON.parseObject(replenishmentDaysSetting.getDataJson(), CfgSettingDTO.ReplenishmentDays.class);
                        int saleQty = replenishmentResult.getSalesInfos()
                                .stream()
                                .filter(v -> !sale.getListingTime().plusDays(replenishmentDays.getStart()).isAfter(v.getDate()) &&
                                        !sale.getListingTime().plusDays(replenishmentDays.getEnd()).isBefore(v.getDate()))
                                .map(ReplenishmentResultDTO.SalesInfoDTO::getOriginalSalesQty)
                                .reduce(0, Math::addExact);
                        if (saleQty == 0) {
                            replenishmentSuggestionDetailService.saveDetail(replenishmentResult.getReplenishmentDetail());
                            replenishmentSuggestionService.notRestockingReplenishment(entity.getId(), "上市超180天，360天内无销量的商品，系统自动标记暂不补货");
                        } else {
                            //获取仓库配置
                            CfgRuleSettingStrategy<CfgRuleWarehouseDTO.StrategyDTO, CfgRuleWarehouseDTO.StrategyResultDTO> warehouseStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_WAREHOUSE.getCode());
                            CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = warehouseStrategy.process(new CfgRuleWarehouseDTO.StrategyDTO(entity.getPlatformType(), entity.getPlatform(), entity.getShopId()));
                            cfgRuleStrategy.setWarehouseResult(warehouseResult);
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
                            stockingTimeHandler.handle(cfgRuleStrategy, replenishmentResult);
                            replenishmentSuggestionService.saveReplenishment(cfgRuleStrategy, replenishmentResult);
                        }
                    } catch (Exception e) {
                        entity.setRemark(e.getMessage());
                        replenishmentSuggestionService.updateById(entity);
                    }
                }
            }, threadPoolTaskExecutor);
        }
    }

    private void getFbaHistorySales(CfgRuleSalesQtyDTO.StrategyResultDTO salesQtyResult, ReplenishmentResultDTO replenishmentResult) {
        if (FbaOrderTypeEnum.FBA.getCode().equals(salesQtyResult.getOrderType())) {
            List<ReplenishmentResultDTO.SalesInfoDTO> sales;
            // 以销售订单订单创建时间计算销量
            if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(salesQtyResult.getSalesQtyType())) {
                sales = salesService.listSalesBySob2c(replenishmentResult);
            } else {
                // 以销售出库单出库时间计算销量
                sales = salesService.listSalesBySoOutStock(replenishmentResult);
            }
            replenishmentResult.setSalesInfos(sales);
            //获取历史库存
            List<FbaHistoryInventoryEntity> list = fbaHistoryInventoryService.listBySkuNo(replenishmentResult.getReplenishment().getSkuNo(), replenishmentResult.getReplenishment().getFbaWarehouseId());
            inventoryService.getHistoryInventory(replenishmentResult,list);
        } else {
            // todo 后期做
        }
    }


    private void getHistorySales(List<CfgRuleSalesQtyEntity> defaultCfgRuleSalesQty, List<ReplenishmentSuggestionEntity> suggestions){
        //执行Amz
        CfgRuleSalesQtyEntity cfgRuleSalesQty = defaultCfgRuleSalesQty.stream()
                .filter(v -> v.getPlatformType().equals(CfgRulePlatformTypeEnum.AMAZON.getCode()))
                .findFirst().orElse(null);
        if (ObjectUtils.isEmpty(cfgRuleSalesQty)) {

        }
        //执行海外

        //执行B2B

        //执行本地
    }
}
