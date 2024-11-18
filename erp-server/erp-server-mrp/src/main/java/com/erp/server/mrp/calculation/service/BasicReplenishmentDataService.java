package com.erp.server.mrp.calculation.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.utils.ApplicationContextUtils;
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
import com.erp.server.mrp.calculation.handler.StockingTimeHandler;
import com.erp.server.mrp.calculation.strategy.CfgRuleSettingStrategy;
import com.erp.server.mrp.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private InventoryService inventoryService;
    @Resource
    private ReplenishmentTaskService replenishmentTaskService;

    @Resource
    @Lazy
    private DataArchivingService dataArchivingService;

    /**
     * 增量变动建议补货基础数据
     */
    public void initReplenishmentSku(LocalDate calculationDate, List<ShopInfoEntity> shopInfoList) {
        String calcDate = calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        inventoryService.checkAllTableExists(calcDate);
        inventoryService.saveAllHistoryInventory(calculationDate, calcDate);
        //获取所有已审核且存在上市时间得非费用服务类sku
        List<SkuVO> vos = plmTaskFeign.listApproveAndListingSku();
        //获取已生成补货基础数据得信息
        List<ReplenishmentSuggestionEntity> replenishmentSuggestion = replenishmentSuggestionService.listAllSkuAndShop();
        Map<String, ReplenishmentSuggestionEntity> oldReplenishmentMap = replenishmentSuggestion.stream()
                .collect(Collectors.toMap(v -> v.getSkuId() + ":" + v.getShopId(), v -> v, (o1, o2) -> o1));
        List<CfgPlatformMappingEntity> mappings = cfgPlatformMappingService.listByEffective();
        Map<String, String> mappingMap = mappings.stream().collect(Collectors.toMap(CfgPlatformMappingEntity::getPlatform, CfgPlatformMappingEntity::getType, (o1, o2) -> o1));
        // 等待所有任务执行完毕
        CompletableFuture.allOf(shopInfoList.stream()
                .map(shopInfo -> CompletableFuture.runAsync(() -> {
                    List<ReplenishmentSuggestionEntity> suggestionLists = new ArrayList<>();
                    String platFormMapping = mappingMap.get(shopInfo.getDictPlatform());
                    if (ObjectUtils.isEmpty(platFormMapping)) {
                        return;
                    }
                    for (SkuVO vo : vos) {
                        ReplenishmentSuggestionEntity entity = Optional.ofNullable(oldReplenishmentMap.get(vo.getSkuId() + ":" + shopInfo.getId())).orElse(new ReplenishmentSuggestionEntity());
                        // 过滤掉已生成建议且 sku_no 未发生变化的 SKU，或未启用的平台
                        if (!ObjectUtils.isEmpty(entity.getId()) && entity.getSkuNo().equals(vo.getSkuNo())) {
                            continue;
                        }
                        entity.setShopId(shopInfo.getId());
                        entity.setArea(shopInfo.getDictAreaCode());
                        entity.setFbaWarehouseId(shopInfo.getWarehouseId());
                        entity.setCountry(shopInfo.getDictCountryCode());
                        entity.setPlatformType(PlatformMappingTypeEnum.getEnum(platFormMapping).getPlatformType().getCode());
                        entity.setPlatform(shopInfo.getDictPlatform());
                        entity.setSkuId(vo.getSkuId());
                        entity.setSkuNo(vo.getSkuNo());
                        entity.setReplenishmentType(ReplenishmentTypeEnum.NORMAL.getCode());
                        suggestionLists.add(entity);
                    }
                    replenishmentSuggestionService.saveOrUpdateBatch(suggestionLists);
                }, threadPoolTaskExecutor)).toArray(CompletableFuture[]::new)).join();
    }


    /**
     * 全量计算明细数据
     */
    public void calculationDetail(List<String> suggestionIds, List<ShopInfoEntity> shopInfoList) {
        if (CollectionUtils.isEmpty(shopInfoList)) {
            ApiResult<List<ShopInfoEntity>> allShopResult = shopInfoFeign.list();
            if (!allShopResult.isSuccess()) {
                throw new ServiceException(allShopResult.getMsg());
            }
            shopInfoList = allShopResult.getData();
        }

        Map<String, List<String>> shopIdByPlatform = shopInfoList.stream()
                .collect(Collectors.groupingBy(ShopInfoEntity::getDictPlatform, Collectors.mapping(ShopInfoEntity::getId, Collectors.toList())));
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
        List<String> replenishmentIds = replenishmentTaskService.listByWaitAndReplenishment(suggestionIds);
        //查询所有需要计算得数据
        List<ReplenishmentResultDTO> suggestions = replenishmentSuggestionService.listAllCalculationData(replenishmentIds);
        for (ReplenishmentResultDTO dto : suggestions) {
            CompletableFuture.runAsync(() -> {
                try {
                    replenishmentTaskService.updateStatus(dto.getReplenishment().getId(), SyncStatusEnum.IN_SYNC.getCode());
                    ReplenishmentResultDTO.DetailDTO detail = dto.getReplenishmentDetail();
                    ReplenishmentResultDTO.BasicDTO entity = dto.getReplenishment();
                    dto.setShopIdByPlatform(shopIdByPlatform);
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
                    getCfgRuleCommon(entity, cfgRuleStrategy, dto);
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
                    replenishmentTaskService.updateStatus(dto.getReplenishment().getId(), SyncStatusEnum.SUCCESS_SYNC.getCode());
                } catch (Exception e) {
                    log.error("计算失败 sku{},店铺{}, 原因{}", dto.getReplenishment().getSkuNo(), dto.getReplenishment().getShopId(), e.getMessage(), e);
                    replenishmentTaskService.updateStatus(dto.getReplenishment().getId(), SyncStatusEnum.FAILED_SYNC.getCode(), e.getMessage());
                }
            }, threadPoolTaskExecutor);
        }
    }


    /**
     * 清洗历史库存及销量
     *
     * @param calculationDate 计算日
     */
    @SuppressWarnings("all")
    public void cleanHistorySalesAndInventory(LocalDate calculationDate, List<ShopInfoEntity> shopInfoList) {
        String calcDate = calculationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        //查询所有需要计算得数据
        List<ReplenishmentSuggestionEntity> suggestions = replenishmentSuggestionService.listCalculationData();
        List<String> skuIds = suggestions.stream().map(ReplenishmentSuggestionEntity::getSkuId).distinct().collect(Collectors.toList());
        List<List<String>> skuPartList = Lists.partition(skuIds, 500);
        List<SkuVO> skuVOList = new ArrayList<>();
        List<ProductSaleEntity> productSales = new ArrayList<>();
        for (List<String> skuIdList : skuPartList) {
            List<SkuVO> vos = plmTaskFeign.listSkuCostByIds(skuIdList);
            skuVOList.addAll(vos);
            List<ProductSaleEntity> productSaleList = plmTaskFeign.listProductSaleBySkuId(skuIdList);
            productSales.addAll(productSaleList);
        }
        Map<String, SkuVO> skuVOMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, v -> v, (o1, o2) -> o1));
        Map<String, ProductSaleEntity> productSaleEntityMap = productSales.stream().collect(Collectors.toMap(ProductSaleEntity::getSkuId, v -> v, (o1, o2) -> o1));
        List<CfgRuleSalesQtyEntity> defaultCfgRuleSalesQty = cfgRuleSalesQtyService.getDefaultCfgRuleSalesQty();
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
        //获取历史库存
        List<FbaHistoryInventoryEntity> fbaHistoryInventoryList = fbaHistoryInventoryService.list();
        Map<String, List<ReplenishmentResultDTO.SalesInfoAllDTO>> salesByPlatformType = new HashMap<>();
        for (CfgRuleSalesQtyEntity cfgRuleSalesQty : defaultCfgRuleSalesQty) {
            List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllList;
            // 以销售订单订单创建时间计算销量
            if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(cfgRuleSalesQty.getSalesQtyType())) {
                salesInfoAllList = salesService.listAllAmzSalesBySob2c(calcDate, cfgRuleSalesQty.getOrderType());
            } else {
                // 以销售出库单出库时间计算销量
                salesInfoAllList = salesService.listAllAmzSalesBySoOutStock(calcDate, cfgRuleSalesQty.getOrderType());
            }
            //todo 获取海外仓本地B2B销量
            salesByPlatformType.put(cfgRuleSalesQty.getPlatformType() + ":" + cfgRuleSalesQty.getType(), salesInfoAllList);
        }
        Map<String, List<ReplenishmentSuggestionEntity>> replenishmentBySku = suggestions.stream()
                .collect(Collectors.groupingBy(ReplenishmentSuggestionEntity::getSkuId));
        //提前处理需要历史库存和销量对应的日期
        LocalDate startDate = calculationDate.minusDays(361);
        List<LocalDate> dateList = new ArrayList<>();
        // 遍历每一天
        while (startDate.isBefore(calculationDate)) {
            dateList.add(startDate);
            startDate = startDate.plusDays(1);
        }
        for (List<ReplenishmentSuggestionEntity> suggestion : replenishmentBySku.values()) {
            CompletableFuture.runAsync(() -> {
                log.warn("开始清洗相同sku不同店铺的历史数据：{}", System.currentTimeMillis());
                List<String> notRestockingId = new ArrayList<>();
                List<ReplenishmentSuggestionDetailEntity> details = new ArrayList<>();
                List<String> suggestionIds = new ArrayList<>();
                List<SalesInfoEntity> salesInfoList = new ArrayList<>();
                for (ReplenishmentSuggestionEntity entity : suggestion) {
                    ReplenishmentSuggestionDetailEntity detail = new ReplenishmentSuggestionDetailEntity();
                    //计算是否新品
                    ProductSaleEntity sale = productSaleEntityMap.get(entity.getSkuId());
                    if (ObjectUtils.isEmpty(sale) || ObjectUtils.isEmpty(sale.getListingTime())) {
                        continue;
                    }
                    SkuVO skuVO = Optional.ofNullable(skuVOMap.get(entity.getSkuId())).orElse(new SkuVO());
                    detail.setSalesPrice(skuVO.getRetailPrice());
                    String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JSRQ, calculationDate);
                    detail.setId(IdWorker.getIdStr());
                    detail.setCalcVersion(code);
                    detail.setCalcDate(calcDate);
                    detail.setMainId(entity.getId());
                    if (sale.getListingTime().plusDays(Long.parseLong(newDaysSetting.getDataJson())).isAfter(LocalDate.now())) {
                        detail.setSkuType(CfgRuleStockingRatioTypeEnum.NEW.getCode());
                    } else {
                        detail.setSkuType(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
                    }
                    //判断是否需要补货
                    CfgSettingDTO.ReplenishmentDays replenishmentDays = JSON.parseObject(replenishmentDaysSetting.getDataJson(), CfgSettingDTO.ReplenishmentDays.class);
                    boolean isOver180Days = sale.getListingTime().plusDays(replenishmentDays.getStart()).isBefore(calculationDate);
                    int saleQty = salesByPlatformType.values()
                            .stream()
                            .flatMap(Collection::stream)
                            .filter(v -> v.getShopId().equals(entity.getShopId()))
                            .filter(v -> v.getSkuId().equals(entity.getSkuId()))
                            .map(ReplenishmentResultDTO.SalesInfoAllDTO::getOriginalSalesQty)
                            .reduce(0, Math::addExact);
                    details.add(detail);
                    if (Boolean.TRUE.equals(isOver180Days) && saleQty == 0) {
                        notRestockingId.add(entity.getId());
                    } else {
                        suggestionIds.add(entity.getId());
                        //清洗历史销量及库存
                        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(entity.getPlatformType())) {
                            salesInfoList.addAll(getFbaHistorySales(salesByPlatformType.get(CfgRulePlatformTypeEnum.AMAZON.getCode() + ":" + detail.getSkuType()), entity, detail, fbaHistoryInventoryList, dateList));
                        } else if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(entity.getPlatformType())) {
                            //todo 后期做
                        } else if (CfgRulePlatformTypeEnum.B2B.getCode().equals(entity.getPlatformType()) ||
                                CfgRulePlatformTypeEnum.INTERNAL.getCode().equals(entity.getPlatformType())) {
                            //todo 后期做
                        }
                    }
                }
                ApplicationContextUtils.getBean(BasicReplenishmentDataService.class).saveSuggestionDetail(salesInfoList, details, notRestockingId, suggestionIds, shopInfoList);
            }, threadPoolTaskExecutor);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveSuggestionDetail(List<SalesInfoEntity> salesInfoList, List<ReplenishmentSuggestionDetailEntity> details, List<String> notRestockingId, List<String> suggestionIds, List<ShopInfoEntity> shopInfoList) {
        salesInfoService.saveBatch(salesInfoList);
        replenishmentSuggestionDetailService.saveOrUpdateBatch(details);
        replenishmentSuggestionService.batchNotRestockingReplenishment(notRestockingId, "上市超180天，360天内无销量的商品，系统自动标记暂不补货");
        replenishmentTaskService.saveTask(suggestionIds);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                if (CollectionUtils.isEmpty(suggestionIds)) {
                    return;
                }
                calculationDetail(suggestionIds, shopInfoList);
            }
        });
    }


    /**
     * 计算历史销量库存
     *
     * @param salesInfoAllDTOS        销量数据
     * @param entity                  主表
     * @param detail                  明细
     * @param fbaHistoryInventoryList 历史库存
     * @param dateList                日期
     */
    private List<SalesInfoEntity> getFbaHistorySales(List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllDTOS,
                                                     ReplenishmentSuggestionEntity entity,
                                                     ReplenishmentSuggestionDetailEntity detail,
                                                     List<FbaHistoryInventoryEntity> fbaHistoryInventoryList,
                                                     List<LocalDate> dateList) {
        Map<LocalDate, Integer> salesMap = salesInfoAllDTOS.stream()
                .filter(v -> v.getSkuId().equals(entity.getSkuId()))
                .filter(v -> v.getShopId().equals(entity.getShopId()))
                .collect(Collectors.toMap(ReplenishmentResultDTO.SalesInfoAllDTO::getDate, ReplenishmentResultDTO.SalesInfoAllDTO::getOriginalSalesQty, Integer::sum));
        List<FbaHistoryInventoryEntity> list = fbaHistoryInventoryList.stream()
                .filter(v -> v.getSkuNo().equals(entity.getSkuNo()))
                .filter(v -> v.getWarehouseId().equals(entity.getFbaWarehouseId()))
                .collect(Collectors.toList());
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

    /**
     * 更新数据
     *
     * @param id 主表id
     */
    public BatchResultDTO renewData(String id) {
        ReplenishmentSuggestionEntity suggestion = replenishmentSuggestionService.getById(id);
        ReplenishmentSuggestionDetailEntity detail = replenishmentSuggestionDetailService.getByMainId(id);
        List<SalesInfoEntity> salesInfoList = salesInfoService.listByReplenishmentDetailIds(Collections.singletonList(detail.getId()));
        ReplenishmentResultDTO resultDTO = new ReplenishmentResultDTO();
        ReplenishmentResultDTO.BasicDTO basicDTO = ReplenishmentResultDTO.BasicDTO.buildBasicDTO(suggestion);
        resultDTO.setReplenishment(basicDTO);
        ReplenishmentResultDTO.DetailDTO detailDTO = ReplenishmentResultDTO.DetailDTO.buildDetailNotId(detail);
        detailDTO.setCalcDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JSRQ, LocalDate.now());
        detailDTO.setCalcVersion(code);
        resultDTO.setReplenishmentDetail(detailDTO);
        resultDTO.setSalesPrice(detail.getSalesPrice());
        resultDTO.setPurchasePrice(detail.getPurchasePrice());
        salesInfoList.forEach(salesInfo -> salesInfo.setId(IdWorker.getIdStr()));
        List<ReplenishmentResultDTO.SalesInfoDTO> salesInfoEntityList = salesInfoList.stream()
                .map(ReplenishmentResultDTO.SalesInfoDTO::buildSalesInfoDTO)
                .collect(Collectors.toList());
        resultDTO.setSalesInfos(salesInfoEntityList);
        salesInfoList.forEach(v -> {
            v.setReplenishmentDetailId(detailDTO.getDetailId());
            v.setCalcVersion(detailDTO.getCalcVersion());
            v.setSalesQty(null);
            v.setIsIgnoreOutOfStock(null);
            v.setSalesQtyType(null);
        });
        //保存历史销量数据到表
        salesInfoService.saveBatch(salesInfoList);
        ReplenishmentSuggestionDetailEntity entity = ReplenishmentResultDTO.DetailDTO.buildNewDetail(detailDTO);
        replenishmentSuggestionDetailService.save(entity);
        //归档该条数据明细
        dataArchivingService.dataArchiving(detail.getId());
        //获取所有店铺
        ApiResult<List<ShopInfoEntity>> allShopResult = shopInfoFeign.list();
        if (!allShopResult.isSuccess()) {
            throw new ServiceException(allShopResult.getMsg());
        }
        Map<String, List<String>> shopIdByPlatform = allShopResult.getData().stream()
                .collect(Collectors.groupingBy(ShopInfoEntity::getDictPlatform, Collectors.mapping(ShopInfoEntity::getId, Collectors.toList())));
        resultDTO.setShopIdByPlatform(shopIdByPlatform);
        //初始化配置
        CfgRuleStrategyDTO cfgRuleStrategy = new CfgRuleStrategyDTO();
        //获取销量配置
        CfgRuleSalesQtyEntity defaultSalesQty = cfgRuleSalesQtyService.getDefaultByPlatformAndSkuType(suggestion.getPlatformType(), detail.getSkuType());
        List<CfgRuleSalesFormulaEntity> defaultFormula = cfgRuleSalesFormulaService.listBySalesQtyIdList(Collections.singletonList(defaultSalesQty.getId()));
        List<CfgRuleSalesDenoisingEntity> defaultDenoising = cfgRuleSalesDenoisingService.listBySalesQtyIdList(Collections.singletonList(defaultSalesQty.getId()));
        CfgRuleSettingStrategy<CfgRuleSalesQtyDTO.StrategyDTO, CfgRuleSalesQtyDTO.StrategyResultDTO> salesStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_SALES_QTY.getCode());
        CfgRuleSalesQtyDTO.StrategyResultDTO salesResult = salesStrategy.process(CfgRuleSalesQtyDTO.StrategyDTO.buildStrategyDTO(basicDTO, detail.getSkuType(), defaultSalesQty, defaultFormula, defaultDenoising));
        cfgRuleStrategy.setSalesQtyResult(salesResult);
        List<CfgSettingDTO> settings = cfgSettingService.listAllSetting();
        cfgRuleStrategy.setSettings(settings);
        getCfgRuleCommon(basicDTO, cfgRuleStrategy, resultDTO);
        //获取备货配置
        CfgRuleStockUpEntity defaultStockUp = cfgRuleStockUpService.getDefaultByPlatform(basicDTO.getPlatformType());
        List<CfgRuleStockingRatioEntity> defaultStockingRatio = cfgRuleStockingRatioService.listByStockUpIdList(Collections.singletonList(defaultStockUp.getId()));
        List<CfgRuleLogisticsEntity> defaultLogistics = cfgRuleLogisticsService.listByStockUpIdList(Collections.singletonList(defaultStockUp.getId()));
        List<String> defaultLogisticsIds = defaultLogistics.stream().map(CfgRuleLogisticsEntity::getId).collect(Collectors.toList());
        List<CfgRuleLogisticsDetailEntity> defaultLogisticsDetailList = cfgRuleLogisticsDetailService.listByMainIdList(defaultLogisticsIds);
        CfgRuleSettingStrategy<CfgRuleStockUpDTO.StrategyDTO, CfgRuleStockUpDTO.StrategyResultDTO> stockUpStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_STOCK_UP.getCode());
        CfgRuleStockUpDTO.StrategyResultDTO stockUpResult = stockUpStrategy.process(CfgRuleStockUpDTO.StrategyDTO.buildStrategyDTO(basicDTO, detail.getSkuType(), defaultStockUp, defaultStockingRatio, defaultLogistics, defaultLogisticsDetailList));
        cfgRuleStrategy.setStockUpResult(stockUpResult);
        stockingTimeHandler.handle(cfgRuleStrategy, resultDTO);
        replenishmentSuggestionService.saveReplenishment(cfgRuleStrategy, resultDTO);
        return BatchResultDTO.success(basicDTO.getId(), detailDTO.getCalcVersion());
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
        //获取库存配置
        CfgRuleSettingStrategy<CfgRuleCommonDTO.StrategyDTO, List<CfgRuleCommonDTO.StrategyResultDTO>> inventoryStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_INVENTORY.getCode());
        List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult = inventoryStrategy.process(new CfgRuleCommonDTO.StrategyDTO(basicDTO.getPlatformType()));
        cfgRuleStrategy.setInventoryResult(inventoryResult);
        //获取建议配置
        CfgRuleSettingStrategy<CfgRuleCommonDTO.StrategyDTO, List<CfgRuleCommonDTO.StrategyResultDTO>> suggestedStrategy = cfgSettingFactory.getCfgRuleSettingHandler(CfgRuleSettingEnum.GET_SUGGESTED_AMOUNT.getCode());
        List<CfgRuleCommonDTO.StrategyResultDTO> suggestResult = suggestedStrategy.process(new CfgRuleCommonDTO.StrategyDTO(basicDTO.getPlatformType()));
        cfgRuleStrategy.setSuggestAmountResult(suggestResult);
    }
}
