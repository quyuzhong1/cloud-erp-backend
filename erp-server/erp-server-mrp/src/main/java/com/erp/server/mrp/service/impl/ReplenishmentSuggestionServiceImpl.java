package com.erp.server.mrp.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.mrp.vo.*;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.mrp.calculation.handler.SalesEstimateHandler;
import com.erp.server.mrp.calculation.service.BasicReplenishmentDataService;
import com.erp.server.mrp.es.entity.HistoryInventoryEsEntity;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;
import com.erp.server.mrp.es.service.HistoryInventoryEsService;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.erp.server.mrp.es.service.OutStockHistorySalesEsService;
import com.erp.server.mrp.mapper.ReplenishmentSuggestionMapper;
import com.erp.server.mrp.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.CfgRuleSalesDenoisingDenoisingTypeEnum.COMPLETELY;

/**
 * <p>
 * 补货建议主表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class ReplenishmentSuggestionServiceImpl extends SuperServiceImpl<ReplenishmentSuggestionMapper, ReplenishmentSuggestionEntity> implements ReplenishmentSuggestionService {

    @Resource
    private SalesInfoService salesInfoService;
    @Resource
    private SalesEstimateService salesEstimateService;
    @Resource
    private RptOutOfStockService rptOutOfStockService;
    @Resource
    private RealOutOfStockService realOutOfStockService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private LabelInfoService labelInfoService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private RecentSuggestionDetailService recentSuggestionDetailService;

    @Resource
    private CfgRuleStockUpService cfgRuleStockUpService;

    @Resource
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;
    @Resource
    private DeliverySuggestService deliverySuggestService;
    @Resource
    private ReplenishmentInventoryDetailService replenishmentInventoryDetailService;
    @Resource
    private SalesEstimateManualService salesEstimateManualService;

    @Resource
    private ReplenishmentSuggestionFavoriteService replenishmentSuggestionFavoriteService;
    @Resource
    private FbaInTransitDetailService fbaInTransitDetailService;
    @Resource
    private OverseasInTransitDetailService overseasInTransitDetailService;
    @Resource
    private LocalInTransitDetailService localInTransitDetailService;
    @Resource
    private EstimatedDeliveryDetailService estimatedDeliveryDetailService;
    @Resource
    private EstimatedPurchaseDetailService estimatedPurchaseDetailService;

    @Resource
    private ReplenishmentRefLabelService replenishmentRefLabelService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private CfgRuleStockingRatioService cfgRuleStockingRatioService;

    @Resource
    private CfgRuleSalesDenoisingService cfgRuleSalesDenoisingService;

    @Resource
    private CfgRuleSalesFormulaService cfgRuleSalesFormulaService;

    @Resource
    private CfgRuleLogisticsService cfgRuleLogisticsService;

    @Resource
    private ReplenishmentSuggestionDetailService replenishmentSuggestionDetailService;
    @Resource
    private PurchaseSuggestService purchaseSuggestService;
    @Resource
    private CfgRuleWarehouseService cfgRuleWarehouseService;

    @Resource
    private SalesEstimateHandler salesEstimateHandler;

    @Resource
    @Lazy
    private BasicReplenishmentDataService replenishmentDataService;
    @Resource
    private HistoryInventoryEsService historyInventoryEsService;
    @Resource
    private OrderHistorySalesEsService orderHistorySalesEsService;
    @Resource
    private OutStockHistorySalesEsService outStockHistorySalesEsService;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Override
    public PagingVO<ReplenishmentSuggestionVO.PagingView> paging(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        LoginUser user = UserContext.getDefaultLoginUser();
        Page<ReplenishmentSuggestionVO.PagingView> pagingVO = baseMapper.paging(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams(),user.getUid());
        if (!CollectionUtils.isEmpty(pagingVO.getRecords())) {
            processData(pagingVO.getRecords());
        }
        return new PagingVO<>(pagingVO);
    }

    private void processData(List<ReplenishmentSuggestionVO.PagingView> records) {
        LoginUser user = UserContext.getDefaultLoginUser();
        List<String> skuIds = records.stream().map(ReplenishmentSuggestionVO.PagingView::getSkuId).distinct().collect(Collectors.toList());
        List<String> ids = records.stream().map(ReplenishmentSuggestionVO.PagingView::getId).distinct().collect(Collectors.toList());
        List<String> detailIds = records.stream().map(ReplenishmentSuggestionVO.PagingView::getDetailId).distinct().collect(Collectors.toList());
        List<String> shopIds = records.stream().map(ReplenishmentSuggestionVO.PagingView::getShopId).distinct().collect(Collectors.toList());
        List<LabelVO> labelVOS = labelInfoService.listLabelByReplenishmentIds(ids);
        List<String> country = records.stream().map(ReplenishmentSuggestionVO.PagingView::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(country);
        List<SkuVO> skuVOS = plmTaskFeign.listSkuCategoryByIds(skuIds);
        List<ShopInfoEntity> shopInfos = shopInfoFeign.listShopInfoByIds(shopIds);
        List<SalesEstimateManualEntity> salesEstimateManuals = salesEstimateManualService.listByReplenishmentIds(ids);
        List<SalesInfoEntity> salesInfos = salesInfoService.listByReplenishmentDetailIds(detailIds, LocalDate.now().minusDays(16), LocalDate.now());
        List<RecentSuggestionDetailEntity> suggestionDetails = recentSuggestionDetailService.listByReplenishmentDetailIds(detailIds);
        List<ReplenishmentSuggestionFavoriteEntity> favoriteList = replenishmentSuggestionFavoriteService.listByReplenishmentIds(ids);
        for (ReplenishmentSuggestionVO.PagingView view : records) {
            SkuVO skuVO = skuVOS.stream().filter(v -> v.getSkuId().equals(view.getSkuId())).findFirst().orElse(new SkuVO());
            ShopInfoEntity shopInfoEntity = shopInfos.stream().filter(v -> v.getId().equals(view.getShopId())).findFirst().orElse(new ShopInfoEntity());
            DictCountryEntity dictCountry = countryList.stream().filter(v -> v.getId().equals(view.getCountry())).findFirst().orElse(new DictCountryEntity());
            boolean favorite = favoriteList.stream().anyMatch(v -> v.getReplenishmentSuggestionId().equals(view.getId()) && v.getUserId().equals(user.getUid()));
            view.setFavorite(favorite);
            view.setProductName(skuVO.getSkuName());
            view.setSkuImgUrl(skuVO.getSkuImagesUrl());
            view.setBrandName(skuVO.getBrandName());
            view.setCategoryName(skuVO.getCategoryName());
            view.setCountryName(dictCountry.getNameCn());
            view.setCountryImgUrl(dictCountry.getFlagUrl());
            view.setLogisticsMethodName(LogisticsMethodEnum.getName(view.getLogisticsMethod()));
            view.setLogisticsMinMethodName(LogisticsMethodEnum.getName(view.getLogisticsMinMethod()));
            view.setLogisticsMaxMethodName(LogisticsMethodEnum.getName(view.getLogisticsMaxMethod()));
            List<LabelVO> vos = labelVOS.stream().filter(v -> v.getReplenishmentId().equals(view.getId())).collect(Collectors.toList());
            view.setLabels(vos);
            view.setShopName(shopInfoEntity.getName());
            view.setAvgSalesQty(JSON.parseObject(view.getAvgSalesQtyJson(), new TypeReference<List<ReplenishmentSuggestionVO.SalesVO>>(){}));
            view.setSalesQty(JSON.parseObject(view.getSalesQtyJson(), new TypeReference<List<ReplenishmentSuggestionVO.SalesVO>>(){}));
            view.setSalesEstimateQty(JSON.parseObject(view.getSalesEstimateQtyJson(), new TypeReference<List<ReplenishmentSuggestionVO.SalesVO>>(){}));
            view.setAvgSalesEstimateQty(JSON.parseObject(view.getAvgSalesEstimateQtyJson(), new TypeReference<List<ReplenishmentSuggestionVO.SalesVO>>(){}));
            List<SalesInfoEntity> salesInfoList = salesInfos.stream().filter(v -> v.getReplenishmentDetailId().equals(view.getDetailId())).sorted(Comparator.comparing(SalesInfoEntity::getDate)).collect(Collectors.toList());
            List<LocalDate> salesAnalysisDate = salesInfoList.stream().map(SalesInfoEntity::getDate).collect(Collectors.toList());
            List<BigDecimal> salesAnalysisQty = salesInfoList.stream().map(SalesInfoEntity::getSalesQty).collect(Collectors.toList());
            //销量分析
            view.setSalesAnalysis(new ReplenishmentSuggestionVO.SalesAnalysisVO(salesAnalysisDate, salesAnalysisQty));
            SalesEstimateManualVO estimateManualVO = salesEstimateManuals.stream().filter(v -> v.getReplenishmentId().equals(view.getId()))
                    .map(v -> new SalesEstimateManualVO(v.getCurrentMonthSalesQty(), v.getNextMonthSales(), v.getFollowingMonthSales())).findFirst().orElse(null);
            //运营月销量预估
            view.setSalesEstimateManualVO(estimateManualVO);
            // 建议标识相关
            Map<String, RecentSuggestionDetailEntity> recentSuggestionDetailMap = suggestionDetails.stream().filter(v -> v.getReplenishmentDetailId().equals(view.getDetailId()))
                    .collect(Collectors.toMap(RecentSuggestionDetailEntity::getType, v -> v, (o1, o2) -> o1));
            // 最近断货日期
            RecentSuggestionDetailEntity recentOutOfStock = recentSuggestionDetailMap.get(RecentSuggestionEnum.RECENT_OUT_OF_STOCK.name());
            if (!ObjectUtils.isEmpty(recentOutOfStock)){
                view.setOutOfStockDay(new ReplenishmentSuggestionVO.DateVO(recentOutOfStock.getMarkType(), recentOutOfStock.getDate(), recentOutOfStock.getDays()));
            }
            // 最近建议发货日期
            RecentSuggestionDetailEntity recentSuggestShipping = recentSuggestionDetailMap.get(RecentSuggestionEnum.RECENT_DELIVERY.name());
            if (!ObjectUtils.isEmpty(recentSuggestShipping)) {
                view.setSuggestShippingDate(new ReplenishmentSuggestionVO.DateVO(recentSuggestShipping.getMarkType(), recentSuggestShipping.getDate(), recentSuggestShipping.getDays()));
                view.setSuggestShippingQty(recentSuggestShipping.getQty());
            }
            // 最近建议采购日期
            RecentSuggestionDetailEntity recentSuggestPurchase = recentSuggestionDetailMap.get(RecentSuggestionEnum.RECENT_PURCHASE.name());
            if (!ObjectUtils.isEmpty(recentSuggestPurchase)) {
                view.setSuggestPurchaseDate(new ReplenishmentSuggestionVO.DateVO(recentSuggestPurchase.getMarkType(), recentSuggestPurchase.getDate(), recentSuggestPurchase.getDays()));
                view.setSuggestPurchaseQty(recentSuggestPurchase.getQty());
            }
        }
    }

    @Override
    public ReplenishmentSuggestionVO.View view(String detailId) {
        ReplenishmentSuggestionVO.View view = baseMapper.view(detailId);
        LoginUser user = UserContext.getDefaultLoginUser();
        Boolean favorite = replenishmentSuggestionFavoriteService.isFavorite(user.getUid(), view.getId());
        view.setFavorite(favorite);
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Collections.singletonList(view.getCountry()));
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(Collections.singletonList(view.getSkuId()));
        List<ShopInfoEntity> shopInfos = shopInfoFeign.listShopInfoByIds(Collections.singletonList(view.getShopId()));
        SkuVO skuVO = skuVOS.stream().filter(v -> v.getSkuId().equals(view.getSkuId())).findFirst().orElse(new SkuVO());
        DictCountryEntity dictCountry = countryList.stream().filter(v -> v.getId().equals(view.getCountry())).findFirst().orElse(new DictCountryEntity());
        ShopInfoEntity shopInfoEntity = shopInfos.stream().filter(v -> v.getId().equals(view.getShopId())).findFirst().orElse(new ShopInfoEntity());
        view.setSkuImgUrl(skuVO.getSkuImagesUrl());
        view.setProductName(skuVO.getSkuName());
        view.setAvgSalesQty(JSON.parseObject(view.getAvgSalesQtyJson(), new TypeReference<List<ReplenishmentSuggestionVO.SalesVO>>(){}));
        view.setSalesEstimateQty(JSON.parseObject(view.getSalesEstimateQtyJson(), new TypeReference<List<ReplenishmentSuggestionVO.SalesVO>>(){}));
        view.setSalesQty(JSON.parseObject(view.getSalesQtyJson(), new TypeReference<List<ReplenishmentSuggestionVO.SalesVO>>(){}));
        view.setAvgSalesEstimateQty(JSON.parseObject(view.getAvgSalesEstimateQtyJson(), new TypeReference<List<ReplenishmentSuggestionVO.SalesVO>>(){}));
        view.setCountryName(dictCountry.getNameCn());
        view.setShopName(shopInfoEntity.getName());
        view.setLogisticsMethodName(LogisticsMethodEnum.getName(view.getLogisticsMethod()));
        view.setLogisticsMinMethodName(LogisticsMethodEnum.getName(view.getLogisticsMinMethod()));
        view.setLogisticsMaxMethodName(LogisticsMethodEnum.getName(view.getLogisticsMaxMethod()));
        List<LabelVO> labelVOS = labelInfoService.listLabelByReplenishmentId(view.getId());
        view.setLabels(labelVOS);
        return view;
    }

    @Override
    public PagingVO<FbaInTransitDetailVO> fbaInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        return fbaInTransitDetailService.fbaInTransitDetail(params);
    }

    @Override
    public PagingVO<OverseasInTransitDetailVO> overseasInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        return overseasInTransitDetailService.overseasInTransitDetail(params);
    }

    @Override
    public PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        return localInTransitDetailService.localInTransitDetail(params);
    }

    @Override
    public PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        return estimatedDeliveryDetailService.estimatedDelivery(params);
    }

    @Override
    public PagingVO<EstimatedPurchaseVO> estimatedPurchase(PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        return estimatedPurchaseDetailService.estimatedPurchase(params);
    }

    @Override
    public PagingVO<InventoryDetailVO> inventoryDetail(PagingDTO<InventoryTotalDTO> params) {
        return replenishmentInventoryDetailService.inventoryDetail(params);
    }

    @Override
    public SalesAnalysisVO salesAnalysis(SalesAnalysisDTO dto) {
        ReplenishmentSuggestionDetailEntity detail = replenishmentSuggestionDetailService.getById(dto.getDetailId());
        CfgRuleStrategyDTO cfgRuleStrategyDTO = JSON.parseObject(detail.getCfgRule(), CfgRuleStrategyDTO.class);
        CfgRuleSalesQtyDTO.StrategyResultDTO salesQtyResult = cfgRuleStrategyDTO.getSalesQtyResult();

        LocalDate startDate = dto.getStartDate().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(TimePeriodEstimateEnum.of(dto.getTimePeriod()).getDays() - 1L);
        Map<LocalDate, Integer> salesHistoryMap = listSalesHistoryMap(detail.getMainId(), salesQtyResult.getSalesQtyType(), salesQtyResult.getOrderType(), startDate, dto.getEndDate());
        SalesAnalysisVO salesAnalysisVO = new SalesAnalysisVO();
        List<SalesInfoEntity> list = salesInfoService.list(Wrappers.<SalesInfoEntity>lambdaQuery()
                .eq(SalesInfoEntity::getReplenishmentDetailId, dto.getDetailId())
                .between(SalesInfoEntity::getDate, dto.getStartDate().minusDays(1), dto.getEndDate())
                .orderByAsc(SalesInfoEntity::getDate)
        );
        List<SalesEstimateEntity> estimateEntityList = salesEstimateService.list(Wrappers.<SalesEstimateEntity>lambdaQuery()
                .eq(SalesEstimateEntity::getReplenishmentDetailId, dto.getDetailId())
                .between(SalesEstimateEntity::getDate, LocalDate.now(), endDate)
                .orderByAsc(SalesEstimateEntity::getDate)
        );
        List<LocalDate> dates = new ArrayList<>();
        while (startDate.isBefore(endDate)) {
            dates.add(startDate);
            startDate = startDate.plusDays(1);
        }
        List<BigDecimal> originalSales = new ArrayList<>();
        List<BigDecimal> sales = new ArrayList<>();
        List<BigDecimal> salesEstimates = new ArrayList<>();
        for (LocalDate date : dates) {
            SalesInfoEntity salesInfo = list.stream()
                    .filter(v -> v.getDate().equals(date))
                    .findFirst()
                    .orElse(new SalesInfoEntity());
            int saleQty = Optional.ofNullable(salesHistoryMap.get(date)).orElse(0);
            if (!date.isBefore(dto.getStartDate().minusDays(1)) && !date.isAfter(dto.getEndDate())) {
                originalSales.add(new BigDecimal(saleQty));
                sales.add(Optional.ofNullable(salesInfo.getSalesQty()).orElse(new BigDecimal(saleQty)));
            } else {
                originalSales.add(null);
                sales.add(null);
            }
            SalesEstimateEntity estimate = estimateEntityList.stream()
                    .filter(v -> v.getDate().equals(date))
                    .findFirst()
                    .orElse(new SalesEstimateEntity());
            salesEstimates.add(estimate.getSalesQty());
        }
        salesAnalysisVO.setDate(dates);
        salesAnalysisVO.setDenoisingSales(originalSales);
        salesAnalysisVO.setHistorySales(sales);
        salesAnalysisVO.setEstimatesSales(salesEstimates);
        return salesAnalysisVO;
    }

    @Override
    public HistoryInventoryVO historyInventory(HistoryInventoryDTO dto) {
        List<HistoryInventoryEsEntity> historyInventoryList = historyInventoryEsService.findByReplenishmentIdAndDateBetween(dto.getDetailId(), dto.getStartDate(), dto.getEndDate());
        Map<LocalDate, Integer> inventoryMap = historyInventoryList.stream()
                .collect(Collectors.toMap(HistoryInventoryEsEntity::getDate, HistoryInventoryEsEntity::getOriginalInventQty, (o1, o2) -> o1));
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate startDate = dto.getStartDate();
        List<Integer> qty = new ArrayList<>();
        // 遍历每一天
        while (!startDate.isAfter(dto.getEndDate())) {
            dateList.add(startDate);
            startDate = startDate.plusDays(1);
            qty.add(Optional.ofNullable(inventoryMap.get(startDate)).orElse(0));
        }
        return HistoryInventoryVO.buildHistoryInventoryVO(dateList, qty);
    }

    @Override
    public List<RptOutOfStockVO> outOfStockReport(String detailId) {
        List<RptOutOfStockEntity> list = rptOutOfStockService.list(Wrappers.<RptOutOfStockEntity>lambdaQuery()
                .eq(RptOutOfStockEntity::getReplenishmentDetailId, detailId)
                .orderByAsc(RptOutOfStockEntity::getDate));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        //处理数据相同断货开始日期的数据合并 并按照断货开始日期升序
        Map<LocalDate, RptOutOfStockVO> outOfStockVOMap = list.stream().collect(Collectors.groupingBy(RptOutOfStockEntity::getStartDate,
                Collectors.collectingAndThen(Collectors.toList(), e -> {
                    RptOutOfStockVO rptOutOfStockVO = new RptOutOfStockVO();
                    e.stream().sorted(Comparator.comparing(RptOutOfStockEntity::getEndDate)).forEach(v -> {
                        rptOutOfStockVO.setStartDate(v.getStartDate());
                        rptOutOfStockVO.setEndDate(v.getEndDate());
                        rptOutOfStockVO.setSalesQty(Optional.ofNullable(rptOutOfStockVO.getSalesQty()).orElse(BigDecimal.ZERO).add( v.getSalesQty()));
                        rptOutOfStockVO.setDays(Math.addExact(Optional.ofNullable(rptOutOfStockVO.getDays()).orElse(0), 1));
                        rptOutOfStockVO.setAmount(Optional.ofNullable(rptOutOfStockVO.getAmount())
                                .orElse(BigDecimal.ZERO).add(v.getAmount()));
                    });
                    return rptOutOfStockVO;
                })));
        // 判断最早的一天是否存在断货开始时间小于今日
        LocalDate minOutOfStock = outOfStockVOMap.keySet().stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        if (minOutOfStock.isBefore(LocalDate.now())) {
            //今日之前的日期属于真实断货
            RptOutOfStockVO rptOutOfStock = outOfStockVOMap.get(minOutOfStock);
            List<RealOutOfStockEntity> realOutOfStock = realOutOfStockService.list(Wrappers.<RealOutOfStockEntity>lambdaQuery()
                    .eq(RealOutOfStockEntity::getReplenishmentDetailId, detailId)
                    .ge(RealOutOfStockEntity::getDate, minOutOfStock)
            );
            BigDecimal amount = realOutOfStock.stream().map(RealOutOfStockEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal salesQty = realOutOfStock.stream().map(RealOutOfStockEntity::getSalesQty).reduce(BigDecimal.ZERO, BigDecimal::add);
            rptOutOfStock.setSalesQty(rptOutOfStock.getSalesQty().add(salesQty));
            rptOutOfStock.setAmount(rptOutOfStock.getAmount().add(amount));
            rptOutOfStock.setDays(rptOutOfStock.getDays() + realOutOfStock.size());
            outOfStockVOMap.put(minOutOfStock, rptOutOfStock);
        }
        return outOfStockVOMap.values().stream()
                .sorted(Comparator.comparing(RptOutOfStockVO::getStartDate))
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal outOfStockReportTotal(String detailId) {
        List<RptOutOfStockEntity> list = rptOutOfStockService.list(Wrappers.<RptOutOfStockEntity>lambdaQuery()
                .eq(RptOutOfStockEntity::getReplenishmentDetailId, detailId)
                .orderByAsc(RptOutOfStockEntity::getDate));
        if (CollectionUtils.isEmpty(list)) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = list.stream().map(RptOutOfStockEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        // 判断最早的一天是否存在断货开始时间小于今日
        RptOutOfStockEntity rptOutOfStock = list.get(0);
        if (rptOutOfStock.getStartDate().isBefore(LocalDate.now())) {
            //今日之前的日期属于真实断货
            List<RealOutOfStockEntity> realOutOfStock = realOutOfStockService.list(Wrappers.<RealOutOfStockEntity>lambdaQuery()
                    .eq(RealOutOfStockEntity::getReplenishmentDetailId, detailId)
                    .ge(RealOutOfStockEntity::getDate, rptOutOfStock.getStartDate())
            );
            BigDecimal realAmount = realOutOfStock.stream().map(RealOutOfStockEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            amount = amount.add(realAmount);
        }
        return amount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO notRestockingReplenishment(String id, String replenishmentRemark) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        if (!ReplenishmentTypeEnum.NORMAL.getCode().equals(entity.getReplenishmentType())) {
            throw new ServiceException(ApiError.ERROR_NOT_RESTOCKING_REPLENISHMENT);
        }
        updateIsReplenishment(id, replenishmentRemark, ReplenishmentTypeEnum.NOT_RESTOCKING.getCode());

        // 操作日志
        String msg = StrUtil.format("操作了暂不补货，原因：【{}】 ",replenishmentRemark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "暂不补货");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchNotRestockingReplenishment(List<String> ids, String replenishmentRemark) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        update(Wrappers.<ReplenishmentSuggestionEntity>lambdaUpdate()
                .in(ReplenishmentSuggestionEntity::getId, ids)
                .set(ReplenishmentSuggestionEntity::getReplenishmentRemark, replenishmentRemark)
                .set(ReplenishmentSuggestionEntity::getReplenishmentType, ReplenishmentTypeEnum.NOT_RESTOCKING.getCode())
                .set(ReplenishmentSuggestionEntity::getIsManual, Boolean.TRUE)
        );
        // 操作日志
        List<Pair<String, String>> addPairList = ids.stream().map(obj -> new Pair<>(obj, replenishmentRemark)).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("操作了暂不补货，原因：【{}】 ", ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), addPairList, "暂不补货");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO restoreReplenishment(String id, String replenishmentRemark) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        if (!ReplenishmentTypeEnum.NOT_RESTOCKING.getCode().equals(entity.getReplenishmentType())) {
            throw new ServiceException(ApiError.ERROR_RESTORE_REPLENISHMENT);
        }
        updateIsReplenishment(id, replenishmentRemark, ReplenishmentTypeEnum.NORMAL.getCode());
        // 操作日志
        String msg = StrUtil.format("操作了恢复补货，原因：【{}】 ",replenishmentRemark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "恢复补货");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO batchUpdateRule(String id, CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO, CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO) {
        if (ObjectUtil.isEmpty(stockUpUpdateDTO) && ObjectUtil.isEmpty(salesQtyUpdateDTO)) {
            throw new ServiceException("备货、销量设置不能全部为空！");
        }
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"补货建议");
        }

        //更新备货信息
        if (ObjectUtil.isNotEmpty(stockUpUpdateDTO)) {
            stockUpUpdateDTO.setRefId(id);
            stockUpUpdateDTO.setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            cfgRuleStockUpService.customUpdate(stockUpUpdateDTO);
        }
        //更新销量信息
        if (ObjectUtil.isNotEmpty(salesQtyUpdateDTO)) {
            salesQtyUpdateDTO.setRefId(id);
            salesQtyUpdateDTO.setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            salesQtyUpdateDTO.setIsCustom(Boolean.TRUE);
            cfgRuleSalesQtyService.update(salesQtyUpdateDTO);
        }
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }
    /**
     * 销量添加默认设置
     * @author will
     * @date 2024/9/10 18:23
     * @param entity
     * @param updateDetailDTO
     */
    private void formatSalesQtyDefaultData (ReplenishmentSuggestionEntity entity,CfgRuleSalesQtyDTO.UpdateDetailDTO updateDetailDTO) {
//        List<ReplenishmentSuggestionDetailEntity> detailList = replenishmentSuggestionDetailService.listByMainIdList(Arrays.asList(entity.getId()));
//        if (CollectionUtils.isEmpty(detailList)) {
//            return;
//        }
//        String skuType = detailList.get(0).getSkuType();
//        CfgRuleSalesQtyDTO.StrategyResultDTO defaultCfgRuleSalesQty = cfgRuleSalesQtyService.getDefaultCfgRuleSalesQty(entity.getPlatformType(), skuType);
//        if (ObjectUtil.isEmpty(defaultCfgRuleSalesQty) || CollectionUtils.isEmpty(defaultCfgRuleSalesQty.getFormulaResults())) {
//            return;
//        }
//        CfgRuleSalesQtyDTO.StrategyFormulaResultDTO strategyFormulaResultDTO = defaultCfgRuleSalesQty.getFormulaResults().stream().filter(obj -> StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode())).findFirst().orElse(null);
//        if (ObjectUtil.isEmpty(strategyFormulaResultDTO)) {
//            return;
//        }
//        CfgRuleSalesFormulaDTO.DefaultUpdateDTO defaultUpdateDTO = BeanMapperUtils.map(CfgRuleSalesFormulaDTO.DefaultUpdateDTO.class, strategyFormulaResultDTO);
//        defaultUpdateDTO.setId(null);
//        CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = JSONUtil.toBean(strategyFormulaResultDTO.getPercentJson(), CfgRuleSalesFormulaDTO.PercentJsonDTO.class);
//        defaultUpdateDTO.setPercentJsonDTO(percentJsonDTO);
//        updateDetailDTO.setDefaultSalesQtyDTO(defaultUpdateDTO);
    }


    /**
     * 备货设置添加默认信息
     * @author will
     * @date 2024/9/10 17:59
     * @param entity
     * @param stockUpUpdateDTO
     */
    private void formatStockUpDefaultData (ReplenishmentSuggestionEntity entity,CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO) {
        CfgRuleStockUpEntity defaultCfgRuleStockUp = cfgRuleStockUpService.getDefaultCfgRuleStockUp(entity.getPlatformType());
        if (ObjectUtil.isEmpty(defaultCfgRuleStockUp)) {
            return;
        }
        //采购审批
        Integer purchaseApproveDays = ObjectUtil.isEmpty(stockUpUpdateDTO.getPurchaseApproveDays()) ? defaultCfgRuleStockUp.getPurchaseApproveDays() : null;
        stockUpUpdateDTO.setPurchaseApproveDays(purchaseApproveDays);
        //生产周期
        Integer productionDays = ObjectUtil.isEmpty(stockUpUpdateDTO.getProductionDays()) ? defaultCfgRuleStockUp.getProductionDays() : null;
        stockUpUpdateDTO.setProductionDays(productionDays);
        //供应商发货
        Integer supplierDeliveryDays = ObjectUtil.isEmpty(stockUpUpdateDTO.getSupplierDeliveryDays()) ? defaultCfgRuleStockUp.getSupplierDeliveryDays() : null;
        stockUpUpdateDTO.setSupplierDeliveryDays(supplierDeliveryDays);
        //质检入库
        Integer qcDays = ObjectUtil.isEmpty(stockUpUpdateDTO.getQcDays()) ? defaultCfgRuleStockUp.getQcDays() : null;
        stockUpUpdateDTO.setQcDays(qcDays);
        //采购频率
        Integer purchaseCycleDays = ObjectUtil.isEmpty(stockUpUpdateDTO.getPurchaseCycleDays()) ? defaultCfgRuleStockUp.getPurchaseCycleDays() : null;
        stockUpUpdateDTO.setPurchaseCycleDays(purchaseCycleDays);
        //FBA安全天数
        Integer safeDays = ObjectUtil.isEmpty(stockUpUpdateDTO.getSafeDays()) ? defaultCfgRuleStockUp.getSafeDays() : null;
        stockUpUpdateDTO.setSafeDays(safeDays);
        //默认备货系数
        BigDecimal stockingRatio = ObjectUtil.isEmpty(stockUpUpdateDTO.getStockingRatio()) ? defaultCfgRuleStockUp.getStockingRatio() : null;
        stockUpUpdateDTO.setStockingRatio(stockingRatio);

        //默认物流信息
        List<CfgRuleLogisticsEntity> cfgRuleLogisticsList = cfgRuleLogisticsService.listByStockUpIdList(Arrays.asList(defaultCfgRuleStockUp.getId()));
        if (CollectionUtils.isNotEmpty(cfgRuleLogisticsList)) {
            List<CfgRuleLogisticsDTO.UpdateDTO> cfgLogisticsList = BeanMapperUtils.copyList(CfgRuleLogisticsDTO.UpdateDTO.class,cfgRuleLogisticsList);
            cfgLogisticsList.stream().forEach(obj -> obj.setId(null));
            stockUpUpdateDTO.setCfgLogisticsList(cfgLogisticsList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO restoreRule(String id, List<String> ruleTypeList) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        if (CollectionUtils.isEmpty(ruleTypeList)) {
            ruleTypeList = Arrays.asList(ReplenishmentRuleTypeEnum.STOCK_UP.getCode(),ReplenishmentRuleTypeEnum.SALES.getCode());
        }
        //恢复备货规则
        if (ruleTypeList.contains(ReplenishmentRuleTypeEnum.STOCK_UP.getCode())) {
            cfgRuleStockUpService.deleteByRefId(entity.getId());
        }
        //恢复销量规则
        if (ruleTypeList.contains(ReplenishmentRuleTypeEnum.SALES.getCode())) {
            cfgRuleSalesQtyService.deleteByRefId(entity.getId());
        }
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO favorite(String id) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));

        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        Boolean isFavorite = replenishmentSuggestionFavoriteService.isFavorite(userInfo.getUid(), entity.getId());
        if (isFavorite) {
            return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "补货建议已关注，无需再次关注");
        }

        ReplenishmentSuggestionFavoriteDTO.AddDTO dto = new ReplenishmentSuggestionFavoriteDTO.AddDTO();
        dto.setUserId(userInfo.getUid());
        dto.setReplenishmentSuggestionId(entity.getId());
        replenishmentSuggestionFavoriteService.add(dto);

        // 操作日志
        String msg = StrUtil.format("设置了关注");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "关注");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelFavorite(String id) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        Boolean isFavorite = replenishmentSuggestionFavoriteService.isFavorite(userInfo.getUid(), entity.getId());
        if (!isFavorite) {
            return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "补货建议未关注，无需取消关注");
        }
        replenishmentSuggestionFavoriteService.cancelFavorite(userInfo.getUid(),entity.getId());

        // 操作日志
        String msg = StrUtil.format("设置了取消关注");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "取消关注");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateLabel(ReplenishmentSuggestionDTO.UpdateLabelDTO updateLabelDTO) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(updateLabelDTO.getId()).orElseThrow(() -> new ServiceException("未找到补货建议数据"));

        //原标签
        List<LabelInfoDTO.ViewDTO> oldList = replenishmentRefLabelService.listLabelInfoByRefId(updateLabelDTO.getId());
        String oldLabelNames = oldList.stream().map(LabelInfoDTO.ViewDTO::getName).distinct().collect(Collectors.joining(","));

        //单个编辑标签
        ReplenishmentRefLabelDTO.UpdateDTO dto = new ReplenishmentRefLabelDTO.UpdateDTO();
        dto.setLabelIdList(updateLabelDTO.getLabelIdList());
        dto.setType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        replenishmentRefLabelService.update(dto,entity.getId());

        //现标签
        List<LabelInfoEntity> labelInfoList = CollectionUtils.isEmpty(updateLabelDTO.getLabelIdList()) ? Collections.EMPTY_LIST : labelInfoService.listByIds(updateLabelDTO.getLabelIdList());
        String labelNames = labelInfoList.stream().map(LabelInfoEntity::getName).collect(Collectors.joining(","));
        // 操作日志
        String msg = StrUtil.format("设置了标签：从【{}】修改为【{}】",oldLabelNames,labelNames);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "设置标签");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO batchAddLabel(String id, List<String> labelIdList) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        if (CollectionUtils.isEmpty(labelIdList)) {
            return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
        }
        //新增标签
        ReplenishmentRefLabelDTO.UpdateDTO dto = new ReplenishmentRefLabelDTO.UpdateDTO();
        dto.setLabelIdList(labelIdList);
        dto.setType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        dto.setIsIncrement(Boolean.TRUE);
        replenishmentRefLabelService.update(dto,entity.getId());
        // 操作日志
        List<LabelInfoEntity> labelInfoList = CollectionUtils.isEmpty(labelIdList) ? Collections.EMPTY_LIST : labelInfoService.listByIds(labelIdList);
        String labelNames = labelInfoList.stream().map(LabelInfoEntity::getName).collect(Collectors.joining(","));
        String msg = StrUtil.format("添加了标签【{}】",labelNames);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "添加标签");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelLabel(String id, List<String> labelIdList) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        if (CollectionUtils.isEmpty(labelIdList)) {
            return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.DELETE);
        }
        //取消标签
        replenishmentRefLabelService.deleteLabel(labelIdList,entity.getId());

        List<LabelInfoEntity> labelInfoList = CollectionUtils.isEmpty(labelIdList) ? Collections.EMPTY_LIST : labelInfoService.listByIds(labelIdList);
        String labelNames = labelInfoList.stream().map(LabelInfoEntity::getName).collect(Collectors.joining(","));
        String msg = StrUtil.format("删除了标签【{}】",labelNames);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "删除标签");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.DELETE);
    }

    /**
     * 手动更新是否补货和补货原因
     *
     * @param id
     * @param replenishmentRemark
     * @param replenishmentType
     * @author will
     * @date 2024/8/29 15:19
     */
    private Boolean updateIsReplenishment(String id, String replenishmentRemark, String replenishmentType) {
        return lambdaUpdate().eq(ReplenishmentSuggestionEntity::getId, id)
                .set(ReplenishmentSuggestionEntity::getReplenishmentRemark, replenishmentRemark)
                .set(ReplenishmentSuggestionEntity::getReplenishmentType, replenishmentType)
                .set(ReplenishmentSuggestionEntity::getIsManual,Boolean.TRUE)
                .update();
    }
    @Override
    public List<ReplenishmentSuggestionEntity> listAllSkuAndShop() {
        return list(Wrappers.<ReplenishmentSuggestionEntity>lambdaQuery()
                .select(ReplenishmentSuggestionEntity::getSkuId,
                        ReplenishmentSuggestionEntity::getSkuNo,
                        ReplenishmentSuggestionEntity::getShopId,
                        ReplenishmentSuggestionEntity::getId));
    }

    @Override
    public List<ReplenishmentSuggestionEntity> listByUnique(List<String> platformCodeList, List<String> shopIdList, List<String> skuIdList) {
        if (CollectionUtils.isEmpty(platformCodeList) || CollectionUtils.isEmpty(shopIdList) || CollectionUtils.isEmpty(skuIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listByUnique(platformCodeList,shopIdList,skuIdList);
    }

    @Override
    public List<ReplenishmentSuggestionEntity> listCalculationData(String platformType) {
        return list(Wrappers.<ReplenishmentSuggestionEntity>lambdaQuery()
                .eq(ReplenishmentSuggestionEntity::getPlatformType, platformType)
                .not(wrapper -> wrapper
                        .eq(ReplenishmentSuggestionEntity::getReplenishmentType, ReplenishmentTypeEnum.NOT_RESTOCKING.getCode())
                        .eq(ReplenishmentSuggestionEntity::getIsManual, true)
                )
                .orderByAsc(ReplenishmentSuggestionEntity::getSkuId));
    }

    @Override
    public List<LabelInfoDTO.ViewDTO> listLabelInfoById(String id) {
        return  replenishmentRefLabelService.listLabelInfoByRefId(id);
    }

    @Override
    public List<String> listLabelIdById(String id) {
        List<LabelInfoDTO.ViewDTO> list = replenishmentRefLabelService.listLabelInfoByRefId(id);
        if (CollectionUtils.isEmpty(list)) {
            return  Collections.EMPTY_LIST;
        }
        return list.stream().filter(obj -> StrUtil.isNotBlank(obj.getId())).map(LabelInfoDTO.ViewDTO::getId).distinct().collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateRemark(String id, String remark) {
        ReplenishmentSuggestionEntity old = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        //新建对象
        ReplenishmentSuggestionEntity entity = new ReplenishmentSuggestionEntity();
        BeanMapperUtils.copy(old,entity);
        entity.setRemark(remark);
        this.updateById(entity);
        // 操作日志
        String msg = StrUtil.format("编辑了备注：从【{}】修改为【{}】",old.getRemark(),remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), old.getId(), "编辑备货");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    public Boolean exportReplenishmentRule(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("补货规则", FileTaskEventEnum.EXPORT_MRP_REPLENISHMENT_RULE.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportHistorySalesQty(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("历史销量", FileTaskEventEnum.EXPORT_MRP_HISTORY_SALES_QTY.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportPurchaseSuggestion(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("补货计划_采购建议", FileTaskEventEnum.EXPORT_MRP_PURCHASE_SUGGESTION.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportDeliverySuggest(ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("补货计划_发货建议", FileTaskEventEnum.EXPORT_MRP_DELIVERY_SUGGESTION.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    @Override
    public List<ReplenishmentSuggestionEntity> listByPlatform(String platformType) {
        return list(Wrappers.<ReplenishmentSuggestionEntity>lambdaQuery().eq(ReplenishmentSuggestionEntity::getPlatformType, platformType));
    }

    @Override
    public PagingVO<DynamicExcelDTO> listHistorySalesQty(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        LoginUser user = UserContext.getDefaultLoginUser();
        Page<ReplenishmentSuggestionVO.PagingView> pagingVO = baseMapper.paging(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams(),user.getUid());
        List<ReplenishmentSuggestionVO.PagingView> list = pagingVO.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException("未找到历史销量数据");
        }
        //历史销量数据处理
        List<LinkedHashMap> resultList = handleHistorySalesQty(list);
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException("未找到历史销量数据");
        }
        LinkedHashMap headMap = (LinkedHashMap) resultList.get(0).get("head");
        List<LinkedHashMap<String ,Object>> convertDataList = (List<LinkedHashMap<String ,Object>>) resultList.get(0).get("data");
        DynamicExcelDTO excelDTO = new DynamicExcelDTO();
        excelDTO.setHeaders(headMap);
        excelDTO.setData(convertDataList);
        excelDTO.setSheetName("销售订单");
        return new PagingVO(Collections.singletonList(excelDTO), (int) pagingVO.getTotal(),(int) pagingVO.getSize(), (int)pagingVO.getCurrent());
    }

    /**
     * 历史销量数据处理
     * @author will
     * @date 2024/9/8 16:03
     * @param list
     * @return List<LinkedHashMap>
     */
    private List<LinkedHashMap> handleHistorySalesQty (List<ReplenishmentSuggestionVO.PagingView> list) {
        List<LinkedHashMap> resultList = Lists.newArrayList();
        LinkedHashMap<String, Object> resultMap = Maps.newLinkedHashMap();
        // 标题
        LinkedHashMap headMap = Maps.newLinkedHashMap();

        // 动态标题
        LinkedHashMap dyHeadMap = Maps.newLinkedHashMap();
        // 结果集
        List<LinkedHashMap> convertDataList = Lists.newArrayListWithExpectedSize(list.size());

        // 公共标题字段
        Arrays.asList(SalesInfoExportHeaderEnum.values()).forEach(headerEnum -> {
            headMap.put(headerEnum.getCode(), headerEnum.getName());
        });

        //历史销量
        List<String> detailIdList = list.stream().map(ReplenishmentSuggestionVO.PagingView::getDetailId).distinct().collect(Collectors.toList());
        List<SalesInfoEntity> salesInfoList = salesInfoService.listHistorySalesInfo(detailIdList);
        if (CollectionUtils.isEmpty(salesInfoList)) {
            return Collections.EMPTY_LIST;
        }
        // 动态字段标题
        salesInfoList.stream().forEach(obj ->{
            headMap.put(obj.getDate().toString(),LocalDateTimeUtil.format(obj.getDate(), DateTimeFormatter.ofPattern("yyyy年MM月dd")));
            dyHeadMap.put(obj.getDate().toString(),LocalDateTimeUtil.format(obj.getDate(), DateTimeFormatter.ofPattern("yyyy年MM月dd")));
        });

        //平台
        List<DictBasicDTO.ViewDTO> platformViewList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

        //店铺
        List<String> shopIdList = list.stream().map(ReplenishmentSuggestionVO.PagingView::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

        //SKU
        List<String> skuIdList = list.stream().map(ReplenishmentSuggestionVO.PagingView::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        for (ReplenishmentSuggestionVO.PagingView pagingView : list) {
            LinkedHashMap<String, Object> convertMap = new LinkedHashMap<>();
            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), pagingView.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");
            //平台名称
            String platformName = platformViewList.stream().filter(obj -> StrUtil.equals(obj.getValue(), pagingView.getPlatform())).map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");
            //产品名称
            String productName = skuList.stream().filter(obj -> StrUtil.equals(obj.getId(), pagingView.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");

            List<SalesInfoEntity> salesList = salesInfoList.stream().filter(obj -> StrUtil.equals(obj.getReplenishmentDetailId(), pagingView.getDetailId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(salesList)) {
                continue;
            }

            convertMap.put("platform",platformName);
            convertMap.put("shopName",shopName);
            convertMap.put("skuNo",pagingView.getSkuNo());
            convertMap.put("productName",productName);
            convertMap.put("typeName","FBA");
            //历史销量
            dyHeadMap.keySet().stream().forEach(obj ->{
                SalesInfoEntity salesInfoEntity = salesList.stream().filter(e -> StrUtil.equals(e.getDate().toString(), obj.toString())).findFirst().orElse(null);
                BigDecimal salesQty = ObjectUtil.isNotEmpty(salesInfoEntity) ? salesInfoEntity.getSalesQty() : BigDecimal.ZERO;
                convertMap.put(obj.toString(),salesQty);
            });
            convertDataList.add(convertMap);
        }
        if (CollectionUtils.isEmpty(convertDataList)) {
            return Collections.EMPTY_LIST;
        }
        resultMap.put("head", headMap);
        resultMap.put("data", convertDataList);
        resultList.add(resultMap);
        return resultList;
    }

    @Override
    public PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> listReplenishmentRule(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        LoginUser user = UserContext.getDefaultLoginUser();
        Page<ReplenishmentSuggestionVO.PagingView> pagingVO = baseMapper.paging(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams(),user.getUid());
        if (CollectionUtils.isEmpty(pagingVO.getRecords())) {
            return new PagingVO<>();
        }
        List<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> exportPagingVO = handleExport(pagingVO.getRecords());
        return new PagingVO(exportPagingVO,(int)pagingVO.getTotal(),(int)pagingVO.getSize(),(int)pagingVO.getCurrent());
    }

    @Override
    public List<LocalInventoryDTO.ShopSalesDTO> getSalesByShopIds(List<String> shopIds, String skuId, CfgRuleSalesQtyDTO.StrategyResultDTO salesQtyResult) {
        // 根据店铺加sku查询建议id
        List<ReplenishmentSuggestionEntity> suggestionList = list(Wrappers.<ReplenishmentSuggestionEntity>lambdaQuery()
                .eq(ReplenishmentSuggestionEntity::getSkuId, skuId)
                .in(ReplenishmentSuggestionEntity::getShopId, shopIds)
        );
        Map<String, String> suggestionMap = suggestionList.stream().collect(Collectors.toMap(ReplenishmentSuggestionEntity::getId, ReplenishmentSuggestionEntity::getShopId, (o1, o2) -> o1));

        if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(salesQtyResult.getSalesQtyType())) {
            List<OrderHistorySalesEsEntity> historySalesList = orderHistorySalesEsService.getRecentSalesBySuggestionIds(suggestionMap.keySet(), salesQtyResult.getOrderType());
            return historySalesList.stream()
                    .map(v -> new LocalInventoryDTO.ShopSalesDTO(suggestionMap.get(v.getReplenishmentId()), v.getOriginalSalesQty()))
                    .collect(Collectors.toList());
        } else {
            // 以销售出库单出库时间计算销量
            List<OutStockHistorySalesEsEntity> historySalesList =  outStockHistorySalesEsService.getRecentSalesBySuggestionIds(suggestionMap.keySet(), salesQtyResult.getOrderType());
            return historySalesList.stream()
                    .map(v -> new LocalInventoryDTO.ShopSalesDTO(suggestionMap.get(v.getReplenishmentId()), v.getOriginalSalesQty()))
                    .collect(Collectors.toList());
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReplenishment(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        ReplenishmentSuggestionDetailEntity entity = ReplenishmentResultDTO.DetailDTO.buildReplenishmentSuggestionDetail(replenishmentResult.getReplenishmentDetail(), cfgRuleStrategy,
                replenishmentResult.getTimePeriodSalesEstimates(), replenishmentResult.getAvgTimePeriodSalesEstimates(), replenishmentResult.getTimePeriodSales(),
                replenishmentResult.getAvgTimePeriodSales(), replenishmentResult.getPurchasePrice(), replenishmentResult.getSalesPrice());
        if (CollectionUtils.isNotEmpty(replenishmentResult.getFbaInTransitDetails())) {
            List<FbaInTransitDetailEntity> fbaInTransitDetailEntities = replenishmentResult.getFbaInTransitDetails().stream()
                    .map(v -> ReplenishmentResultDTO.FbaInTransitDetailDTO.buildFbaInTransitDetail(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            fbaInTransitDetailService.saveBatch(fbaInTransitDetailEntities);
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getFbaDeliveryDetails())) {
            List<EstimatedDeliveryDetailEntity> fbaDeliveryDetails = replenishmentResult.getFbaDeliveryDetails().stream()
                    .map(v -> ReplenishmentResultDTO.EstimatedDeliveryDetailDTO.buildEstimatedDeliveryDetail(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            estimatedDeliveryDetailService.saveBatch(fbaDeliveryDetails);
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getOverseasUsableDetail())) {
            replenishmentInventoryDetailService.saveInventoryDetail(replenishmentResult.getOverseasUsableDetail(), replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion());
        }

        if (CollectionUtils.isNotEmpty(replenishmentResult.getOverseasInTransitDetails())) {
            List<OverseasInTransitDetailEntity> overseasInTransitDetails = replenishmentResult.getOverseasInTransitDetails().stream()
                    .map(v -> ReplenishmentResultDTO.OverseasInTransitDetailDTO.buildOverseasInTransitDetail(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            overseasInTransitDetailService.saveBatch(overseasInTransitDetails);
        }

        if (CollectionUtils.isNotEmpty(replenishmentResult.getOverseasInTransitDetail())) {
            replenishmentInventoryDetailService.saveInventoryDetail(replenishmentResult.getOverseasInTransitDetail(), replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion());
        }

        if (CollectionUtils.isNotEmpty(replenishmentResult.getOverseasDeliveryDetails())) {
            List<EstimatedDeliveryDetailEntity> overseasDeliveryDetails = replenishmentResult.getOverseasDeliveryDetails().stream()
                    .map(v -> ReplenishmentResultDTO.EstimatedDeliveryDetailDTO.buildEstimatedDeliveryDetail(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            estimatedDeliveryDetailService.saveBatch(overseasDeliveryDetails);
        }

        if (CollectionUtils.isNotEmpty(replenishmentResult.getOverseasDeliveryDetail())) {
            replenishmentInventoryDetailService.saveInventoryDetail(replenishmentResult.getOverseasDeliveryDetail(), replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion());
        }

        if (CollectionUtils.isNotEmpty(replenishmentResult.getLocalUsableDetail())) {
            replenishmentInventoryDetailService.saveInventoryDetail(replenishmentResult.getLocalUsableDetail(), replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion());
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getLocalInTransitDetails())) {
            List<LocalInTransitDetailEntity> localInTransitDetails = replenishmentResult.getLocalInTransitDetails().stream()
                    .map(v -> ReplenishmentResultDTO.LocalInTransitDetailDTO.buildLocalInTransitDetail(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            localInTransitDetailService.saveBatch(localInTransitDetails);
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getLocalInTransitDetail())) {
            replenishmentInventoryDetailService.saveInventoryDetail(replenishmentResult.getLocalInTransitDetail(), replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion());
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getLocalPurchaseDetails())) {
            List<EstimatedPurchaseDetailEntity> localPurchaseDetails = replenishmentResult.getLocalPurchaseDetails().stream()
                    .map(v -> ReplenishmentResultDTO.EstimatedPurchaseDetailDTO.buildEstimatedPurchaseDetail(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            estimatedPurchaseDetailService.saveBatch(localPurchaseDetails);
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getLocalPurchaseDetail())) {
            replenishmentInventoryDetailService.saveInventoryDetail(replenishmentResult.getLocalPurchaseDetail(), replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion());
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getRptOutOfStocks())) {
            List<RptOutOfStockEntity> rptOutOfStocks = replenishmentResult.getRptOutOfStocks().stream()
                    .map(v -> ReplenishmentResultDTO.RptOutOfStockDTO.buildRptOutOfStock(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            rptOutOfStockService.saveBatch(rptOutOfStocks);
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getSalesInfoList())) {
            List<SalesInfoEntity> salesInfos = replenishmentResult.getSalesInfoList().stream()
                    .map(v -> ReplenishmentResultDTO.SalesInfoDTO.buildSalesInfo(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            salesInfoService.saveOrUpdateBatch(salesInfos);
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getSalesEstimates())) {
            List<SalesEstimateEntity> salesEstimates = replenishmentResult.getSalesEstimates().stream()
                    .map(v -> ReplenishmentResultDTO.SalesEstimateDTO.buildSalesEstimate(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            salesEstimateService.saveBatch(salesEstimates);
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getDeliverySuggests())) {
            List<DeliverySuggestEntity> deliverySuggests = replenishmentResult.getDeliverySuggests().stream()
                    .map(ReplenishmentResultDTO.DeliverySuggestDTO::buildDeliverySuggest)
                    .collect(Collectors.toList());
            deliverySuggestService.saveBatch(deliverySuggests);
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getPurchaseSuggests())) {
            List<PurchaseSuggestEntity> purchaseSuggests = replenishmentResult.getPurchaseSuggests().stream()
                    .map(ReplenishmentResultDTO.PurchaseSuggestDTO::buildPurchaseSuggest)
                    .collect(Collectors.toList());
            purchaseSuggestService.saveBatch(purchaseSuggests);
        }
        if (CollectionUtils.isNotEmpty(replenishmentResult.getRecentSuggestions())) {
            List<RecentSuggestionDetailEntity> recentSuggestionDetails = replenishmentResult.getRecentSuggestions().stream()
                    .map(v -> ReplenishmentResultDTO.RecentSuggestionDTO.buildRecentSuggestionEntity(v, replenishmentResult.getReplenishmentDetail().getDetailId(), replenishmentResult.getReplenishmentDetail().getCalcVersion()))
                    .collect(Collectors.toList());
            recentSuggestionDetailService.saveBatch(recentSuggestionDetails);
        }
        replenishmentSuggestionDetailService.saveOrUpdate(entity);
    }

    @Override
    public List<ReplenishmentResultDTO> listAllCalculationData(String platformType, String salesQtyType, String orderType, LocalDate calculationDate) {

        List<ReplenishmentSuggestionEntity> entities = baseMapper.listAllCalculationData(platformType);
        List<String> suggestionIds = entities.stream().map(ReplenishmentSuggestionEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(suggestionIds)) {
            return Collections.emptyList();
        }
        List<ReplenishmentSuggestionDetailEntity> detailList = getReplenishmentSuggestionDetailEntities(suggestionIds);
        List<String> detailsIds = detailList.stream().map(ReplenishmentSuggestionDetailEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(detailsIds)) {
            return Collections.emptyList();
        }
        LocalDate caleStartDate = calculationDate.minusDays(361);
        LocalDate caleEndDate = calculationDate.minusDays(1);
        //获取历史数据
        List<ReplenishmentResultDTO.SalesHistoryDTO> listedSalesHistory = listSalesHistory(suggestionIds, salesQtyType, orderType, caleStartDate, caleEndDate);
        List<ReplenishmentResultDTO.InventoryHistoryDTO> historyInventoryList = historyInventoryEsService.listByReplenishmentIdsAndDate(suggestionIds, caleStartDate, caleEndDate);
        Map<String, Map<String, Integer>> shopSalesHistoryMap = getSalesHistoryMap(salesQtyType, orderType);
        return entities.parallelStream()
                .map(v -> {
                    ReplenishmentResultDTO resultDTO = new ReplenishmentResultDTO();
                    ReplenishmentResultDTO.BasicDTO basicDTO = ReplenishmentResultDTO.BasicDTO.buildBasicDTO(v);
                    resultDTO.setReplenishment(basicDTO);
                    ReplenishmentSuggestionDetailEntity detail = detailList.stream()
                            .filter(e -> e.getMainId().equals(v.getId()))
                            .findFirst()
                            .orElse(null);
                    if (ObjectUtils.isEmpty(detail)) {
                        return null;
                    }
                    Map<LocalDate, Integer> salesHistoryDTOList = listedSalesHistory.stream()
                            .filter(e -> e.getReplenishmentId().equals(v.getId()))
                            .collect(Collectors.toMap(ReplenishmentResultDTO.SalesHistoryDTO::getDate, ReplenishmentResultDTO.SalesHistoryDTO::getOriginalSalesQty, Integer::sum));
                    Map<LocalDate, Integer> historyInventory = historyInventoryList.stream()
                            .filter(e -> e.getReplenishmentId().equals(v.getId()))
                            .collect(Collectors.toMap(ReplenishmentResultDTO.InventoryHistoryDTO::getDate, ReplenishmentResultDTO.InventoryHistoryDTO::getOriginalInventQty, Integer::sum));
                    ReplenishmentResultDTO.DetailDTO detailDTO = ReplenishmentResultDTO.DetailDTO.buildDetail(detail);
                    resultDTO.setReplenishmentDetail(detailDTO);
                    resultDTO.setHistorySalesList(salesHistoryDTOList);
                    resultDTO.setHistoryInventoryList(historyInventory);
                    resultDTO.setSalesPrice(detail.getSalesPrice());
                    resultDTO.setPurchasePrice(detail.getPurchasePrice());
                    resultDTO.setShopSalesMap(shopSalesHistoryMap.get(v.getSkuId()));
                    return resultDTO;
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取sku对应店铺的销量
     * @param salesQtyType 销量类型
     * @param orderType    订单类型
     */
    @Override
    public Map<String, Map<String, Integer>> getSalesHistoryMap(String salesQtyType, String orderType) {
        Map<String, Integer> salesMap;
        if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(salesQtyType)) {
            salesMap = orderHistorySalesEsService.listByType(orderType);
        } else {
            // 以销售出库单出库时间计算销量
            salesMap = outStockHistorySalesEsService.listByType(orderType);
        }
        if (org.springframework.util.CollectionUtils.isEmpty(salesMap)) {
            return Collections.emptyMap();
        }
        //根据建议id查询建议明细
        List<ReplenishmentSuggestionEntity> suggestion = listReplenishmentSuggestion(new ArrayList<>(salesMap.keySet()));
        return suggestion.stream()
                .collect(Collectors.groupingBy(
                        ReplenishmentSuggestionEntity::getSkuId,
                        Collectors.toMap(
                                ReplenishmentSuggestionEntity::getShopId,
                                entity -> salesMap.getOrDefault(entity.getId(), 0)
                        )
                ));
    }

    @Override
    public PagingVO<CfgRuleCalcDTO.HistorySaleDTO> exportCalcHistorySale(PagingDTO<CfgRuleCalcDTO.DownloadDTO> dto) {
        CfgRuleCalcDTO.DownloadDTO params = dto.getParams();
        LocalDate startDate = params.getStartCalcDate().minusDays(361);
        LocalDate endDate = params.getStartCalcDate().minusDays(1);
        org.springframework.data.domain.Page<OrderHistorySalesEsEntity> historySales = orderHistorySalesEsService.findByShopIdInAndSkuIdInAndDateBetween(params.getShopIds(), params.getSkuIds(), startDate, endDate,
                PageRequest.of(dto.getCurrPage(), dto.getPageSize()));
        if (ObjectUtil.isEmpty(historySales.toList())) {
            return new PagingVO<>();
        }
        List<String> shopIds = historySales.stream().map(OrderHistorySalesEsEntity::getShopId).distinct().collect(Collectors.toList());
        List<String> skuIds = historySales.stream().map(OrderHistorySalesEsEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfo = shopInfoFeign.listShopInfoByIds(shopIds);
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, String> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuNo, (o1, o2) -> o1));

        List<CfgRuleCalcDTO.HistorySaleDTO> list = new ArrayList<>();
        for (OrderHistorySalesEsEntity entity : historySales.toList()) {
            ShopInfoEntity info = shopInfo.stream()
                    .filter(v -> v.getId().equals(entity.getShopId()))
                    .findFirst()
                    .orElse(new ShopInfoEntity());
            CfgRuleCalcDTO.HistorySaleDTO saleDTO = new CfgRuleCalcDTO.HistorySaleDTO();
            saleDTO.setSkuId(entity.getSkuId());
            saleDTO.setSkuNo(skuMap.get(entity.getSkuId()));
            saleDTO.setShopId(entity.getShopId());
            saleDTO.setShopName(info.getName());
            saleDTO.setQty(entity.getOriginalSalesQty());
            saleDTO.setPlatform(info.getDictPlatform());
            saleDTO.setBillDate(entity.getDate());
            list.add(saleDTO);
        }
        return new PagingVO<>(list, (int) historySales.getTotalElements(), dto.getPageSize(), dto.getCurrPage());
    }

    /**
     * 异步获取主表数据
     * @param suggestionIds 建议id
     */
    private List<ReplenishmentSuggestionEntity> listReplenishmentSuggestion(List<String> suggestionIds) {
        List<List<String>> partition = Lists.partition(suggestionIds, 1000);
        return partition.stream()
                .map(suggestionIdList -> CompletableFuture.supplyAsync(
                        () -> listByIds(suggestionIdList),
                        threadPoolTaskExecutor))
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)  // 等待每个 CompletableFuture 完成
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 异步获取详情数据
     * @param suggestionIds 建议id
     * @return
     */
    private List<ReplenishmentSuggestionDetailEntity> getReplenishmentSuggestionDetailEntities(List<String> suggestionIds) {
        List<List<String>> partition = Lists.partition(suggestionIds, 1000);
        return partition.stream()
                .map(suggestionIdList -> CompletableFuture.supplyAsync(
                        () -> replenishmentSuggestionDetailService.listByMainIdList(suggestionIdList),
                        threadPoolTaskExecutor))
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)  // 等待每个 CompletableFuture 完成
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReplenishmentResultDTO.SalesHistoryDTO> listSalesHistory(List<String> replenishmentIds, String salesQtyType, String orderType, LocalDate startDate, LocalDate endDate) {
        List<List<String>> partition = Lists.partition(replenishmentIds, 1000);
        return partition.stream()
                .map(suggestionIdList -> CompletableFuture.supplyAsync(() -> {
                    if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(salesQtyType)) {
                        return orderHistorySalesEsService.listByReplenishmentIdsAndDate(suggestionIdList, orderType, startDate, endDate);
                    } else {
                        // 以销售出库单出库时间计算销量
                        return outStockHistorySalesEsService.listByReplenishmentIdsAndDate(suggestionIdList, orderType, startDate, endDate);
                    }
                }, threadPoolTaskExecutor)).collect(Collectors.toList()).stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Map<LocalDate, Integer> listSalesHistoryMap(String replenishmentId, String salesQtyType, String orderType, LocalDate startDate, LocalDate endDate) {
        List<ReplenishmentResultDTO.SalesHistoryDTO> salesHistory;
        if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(salesQtyType)) {
            salesHistory = orderHistorySalesEsService.listByReplenishmentIdsAndDate(Collections.singletonList(replenishmentId), orderType, startDate, endDate);
        } else {
            // 以销售出库单出库时间计算销量
            salesHistory = outStockHistorySalesEsService.listByReplenishmentIdsAndDate(Collections.singletonList(replenishmentId), orderType, startDate, endDate);
        }
        return salesHistory.stream()
                .collect(Collectors.toMap(ReplenishmentResultDTO.SalesHistoryDTO::getDate, ReplenishmentResultDTO.SalesHistoryDTO::getOriginalSalesQty, Integer::sum));
    }

    @Override
    public EstimationResultDTO inventoryEstimation(InventoryEstimationDTO dto) {

        EstimationResultDTO resultDTO = new EstimationResultDTO();
        Integer days = TimePeriodEstimateEnum.of(dto.getTimePeriodEstimate()).getDays();
        ReplenishmentSuggestionDetailEntity detail = replenishmentSuggestionDetailService.getById(dto.getDetailId());
        ReplenishmentSuggestionEntity suggestion = getById(detail.getMainId());
        //获取销量预估
        List<SalesEstimateEntity> salesEstimateList = salesEstimateService.listByReplenishmentIdAndDay(dto.getDetailId(), LocalDate.now().plusDays(days));
        Map<LocalDate, BigDecimal> salesEstimateMap = salesEstimateList.stream()
                .collect(Collectors.toMap(SalesEstimateEntity::getDate, SalesEstimateEntity::getSalesQty, (o1, o2) -> o1));
        CfgRulePlatformTypeEnum platformType = CfgRulePlatformTypeEnum.getEnum(suggestion.getPlatformType());
        if (ObjectUtils.isEmpty(platformType)) {
            throw new ServiceException(ApiError.ERROR_9028);
        }

        switch (platformType) {
            case AMAZON:
                resultDTO = handlerFbaEstimate(detail, salesEstimateMap, days, dto);
                break;
            case OVERSEAS:
                resultDTO = handlerOverseasEstimate(detail, salesEstimateMap, days, dto);
                break;
            case B2B:
            case INTERNAL:
                resultDTO = handlerLocalEstimate(detail, salesEstimateMap, days, dto);
                break;
            default:
        }
        return resultDTO;
    }

    private EstimationResultDTO handlerLocalEstimate(ReplenishmentSuggestionDetailEntity detail, Map<LocalDate, BigDecimal> salesEstimateMap,
                                                     Integer days, InventoryEstimationDTO dto) {
        EstimationResultDTO resultDTO = new EstimationResultDTO();
        List<LocalDate> date = new ArrayList<>();
        List<BigDecimal> inventoryQty = new ArrayList<>();
        List<BigDecimal> calcInventoryQty = new ArrayList<>();
        List<LocalDate> planArrivalDate = new ArrayList<>();
        List<LocalDate> outOfStockDate = new ArrayList<>();
        List<LocalDate> calcOutOfStockDate = new ArrayList<>();
        Set<LocalDate> calcPlanArrivalDate = new HashSet<>();
        LocalDate now = LocalDate.now();
        // 获取本地在途
        List<LocalInTransitDetailEntity> localInTransitDetails = localInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localInTransitDetailMap = localInTransitDetails.stream()
                .collect(Collectors.toMap(LocalInTransitDetailEntity::getEstimateSalesDate, LocalInTransitDetailEntity::getQty, Integer::sum));
        // 获取本地采购
        List<EstimatedPurchaseDetailEntity> localEstimatedPurchase = estimatedPurchaseDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localEstimatedPurchaseMap = localEstimatedPurchase.stream()
                .collect(Collectors.toMap(EstimatedPurchaseDetailEntity::getEstimateSalesDate, EstimatedPurchaseDetailEntity::getQty, Integer::sum));
        // 获取首日结余库存
        BigDecimal balanceInventory = new BigDecimal(detail.getLocalUsableQty());
        BigDecimal calcBalanceInventory = new BigDecimal(detail.getLocalUsableQty());
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = now.plusDays(i);
            date.add(currentDate);
            BigDecimal temp = balanceInventory;
            BigDecimal salesEstimate = Optional.ofNullable(salesEstimateMap.get(currentDate)).orElse(BigDecimal.ZERO);
            //到货数量
            int planArrivalQty = Optional.ofNullable(localInTransitDetailMap.get(currentDate)).orElse(0) +
                    Optional.ofNullable(localEstimatedPurchaseMap.get(currentDate)).orElse(0);
            if (0 != planArrivalQty) {
                planArrivalDate.add(currentDate);
            }
            //库存等于结余库存-预计销量+到货数量
            balanceInventory = balanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty));
            inventoryQty.add(balanceInventory);
            if (temp.compareTo(BigDecimal.ZERO) > 0 && balanceInventory.compareTo(BigDecimal.ZERO) <= 0) {
                outOfStockDate.add(currentDate);
            }
            if (Boolean.TRUE.equals(dto.getIsSimulated())) {
                if (0 != planArrivalQty) {
                    calcPlanArrivalDate.add(currentDate);
                }
                BigDecimal calcTemp = calcBalanceInventory;
                //计算试算的建议库存
                BigDecimal suggestInventory = getSuggestInventory(currentDate, dto.getDeliverySuggest(), dto.getPurchaseSuggest());
                if (suggestInventory.compareTo(BigDecimal.ZERO) > 0) {
                    calcPlanArrivalDate.add(currentDate);
                }
                calcBalanceInventory = calcBalanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty)).add(suggestInventory);
                calcInventoryQty.add(calcBalanceInventory);
                if (calcTemp.compareTo(BigDecimal.ZERO) > 0 && calcBalanceInventory.compareTo(BigDecimal.ZERO) <= 0) {
                    calcOutOfStockDate.add(currentDate);
                }
            }
        }
        resultDTO.setDate(date);
        resultDTO.setInventoryQty(inventoryQty);
        resultDTO.setPlanArrivalDate(planArrivalDate);
        resultDTO.setOutOfStockDate(outOfStockDate);
        resultDTO.setCalcInventoryQty(calcInventoryQty);
        resultDTO.setCalcOutOfStockDate(calcOutOfStockDate);
        resultDTO.setCalcPlanArrivalDate(new ArrayList<>(calcPlanArrivalDate));
        return resultDTO;
    }

    private EstimationResultDTO handlerOverseasEstimate(ReplenishmentSuggestionDetailEntity detail, Map<LocalDate, BigDecimal> salesEstimateMap,
                                                        Integer days, InventoryEstimationDTO dto) {
        EstimationResultDTO resultDTO = new EstimationResultDTO();
        List<LocalDate> date = new ArrayList<>();
        List<BigDecimal> inventoryQty = new ArrayList<>();
        List<BigDecimal> calcInventoryQty = new ArrayList<>();
        List<LocalDate> planArrivalDate = new ArrayList<>();
        List<LocalDate> outOfStockDate = new ArrayList<>();
        List<LocalDate> calcOutOfStockDate = new ArrayList<>();
        Set<LocalDate> calcPlanArrivalDate = new HashSet<>();
        LocalDate now = LocalDate.now();
        // 获取海外仓在途
        List<OverseasInTransitDetailEntity> overseasInTransitDetails = overseasInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> overseasInTransitDetailMap = overseasInTransitDetails.stream()
                .collect(Collectors.toMap(OverseasInTransitDetailEntity::getEstimateSalesDate, OverseasInTransitDetailEntity::getInTransitQty, Integer::sum));
        // 获取海外仓预计发货
        List<EstimatedDeliveryDetailEntity> overseasEstimatedDelivery = estimatedDeliveryDetailService.getByReplenishmentIdAndType(detail.getId(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY);
        Map<LocalDate, Integer> overseasEstimatedDeliveryMap = overseasEstimatedDelivery.stream()
                .collect(Collectors.toMap(EstimatedDeliveryDetailEntity::getEstimateSalesDate, EstimatedDeliveryDetailEntity::getQty, Integer::sum));
        // 获取本地在途
        List<LocalInTransitDetailEntity> localInTransitDetails = localInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localInTransitDetailMap = localInTransitDetails.stream()
                .collect(Collectors.toMap(LocalInTransitDetailEntity::getEstimateSalesDate, LocalInTransitDetailEntity::getQty, Integer::sum));
        // 获取本地采购
        List<EstimatedPurchaseDetailEntity> localEstimatedPurchase = estimatedPurchaseDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localEstimatedPurchaseMap = localEstimatedPurchase.stream()
                .collect(Collectors.toMap(EstimatedPurchaseDetailEntity::getEstimateSalesDate, EstimatedPurchaseDetailEntity::getQty, Integer::sum));
        // 获取首日结余库存
        BigDecimal balanceInventory = new BigDecimal(detail.getOverseasUsableQty());
        BigDecimal calcBalanceInventory = new BigDecimal(detail.getOverseasUsableQty());
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = now.plusDays(i);
            date.add(currentDate);
            BigDecimal temp = balanceInventory;
            BigDecimal salesEstimate = Optional.ofNullable(salesEstimateMap.get(currentDate)).orElse(BigDecimal.ZERO);
            //到货数量
            int planArrivalQty = Optional.ofNullable(overseasInTransitDetailMap.get(currentDate)).orElse(0) +
                    Optional.ofNullable(overseasEstimatedDeliveryMap.get(currentDate)).orElse(0) +
                    Optional.ofNullable(localInTransitDetailMap.get(currentDate)).orElse(0) +
                    Optional.ofNullable(localEstimatedPurchaseMap.get(currentDate)).orElse(0);
            if (0 != planArrivalQty) {
                planArrivalDate.add(currentDate);
            }
            //库存等于结余库存-预计销量+到货数量
            balanceInventory = balanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty));
            inventoryQty.add(balanceInventory);
            if (temp.compareTo(BigDecimal.ZERO) > 0 && balanceInventory.compareTo(BigDecimal.ZERO) <= 0) {
                outOfStockDate.add(currentDate);
            }
            if (Boolean.TRUE.equals(dto.getIsSimulated())) {
                if (0 != planArrivalQty) {
                    calcPlanArrivalDate.add(currentDate);
                }
                BigDecimal calcTemp = calcBalanceInventory;
                //计算试算的建议库存
                BigDecimal suggestInventory = getSuggestInventory(currentDate, dto.getDeliverySuggest(), dto.getPurchaseSuggest());
                if (suggestInventory.compareTo(BigDecimal.ZERO) > 0) {
                    calcPlanArrivalDate.add(currentDate);
                }
                calcBalanceInventory = calcBalanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty)).add(suggestInventory);
                calcInventoryQty.add(calcBalanceInventory);
                if (calcTemp.compareTo(BigDecimal.ZERO) > 0 && calcBalanceInventory.compareTo(BigDecimal.ZERO) <= 0) {
                    calcOutOfStockDate.add(currentDate);
                }
            }
        }
        resultDTO.setDate(date);
        resultDTO.setInventoryQty(inventoryQty);
        resultDTO.setPlanArrivalDate(planArrivalDate);
        resultDTO.setOutOfStockDate(outOfStockDate);
        resultDTO.setCalcInventoryQty(calcInventoryQty);
        resultDTO.setCalcOutOfStockDate(calcOutOfStockDate);
        resultDTO.setCalcPlanArrivalDate(new ArrayList<>(calcPlanArrivalDate));
        return resultDTO;
    }

    private EstimationResultDTO handlerFbaEstimate(ReplenishmentSuggestionDetailEntity detail, Map<LocalDate, BigDecimal> salesEstimateMap,
                                                   Integer days, InventoryEstimationDTO dto) {
        EstimationResultDTO resultDTO = new EstimationResultDTO();
        List<LocalDate> date = new ArrayList<>();
        List<BigDecimal> inventoryQty = new ArrayList<>();
        List<BigDecimal> calcInventoryQty = new ArrayList<>();
        List<LocalDate> planArrivalDate = new ArrayList<>();
        List<LocalDate> outOfStockDate = new ArrayList<>();
        List<LocalDate> calcOutOfStockDate = new ArrayList<>();
        Set<LocalDate> calcPlanArrivalDate = new HashSet<>();
        LocalDate now = LocalDate.now();
        // 获取Fba在途
        List<FbaInTransitDetailEntity> fbaInTransitDetails = fbaInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> fbaInTransitDetailMap = fbaInTransitDetails.stream()
                .collect(Collectors.toMap(FbaInTransitDetailEntity::getEstimateSalesDate, FbaInTransitDetailEntity::getInTransitQty, Integer::sum));
        // 获取Fba预计发货
        List<EstimatedDeliveryDetailEntity> fbaEstimatedDelivery = estimatedDeliveryDetailService.getByReplenishmentIdAndType(detail.getId(), ReplenishmentInventoryTypeEnum.FBA_ESTIMATED_DELIVERY);
        Map<LocalDate, Integer> fbaEstimatedDeliveryMap = fbaEstimatedDelivery.stream()
                .collect(Collectors.toMap(EstimatedDeliveryDetailEntity::getEstimateSalesDate, EstimatedDeliveryDetailEntity::getQty, Integer::sum));
        // 获取海外仓在途
        List<OverseasInTransitDetailEntity> overseasInTransitDetails = overseasInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> overseasInTransitDetailMap = overseasInTransitDetails.stream()
                .collect(Collectors.toMap(OverseasInTransitDetailEntity::getEstimateSalesDate, OverseasInTransitDetailEntity::getInTransitQty, Integer::sum));
        // 获取海外仓预计发货
        List<EstimatedDeliveryDetailEntity> overseasEstimatedDelivery = estimatedDeliveryDetailService.getByReplenishmentIdAndType(detail.getId(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY);
        Map<LocalDate, Integer> overseasEstimatedDeliveryMap = overseasEstimatedDelivery.stream()
                .collect(Collectors.toMap(EstimatedDeliveryDetailEntity::getEstimateSalesDate, EstimatedDeliveryDetailEntity::getQty, Integer::sum));
        // 获取本地在途
        List<LocalInTransitDetailEntity> localInTransitDetails = localInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localInTransitDetailMap = localInTransitDetails.stream()
                .collect(Collectors.toMap(LocalInTransitDetailEntity::getEstimateSalesDate, LocalInTransitDetailEntity::getQty, Integer::sum));
        // 获取本地采购
        List<EstimatedPurchaseDetailEntity> localEstimatedPurchase = estimatedPurchaseDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localEstimatedPurchaseMap = localEstimatedPurchase.stream()
                .collect(Collectors.toMap(EstimatedPurchaseDetailEntity::getEstimateSalesDate, EstimatedPurchaseDetailEntity::getQty, Integer::sum));
        // 获取首日结余库存
        BigDecimal balanceInventory = new BigDecimal(detail.getFbaUsableQty());
        BigDecimal calcBalanceInventory = new BigDecimal(detail.getFbaUsableQty());
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = now.plusDays(i);
            date.add(currentDate);
            BigDecimal temp = balanceInventory;
            BigDecimal salesEstimate = Optional.ofNullable(salesEstimateMap.get(currentDate)).orElse(BigDecimal.ZERO);
            //到货数量
            int planArrivalQty = Optional.ofNullable(fbaInTransitDetailMap.get(currentDate)).orElse(0) +
                    Optional.ofNullable(fbaEstimatedDeliveryMap.get(currentDate)).orElse(0) +
                    Optional.ofNullable(overseasInTransitDetailMap.get(currentDate)).orElse(0) +
                    Optional.ofNullable(overseasEstimatedDeliveryMap.get(currentDate)).orElse(0) +
                    Optional.ofNullable(localInTransitDetailMap.get(currentDate)).orElse(0) +
                    Optional.ofNullable(localEstimatedPurchaseMap.get(currentDate)).orElse(0);
            if (0 != planArrivalQty) {
                planArrivalDate.add(currentDate);
            }
            //库存等于结余库存-预计销量+到货数量
            balanceInventory = balanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty));
            inventoryQty.add(balanceInventory);
            if (temp.compareTo(BigDecimal.ZERO) > 0 && balanceInventory.compareTo(BigDecimal.ZERO) <= 0) {
                outOfStockDate.add(currentDate);
            }
            if (Boolean.TRUE.equals(dto.getIsSimulated())) {
                if (0 != planArrivalQty) {
                    calcPlanArrivalDate.add(currentDate);
                }
                BigDecimal calcTemp = calcBalanceInventory;
                //计算试算的建议库存
                BigDecimal suggestInventory = getSuggestInventory(currentDate, dto.getDeliverySuggest(), dto.getPurchaseSuggest());
                if (suggestInventory.compareTo(BigDecimal.ZERO) > 0) {
                    calcPlanArrivalDate.add(currentDate);
                }
                calcBalanceInventory = calcBalanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty)).add(suggestInventory);
                calcInventoryQty.add(calcBalanceInventory);
                if (calcTemp.compareTo(BigDecimal.ZERO) > 0 && calcBalanceInventory.compareTo(BigDecimal.ZERO) <= 0) {
                    calcOutOfStockDate.add(currentDate);
                }
            }
        }
        resultDTO.setDate(date);
        resultDTO.setInventoryQty(inventoryQty);
        resultDTO.setPlanArrivalDate(planArrivalDate);
        resultDTO.setOutOfStockDate(outOfStockDate);
        resultDTO.setCalcInventoryQty(calcInventoryQty);
        resultDTO.setCalcOutOfStockDate(calcOutOfStockDate);
        resultDTO.setCalcPlanArrivalDate(new ArrayList<>(calcPlanArrivalDate));
        return resultDTO;
    }

    /**
     * 计算试算的建议库存
     * @param currentDate 当前日期
     */
    private BigDecimal  getSuggestInventory(LocalDate currentDate,
                                           List<DeliverySuggestDTO.ListDTO> calcDeliverySuggestList, List<PurchaseSuggestDTO.ListDTO> calcPurchaseSuggestList) {
        BigDecimal suggestInventory = BigDecimal.ZERO;
        int calcDeliverySuggests = calcDeliverySuggestList.stream()
                .filter(v -> v.getEstimateSalesDate().equals(currentDate))
                .map(DeliverySuggestDTO.ListDTO::getSuggestDeliveryQty)
                .reduce(0, Math::addExact);
        int calcPurchaseSuggests = calcPurchaseSuggestList.stream()
                .filter(v -> v.getEstimateSalesDate().equals(currentDate))
                .map(PurchaseSuggestDTO.ListDTO::getSuggestPurchaseQty)
                .reduce(0, Math::addExact);
        return suggestInventory.add(new BigDecimal(calcDeliverySuggests)).add(new BigDecimal(calcPurchaseSuggests));
    }


    @Override
    public EstimationDetailResultDTO inventoryEstimationDetail(InventoryEstimationDetailDTO dto) {

        EstimationDetailResultDTO resultDTO = new EstimationDetailResultDTO();
        ReplenishmentSuggestionDetailEntity detail = replenishmentSuggestionDetailService.getById(dto.getDetailId());
        ReplenishmentSuggestionEntity suggestion = getById(detail.getMainId());
        //获取销量预估
        List<SalesEstimateEntity> salesEstimateList = salesEstimateService.listByReplenishmentIdAndDay(dto.getDetailId(), dto.getDate());
        Map<LocalDate, BigDecimal> salesEstimateMap = salesEstimateList.stream()
                .collect(Collectors.toMap(SalesEstimateEntity::getDate, SalesEstimateEntity::getSalesQty, (o1, o2) -> o1));
        CfgRulePlatformTypeEnum platformType = CfgRulePlatformTypeEnum.getEnum(suggestion.getPlatformType());
        if (ObjectUtils.isEmpty(platformType)) {
            throw new ServiceException(ApiError.ERROR_9028);
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), dto.getDate()) + 1;
        switch (platformType) {
            case AMAZON:
                resultDTO = handlerFbaEstimateDetail(detail, salesEstimateMap, dto, days);
                break;
            case OVERSEAS:
                resultDTO = handlerOverseasEstimateDetail(detail, salesEstimateMap, dto, days);
                break;
            case B2B:
            case INTERNAL:
                resultDTO = handlerLocalEstimateDetail(detail, salesEstimateMap, dto, days);
                break;
            default:
        }
        return resultDTO;
    }

    @Override
    public Integer inventoryTotal(InventoryTotalDTO params) {
        ReplenishmentInventoryTypeEnum inventoryType = ReplenishmentInventoryTypeEnum.of(params.getType());
        int totalQty = 0;
        switch (inventoryType) {
            case FBA_IN_TRANSIT:
                totalQty = fbaInTransitDetailService.totalQtyByReplenishment(params.getDetailId());
                break;
            case FBA_ESTIMATED_DELIVERY:
                totalQty = estimatedDeliveryDetailService.totalQtyByReplenishment(params.getDetailId(), ReplenishmentInventoryTypeEnum.FBA_ESTIMATED_DELIVERY);
                break;
            case OVERSEAS_IN_TRANSIT:
                totalQty = overseasInTransitDetailService.totalQtyByReplenishment(params.getDetailId());
                break;
            case OVERSEAS_ESTIMATED_DELIVERY:
                totalQty = estimatedDeliveryDetailService.totalQtyByReplenishment(params.getDetailId(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY);
                break;
            case LOCAL_IN_TRANSIT:
                totalQty = localInTransitDetailService.totalQtyByReplenishment(params.getDetailId());
                break;
            case LOCAL_ESTIMATED_DELIVERY:
                totalQty = estimatedPurchaseDetailService.totalQtyByReplenishment(params.getDetailId());
                break;
            default:
        }
        return totalQty;
    }

    @Override
    public BatchResultDTO renewData(String id) {
        return replenishmentDataService.renewData(id);
    }

    @Override
    public SalesAnalysisVO mockSalesAnalysis(MockSalesAnalysisDTO dto) {

        ReplenishmentSuggestionDetailEntity detail = replenishmentSuggestionDetailService.getById(dto.getDetailId());
        CfgRuleStrategyDTO cfgRuleStrategyDTO = JSON.parseObject(detail.getCfgRule(), CfgRuleStrategyDTO.class);
        CfgRuleSalesQtyDTO.StrategyResultDTO salesQtyResult = cfgRuleStrategyDTO.getSalesQtyResult();
        ReplenishmentSuggestionEntity suggestion = getById(detail.getMainId());
        CfgRuleSalesQtyEntity cfgRuleSalesQty = cfgRuleSalesQtyService.getDefaultByPlatformAndSkuType(suggestion.getPlatformType(), detail.getSkuType());
        List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> defaultDenoisingResults = cfgRuleSalesDenoisingService.listDenoisingBySalesId(cfgRuleSalesQty.getId());
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults = cfgRuleSalesFormulaService.listFormulaBySalesId(cfgRuleSalesQty.getId());
        List<LocalDate> dates = new ArrayList<>();
        LocalDate startDate = dto.getStartDate().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(TimePeriodEstimateEnum.of(dto.getTimePeriod()).getDays() - 1L);
        while (startDate.isBefore(endDate)) {
            dates.add(startDate);
            startDate = startDate.plusDays(1);
        }
        LocalDate caleStartDate = LocalDate.now().minusDays(361);
        LocalDate caleEndDate = LocalDate.now().minusDays(1);
        Map<LocalDate, Integer> salesHistoryMap = listSalesHistoryMap(detail.getMainId(), salesQtyResult.getSalesQtyType(), salesQtyResult.getOrderType(), caleStartDate, caleEndDate);
        Map<LocalDate, Integer> historyInventoryMap = historyInventoryEsService.findByReplenishmentIdAndDateBetweenMap(detail.getMainId(), caleStartDate, caleEndDate);
        //模拟计算去噪销量
        List<ReplenishmentResultDTO.SalesInfoDTO> list = calculationSales(cfgRuleSalesQty.getIsIgnoreOutOfStock(), salesHistoryMap,
                historyInventoryMap, dto.getSalesQtyUpdateDTO().getSalesDenoisingList(), defaultDenoisingResults, caleStartDate, caleEndDate);
        List<ReplenishmentResultDTO.TimePeriodSalesDTO> avgTimePeriodSales = calculationTimePeriodSales(list, salesHistoryMap);
        List<ReplenishmentResultDTO.SalesEstimateDTO> estimateEntityList = calculationSaleEstimate(TimePeriodEstimateEnum.of(dto.getTimePeriod()).getDays() - 1,
                list, avgTimePeriodSales, dto.getSalesQtyUpdateDTO(), defaultFormulaResults);
        SalesAnalysisVO salesAnalysisVO = new SalesAnalysisVO();
        List<BigDecimal> originalSales = new ArrayList<>();
        List<BigDecimal> sales = new ArrayList<>();
        List<BigDecimal> salesEstimates = new ArrayList<>();
        for (LocalDate date : dates) {
            ReplenishmentResultDTO.SalesInfoDTO salesInfo = list.stream()
                    .filter(v -> v.getDate().equals(date))
                    .findFirst()
                    .orElse(new ReplenishmentResultDTO.SalesInfoDTO());
            int saleQty = Optional.ofNullable(salesHistoryMap.get(date)).orElse(0);
            if (!date.isBefore(startDate) && !date.isAfter(dto.getEndDate())) {
                originalSales.add(new BigDecimal(saleQty));
            } else {
                originalSales.add(null);
            }
            sales.add(salesInfo.getSalesQty());
            ReplenishmentResultDTO.SalesEstimateDTO estimate = estimateEntityList.stream()
                    .filter(v -> v.getDate().equals(date))
                    .findFirst()
                    .orElse(new ReplenishmentResultDTO.SalesEstimateDTO());
            salesEstimates.add(estimate.getSalesQty());
        }
        salesAnalysisVO.setDate(dates);
        salesAnalysisVO.setDenoisingSales(sales);
        salesAnalysisVO.setHistorySales(originalSales);
        salesAnalysisVO.setEstimatesSales(salesEstimates);
        return salesAnalysisVO;
    }


    /**
     * 模拟计算分时段销量
     *
     * @param list            去噪销量
     * @param salesHistoryMap 历史销量
     */
    private List<ReplenishmentResultDTO.TimePeriodSalesDTO> calculationTimePeriodSales(List<ReplenishmentResultDTO.SalesInfoDTO> list, Map<LocalDate, Integer> salesHistoryMap) {
        //分时段日均销量
        List<ReplenishmentResultDTO.TimePeriodSalesDTO> avgTimePeriodSales = new ArrayList<>();
        LocalDate endDate = LocalDate.now().minusDays(1);
        for (TimePeriodEnum value : TimePeriodEnum.values()) {
            BigDecimal qty = list.stream()
                    .filter(v -> !endDate.minusDays(value.getDays()).isAfter(v.getDate()) && endDate.isAfter(v.getDate()))
                    .map(ReplenishmentResultDTO.SalesInfoDTO::getSalesQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(0, RoundingMode.CEILING);
            long count = list.stream()
                    .filter(v -> !endDate.minusDays(value.getDays()).isAfter(v.getDate()) && endDate.isAfter(v.getDate()))
                    .filter(v -> Boolean.FALSE.equals(v.getIsIgnoreOutOfStock()))
                    .filter(v -> !COMPLETELY.getCode().equals(v.getDenoisingType()))
                    .count();
            BigDecimal avgQty = new BigDecimal(0);
            if (count != 0) {
                avgQty = qty.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            }
            avgTimePeriodSales.add(new ReplenishmentResultDTO.TimePeriodSalesDTO(value, avgQty));
        }
        return avgTimePeriodSales;
    }

    private List<ReplenishmentResultDTO.SalesEstimateDTO> calculationSaleEstimate(int days,
                                                                                  List<ReplenishmentResultDTO.SalesInfoDTO> salesInfos, List<ReplenishmentResultDTO.TimePeriodSalesDTO> avgTimePeriodSales,
                                                                                  CfgRuleSalesQtyDTO.UpdateDetailDTO updateDetail, List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults) {
        List<ReplenishmentResultDTO.SalesEstimateDTO> salesEstimates = new ArrayList<>();
        LocalDate basicCalcDate = LocalDate.now();
        for (int i = 0; i < days; i++) {
            LocalDate calcDate = basicCalcDate.plusDays(i);
            //获取最大优先级的规则 优先取 sku 固定规则，其次sku动态规则，其次sku默认规则，取不到则取系统动态规则，其次系统默认规则
            CfgRuleSalesQtyDTO.StrategyFormulaResultDTO formulaResult = updateDetail.getFixedSalesQtyList().stream()
                    .filter(v -> !v.getDateList().get(0).isAfter(calcDate) && !v.getDateList().get(1).isBefore(calcDate))
                    .map(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::buildFormulaResultDTO)
                    .reduce((first, second) -> second)
                    .orElse(getSkuDynamic(updateDetail, defaultFormulaResults, calcDate));
            if (ObjectUtils.isEmpty(formulaResult)) {
                continue;
            }
            BigDecimal saleQty = salesEstimateHandler.getSaleQty(salesInfos, avgTimePeriodSales, formulaResult, basicCalcDate);
            ReplenishmentResultDTO.SalesEstimateDTO salesEstimateDTO = new ReplenishmentResultDTO.SalesEstimateDTO(calcDate, saleQty,
                    calcDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            salesEstimates.add(salesEstimateDTO);
        }
        return salesEstimates;
    }

    /**
     * 获取sku 动态规则
     * @param updateDetail 入参
     * @param defaultFormulaResults 系统销量规则
     * @param calcDate 当前计算日
     */
    private static CfgRuleSalesQtyDTO.StrategyFormulaResultDTO getSkuDynamic(CfgRuleSalesQtyDTO.UpdateDetailDTO updateDetail, List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults, LocalDate calcDate) {
        return updateDetail.getDynamicSalesQtyList().stream()
                .filter(v -> !v.getDateList().get(0).isAfter(calcDate) && !v.getDateList().get(1).isBefore(calcDate))
                .map(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::buildFormulaResultDTO)
                .reduce((first, second) -> second)
                .orElse(getSkuDefault(updateDetail, defaultFormulaResults, calcDate));
    }

    /**
     * 获取sku默认规则
     * @param updateDetail 入参
     * @param defaultFormulaResults 系统销量规则
     * @param calcDate 当前计算日
     */
    private static CfgRuleSalesQtyDTO.StrategyFormulaResultDTO getSkuDefault(CfgRuleSalesQtyDTO.UpdateDetailDTO updateDetail, List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults, LocalDate calcDate) {
        return Optional.ofNullable(updateDetail.getDefaultSalesQtyDTO()).map(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::buildFormulaResultDTO)
                .orElse(getSysDynamic(defaultFormulaResults, calcDate));
    }

    /**
     * 获取系统动态规则
     * @param defaultFormulaResults 系统销量规则
     * @param calcDate 当前计算日
     */
    private static CfgRuleSalesQtyDTO.StrategyFormulaResultDTO getSysDynamic(List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults, LocalDate calcDate) {
        return defaultFormulaResults.stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getStartDate()) && !ObjectUtils.isEmpty(v.getEndDate()) &&
                        !v.getStartDate().isAfter(calcDate) && !v.getEndDate().isBefore(calcDate))
                .min(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getPriority)
                        .thenComparing(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::getIndex).reversed()))
                .orElse(getSysDefault(defaultFormulaResults));
    }

    /**
     * 获取系统默认规则
     * @param defaultFormulaResults 系统销量规则
     */
    private static CfgRuleSalesQtyDTO.StrategyFormulaResultDTO getSysDefault(List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormulaResults) {
        return defaultFormulaResults.stream()
                .filter(v -> CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(v.getType()))
                .findFirst().orElse(null);
    }


    /**
     * 模拟计算去噪销量
     *
     * @param isIgnoreOutOfStock      是否断货排除
     * @param salesHistoryMap         历史销量
     * @param historyInventoryMap     历史库存
     * @param salesDenoisingList      去噪规则
     * @param defaultDenoisingResults 默认去噪规则
     * @param startDate               计算开始时间
     * @param endDate                 计算结束时间
     */
    private List<ReplenishmentResultDTO.SalesInfoDTO> calculationSales(Boolean isIgnoreOutOfStock, Map<LocalDate, Integer> salesHistoryMap,
                                                                       Map<LocalDate, Integer> historyInventoryMap,
                                                                       List<CfgRuleSalesDenoisingDTO.UpdateDTO> salesDenoisingList,
                                                                       List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> defaultDenoisingResults,
                                                                       LocalDate startDate, LocalDate endDate) {
        List<ReplenishmentResultDTO.SalesInfoDTO> salesInfoDTOS = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            LocalDate date = startDate;
            ReplenishmentResultDTO.SalesInfoDTO salesInfoDTO = new ReplenishmentResultDTO.SalesInfoDTO();
            salesInfoDTO.setId(IdWorker.getIdStr());
            salesInfoDTO.setDate(date);
            //获取符合的最大优先级销量去噪规则 (序号越小优先级越大)
            CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO denoisingResult = salesDenoisingList.stream()
                    .filter(v -> !v.getDateList().get(0).isAfter(date) && !v.getDateList().get(1).isBefore(date))
                    .map(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO::buildStrategyDenoisingResultDTO)
                    .reduce((first, second) -> second)
                    .orElse(defaultDenoisingResults.stream()
                            .filter(v -> !v.getStartDate().isAfter(date) && !v.getEndDate().isBefore(date))
                            .max(Comparator.comparing(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO::getIndex))
                            .orElse(null));
            int originalSalesQty = Optional.ofNullable(salesHistoryMap.get(date)).orElse(0);
            int originalInventQty = Optional.ofNullable(historyInventoryMap.get(date)).orElse(0);
            // 断货排除 ＞ 销量去噪
            if (Boolean.TRUE.equals(isIgnoreOutOfStock) && originalSalesQty == 0 && originalInventQty == 0) {
                //存在真实断货
                salesInfoDTO.setIsIgnoreOutOfStock(true);
                salesInfoDTO.setSalesQty(new BigDecimal(0));
            } else {
                salesInfoDTO.setIsIgnoreOutOfStock(false);
                if (ObjectUtils.isEmpty(denoisingResult)) {
                    salesInfoDTO.setSalesQty(new BigDecimal(originalSalesQty));
                } else {
                    CfgRuleSalesDenoisingDenoisingTypeEnum code = CfgRuleSalesDenoisingDenoisingTypeEnum.getEnumByCode(denoisingResult.getDenoisingType());
                    BigDecimal salesQty = new BigDecimal(0);
                    switch (Objects.requireNonNull(code)) {
                        case PERCENTAGE:
                            salesQty = new BigDecimal(originalSalesQty)
                                    .multiply(BigDecimal.valueOf(denoisingResult.getEffectiveValue()))
                                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                            break;
                        case FIXED_VALUE:
                            salesQty = BigDecimal.valueOf(denoisingResult.getEffectiveValue());
                            break;
                        default:
                            break;
                    }
                    salesInfoDTO.setSalesQty(salesQty);
                    salesInfoDTO.setDenoisingType(denoisingResult.getDenoisingType());
                }
            }
            salesInfoDTOS.add(salesInfoDTO);
            startDate = startDate.plusDays(1);
        }
        return salesInfoDTOS;
    }

    private EstimationDetailResultDTO handlerLocalEstimateDetail(ReplenishmentSuggestionDetailEntity detail, Map<LocalDate, BigDecimal> salesEstimateMap, InventoryEstimationDetailDTO dto, long days) {
        EstimationDetailResultDTO resultDTO = new EstimationDetailResultDTO();
        LocalDate now = LocalDate.now();
        resultDTO.setDate(dto.getDate());
        // 获取本地在途
        List<LocalInTransitDetailEntity> localInTransitDetails = localInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localInTransitDetailMap = localInTransitDetails.stream()
                .collect(Collectors.toMap(LocalInTransitDetailEntity::getEstimateSalesDate, LocalInTransitDetailEntity::getQty, Integer::sum));
        // 获取本地采购
        List<EstimatedPurchaseDetailEntity> localEstimatedPurchase = estimatedPurchaseDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localEstimatedPurchaseMap = localEstimatedPurchase.stream()
                .collect(Collectors.toMap(EstimatedPurchaseDetailEntity::getEstimateSalesDate, EstimatedPurchaseDetailEntity::getQty, Integer::sum));
        int localInTransitQty = 0;
        int localPlanPurchaseQty = 0;
        // 获取首日结余库存
        BigDecimal balanceInventory = new BigDecimal(detail.getFbaUsableQty());
        BigDecimal calcBalanceInventory = new BigDecimal(detail.getFbaUsableQty());
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = now.plusDays(i);
            BigDecimal salesEstimate = Optional.ofNullable(salesEstimateMap.get(currentDate)).orElse(BigDecimal.ZERO);
            resultDTO.setSalesQty(salesEstimate);
            localInTransitQty = Optional.ofNullable(localInTransitDetailMap.get(currentDate)).orElse(0);
            localPlanPurchaseQty = Optional.ofNullable(localEstimatedPurchaseMap.get(currentDate)).orElse(0);
            //到货数量
            int planArrivalQty = localInTransitQty + localPlanPurchaseQty;
            //库存等于结余库存-预计销量+到货数量
            balanceInventory = balanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty));
            resultDTO.setInventoryQty(balanceInventory);
            resultDTO.setPlanArrivalQty(new BigDecimal(planArrivalQty));
            if (Boolean.TRUE.equals(dto.getIsSimulated())) {
                //计算试算的建议库存
                BigDecimal suggestInventory = getSuggestInventory(currentDate, dto.getDeliverySuggest(), dto.getPurchaseSuggest());
                calcBalanceInventory = calcBalanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty)).add(suggestInventory);
                resultDTO.setCalcInventoryQty(calcBalanceInventory);
                resultDTO.setCalcPlanArrivalQty(new BigDecimal(planArrivalQty).add(suggestInventory));
            }
        }
        resultDTO.setLocalInTransitQty(localInTransitQty);
        resultDTO.setLocalPlanPurchaseQty(localPlanPurchaseQty);
        return resultDTO;
    }

    private EstimationDetailResultDTO handlerOverseasEstimateDetail(ReplenishmentSuggestionDetailEntity detail, Map<LocalDate, BigDecimal> salesEstimateMap, InventoryEstimationDetailDTO dto, long days) {
        EstimationDetailResultDTO resultDTO = new EstimationDetailResultDTO();
        LocalDate now = LocalDate.now();
        resultDTO.setDate(dto.getDate());
        // 获取海外仓在途
        List<OverseasInTransitDetailEntity> overseasInTransitDetails = overseasInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> overseasInTransitDetailMap = overseasInTransitDetails.stream()
                .collect(Collectors.toMap(OverseasInTransitDetailEntity::getEstimateSalesDate, OverseasInTransitDetailEntity::getInTransitQty, Integer::sum));
        // 获取海外仓预计发货
        List<EstimatedDeliveryDetailEntity> overseasEstimatedDelivery = estimatedDeliveryDetailService.getByReplenishmentIdAndType(detail.getId(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY);
        Map<LocalDate, Integer> overseasEstimatedDeliveryMap = overseasEstimatedDelivery.stream()
                .collect(Collectors.toMap(EstimatedDeliveryDetailEntity::getEstimateSalesDate, EstimatedDeliveryDetailEntity::getQty, Integer::sum));
        // 获取本地在途
        List<LocalInTransitDetailEntity> localInTransitDetails = localInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localInTransitDetailMap = localInTransitDetails.stream()
                .collect(Collectors.toMap(LocalInTransitDetailEntity::getEstimateSalesDate, LocalInTransitDetailEntity::getQty, Integer::sum));
        // 获取本地采购
        List<EstimatedPurchaseDetailEntity> localEstimatedPurchase = estimatedPurchaseDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localEstimatedPurchaseMap = localEstimatedPurchase.stream()
                .collect(Collectors.toMap(EstimatedPurchaseDetailEntity::getEstimateSalesDate, EstimatedPurchaseDetailEntity::getQty, Integer::sum));
        int overseasInTransitQty = 0;
        int overseasPlanDeliveryQty = 0;
        int localInTransitQty = 0;
        int localPlanPurchaseQty = 0;
        // 获取首日结余库存
        BigDecimal balanceInventory = new BigDecimal(detail.getFbaUsableQty());
        BigDecimal calcBalanceInventory = new BigDecimal(detail.getFbaUsableQty());
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = now.plusDays(i);
            BigDecimal salesEstimate = Optional.ofNullable(salesEstimateMap.get(currentDate)).orElse(BigDecimal.ZERO);
            resultDTO.setSalesQty(salesEstimate);
            overseasInTransitQty = Optional.ofNullable(overseasInTransitDetailMap.get(currentDate)).orElse(0);
            overseasPlanDeliveryQty = Optional.ofNullable(overseasEstimatedDeliveryMap.get(currentDate)).orElse(0);
            localInTransitQty = Optional.ofNullable(localInTransitDetailMap.get(currentDate)).orElse(0);
            localPlanPurchaseQty = Optional.ofNullable(localEstimatedPurchaseMap.get(currentDate)).orElse(0);
            //到货数量
            int planArrivalQty = overseasInTransitQty + overseasPlanDeliveryQty + localInTransitQty + localPlanPurchaseQty;
            //库存等于结余库存-预计销量+到货数量
            balanceInventory = balanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty));
            resultDTO.setInventoryQty(balanceInventory);
            resultDTO.setPlanArrivalQty(new BigDecimal(planArrivalQty));
            if (Boolean.TRUE.equals(dto.getIsSimulated())) {
                //计算试算的建议库存
                BigDecimal suggestInventory = getSuggestInventory(currentDate, dto.getDeliverySuggest(), dto.getPurchaseSuggest());
                calcBalanceInventory = calcBalanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty)).add(suggestInventory);
                resultDTO.setCalcInventoryQty(calcBalanceInventory);
                resultDTO.setCalcPlanArrivalQty(new BigDecimal(planArrivalQty).add(suggestInventory));
            }
        }
        resultDTO.setOverseasInTransitQty(overseasInTransitQty);
        resultDTO.setOverseasPlanDeliveryQty(overseasPlanDeliveryQty);
        resultDTO.setLocalInTransitQty(localInTransitQty);
        resultDTO.setLocalPlanPurchaseQty(localPlanPurchaseQty);
        return resultDTO;
    }

    private EstimationDetailResultDTO handlerFbaEstimateDetail(ReplenishmentSuggestionDetailEntity detail, Map<LocalDate, BigDecimal> salesEstimateMap, InventoryEstimationDetailDTO dto, long days) {
        EstimationDetailResultDTO resultDTO = new EstimationDetailResultDTO();
        LocalDate now = LocalDate.now();
        resultDTO.setDate(dto.getDate());
        // 获取Fba在途
        List<FbaInTransitDetailEntity> fbaInTransitDetails = fbaInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> fbaInTransitDetailMap = fbaInTransitDetails.stream()
                .collect(Collectors.toMap(FbaInTransitDetailEntity::getEstimateSalesDate, FbaInTransitDetailEntity::getInTransitQty, Integer::sum));
        // 获取Fba预计发货
        List<EstimatedDeliveryDetailEntity> fbaEstimatedDelivery = estimatedDeliveryDetailService.getByReplenishmentIdAndType(detail.getId(), ReplenishmentInventoryTypeEnum.FBA_ESTIMATED_DELIVERY);
        Map<LocalDate, Integer> fbaEstimatedDeliveryMap = fbaEstimatedDelivery.stream()
                .collect(Collectors.toMap(EstimatedDeliveryDetailEntity::getEstimateSalesDate, EstimatedDeliveryDetailEntity::getQty, Integer::sum));
        // 获取海外仓在途
        List<OverseasInTransitDetailEntity> overseasInTransitDetails = overseasInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> overseasInTransitDetailMap = overseasInTransitDetails.stream()
                .collect(Collectors.toMap(OverseasInTransitDetailEntity::getEstimateSalesDate, OverseasInTransitDetailEntity::getInTransitQty, Integer::sum));
        // 获取海外仓预计发货
        List<EstimatedDeliveryDetailEntity> overseasEstimatedDelivery = estimatedDeliveryDetailService.getByReplenishmentIdAndType(detail.getId(), ReplenishmentInventoryTypeEnum.OVERSEAS_ESTIMATED_DELIVERY);
        Map<LocalDate, Integer> overseasEstimatedDeliveryMap = overseasEstimatedDelivery.stream()
                .collect(Collectors.toMap(EstimatedDeliveryDetailEntity::getEstimateSalesDate, EstimatedDeliveryDetailEntity::getQty, Integer::sum));
        // 获取本地在途
        List<LocalInTransitDetailEntity> localInTransitDetails = localInTransitDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localInTransitDetailMap = localInTransitDetails.stream()
                .collect(Collectors.toMap(LocalInTransitDetailEntity::getEstimateSalesDate, LocalInTransitDetailEntity::getQty, Integer::sum));
        // 获取本地采购
        List<EstimatedPurchaseDetailEntity> localEstimatedPurchase = estimatedPurchaseDetailService.getByReplenishmentId(detail.getId());
        Map<LocalDate, Integer> localEstimatedPurchaseMap = localEstimatedPurchase.stream()
                .collect(Collectors.toMap(EstimatedPurchaseDetailEntity::getEstimateSalesDate, EstimatedPurchaseDetailEntity::getQty, Integer::sum));
        int fbaInTransitQty = 0;
        int fbaPlanDeliveryQty = 0;
        int overseasInTransitQty = 0;
        int overseasPlanDeliveryQty = 0;
        int localInTransitQty = 0;
        int localPlanPurchaseQty = 0;
        // 获取首日结余库存
        BigDecimal balanceInventory = new BigDecimal(detail.getFbaUsableQty());
        BigDecimal calcBalanceInventory = new BigDecimal(detail.getFbaUsableQty());
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = now.plusDays(i);
            BigDecimal salesEstimate = Optional.ofNullable(salesEstimateMap.get(currentDate)).orElse(BigDecimal.ZERO);
            resultDTO.setSalesQty(salesEstimate);
            fbaInTransitQty = Optional.ofNullable(fbaInTransitDetailMap.get(currentDate)).orElse(0);
            fbaPlanDeliveryQty = Optional.ofNullable(fbaEstimatedDeliveryMap.get(currentDate)).orElse(0);
            overseasInTransitQty = Optional.ofNullable(overseasInTransitDetailMap.get(currentDate)).orElse(0);
            overseasPlanDeliveryQty = Optional.ofNullable(overseasEstimatedDeliveryMap.get(currentDate)).orElse(0);
            localInTransitQty = Optional.ofNullable(localInTransitDetailMap.get(currentDate)).orElse(0);
            localPlanPurchaseQty = Optional.ofNullable(localEstimatedPurchaseMap.get(currentDate)).orElse(0);
            //到货数量
            int planArrivalQty = fbaInTransitQty + fbaPlanDeliveryQty + overseasInTransitQty + overseasPlanDeliveryQty + localInTransitQty + localPlanPurchaseQty;
            //库存等于结余库存-预计销量+到货数量
            balanceInventory = balanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty));
            resultDTO.setInventoryQty(balanceInventory);
            resultDTO.setPlanArrivalQty(new BigDecimal(planArrivalQty));
            if (Boolean.TRUE.equals(dto.getIsSimulated())) {
                //计算试算的建议库存
                BigDecimal suggestInventory = getSuggestInventory(currentDate, dto.getDeliverySuggest(), dto.getPurchaseSuggest());
                calcBalanceInventory = calcBalanceInventory.subtract(salesEstimate).add(new BigDecimal(planArrivalQty)).add(suggestInventory);
                resultDTO.setCalcInventoryQty(calcBalanceInventory);
                resultDTO.setCalcPlanArrivalQty(new BigDecimal(planArrivalQty).add(suggestInventory));
            }
        }
        resultDTO.setFbaInTransitQty(fbaInTransitQty);
        resultDTO.setFbaPlanDeliveryQty(fbaPlanDeliveryQty);
        resultDTO.setOverseasInTransitQty(overseasInTransitQty);
        resultDTO.setOverseasPlanDeliveryQty(overseasPlanDeliveryQty);
        resultDTO.setLocalInTransitQty(localInTransitQty);
        resultDTO.setLocalPlanPurchaseQty(localPlanPurchaseQty);
        return resultDTO;
    }

    /**
     * 导出数据处理
     * @author will
     * @date 2024/9/8 11:52
     * @param list
     * @return PagingVO<ReplenishmentRuleExportDTO>
     */
    private List<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> handleExport(List<ReplenishmentSuggestionVO.PagingView> list) {
        List<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> exportList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return exportList;
        }

        //店铺
        List<String> shopIdList = list.stream().map(ReplenishmentSuggestionVO.PagingView::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

        //平台
        List<DictBasicDTO.ViewDTO> platformViewList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

        //建议id集合
        List<String> suggestIdList = list.stream().map(ReplenishmentSuggestionVO.PagingView::getId).distinct().collect(Collectors.toList());
        //备货
        List<CfgRuleStockUpEntity> cfgRuleStockUpList = cfgRuleStockUpService.listByRefIdList(suggestIdList);

        //动态备货系数
        List<String> stockUpIdList = cfgRuleStockUpList.stream().map(CfgRuleStockUpEntity::getId).distinct().collect(Collectors.toList());
        List<CfgRuleStockingRatioEntity> cfgRuleStockingRatioList = cfgRuleStockingRatioService.listByStockUpIdList(stockUpIdList);

        //物流信息
        List<CfgRuleLogisticsEntity> cfgRuleLogisticList = cfgRuleLogisticsService.listByStockUpIdList(stockUpIdList);

        //销量主表
        List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList = cfgRuleSalesQtyService.listByRefIdList(suggestIdList);

        //日销量信息
        List<String> salesQtyIdList = cfgRuleSalesQtyList.stream().map(CfgRuleSalesQtyEntity::getId).distinct().collect(Collectors.toList());
        List<CfgRuleSalesFormulaEntity> salesFormulaList = cfgRuleSalesFormulaService.listBySalesQtyIdList(salesQtyIdList);

       //销量去噪
        List<CfgRuleSalesDenoisingEntity> cfgRuleSalesDenoisingList = cfgRuleSalesDenoisingService.listBySalesQtyIdList(salesQtyIdList);

        for (ReplenishmentSuggestionVO.PagingView pagingView :list) {
             ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO exportDTO = new ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO();

            //备货主表
            List<CfgRuleStockUpEntity> thisStockUpList = cfgRuleStockUpList.stream().filter(obj ->StrUtil.equals(obj.getRefId(),pagingView.getId())).collect(Collectors.toList());

            //销量主表
            List<CfgRuleSalesQtyEntity> thisSalesQtyList = cfgRuleSalesQtyList.stream().filter(obj ->StrUtil.equals(obj.getRefId(),pagingView.getId())).collect(Collectors.toList());

            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), pagingView.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");

            //平台名称
            String platformName = platformViewList.stream().filter(obj -> StrUtil.equals(obj.getValue(), pagingView.getPlatform())).map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");

            List<CfgRuleStockUpDTO.StockUpExportDTO> stockUpExportList = formatExportStockUp(pagingView, thisStockUpList, cfgRuleLogisticList, shopName, platformName);
            exportDTO.setStockUpExportList(stockUpExportList);
            List<CfgRuleStockingRatioDTO.StockingRatioExportDTO> stockingRatioExportList = formatExportStockingRatio(pagingView,thisStockUpList, cfgRuleStockingRatioList, shopName, platformName);
            exportDTO.setStockingRatioExportList(stockingRatioExportList);
            List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> defaultSalesQtyExportList = formatDefaultSalesQty(pagingView,thisSalesQtyList, salesFormulaList, shopName, platformName);
            exportDTO.setDefaultSalesQtyExportList(defaultSalesQtyExportList);
            List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> dynamicSalesQtyExportList = formatDynamicSalesQty(pagingView,thisSalesQtyList, salesFormulaList, shopName, platformName);
            exportDTO.setDynamicSalesQtyExportList(dynamicSalesQtyExportList);
            List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> fixedSalesQtyExportList = formatFixedSalesQty(pagingView,thisSalesQtyList, salesFormulaList, shopName, platformName);
            exportDTO.setFixedSalesQtyExportList(fixedSalesQtyExportList);
            List<CfgRuleSalesDenoisingDTO.salesDenoisingExportDTO> salesDenoisingExportList = formatSalesDenoising(pagingView,thisSalesQtyList,  cfgRuleSalesDenoisingList, shopName, platformName);
            exportDTO.setSalesDenoisingExportList(salesDenoisingExportList);
            exportList.add(exportDTO);
        }
        return exportList;
    }
    /**
     * 备货信息
     */
    private List<CfgRuleStockUpDTO.StockUpExportDTO> formatExportStockUp (ReplenishmentSuggestionVO.PagingView pagingView,List<CfgRuleStockUpEntity> cfgRuleStockUpList,
                                                                          List<CfgRuleLogisticsEntity> cfgRuleLogisticList,String shopName,String platformName) {
        List<CfgRuleStockUpDTO.StockUpExportDTO> stockUpList = new ArrayList<>();

        if (CollectionUtils.isEmpty(cfgRuleStockUpList)) {
            return stockUpList;
        }
        for (CfgRuleStockUpEntity stockUpEntity : cfgRuleStockUpList) {
            CfgRuleStockUpDTO.StockUpExportDTO stockUpExportDTO = new CfgRuleStockUpDTO.StockUpExportDTO();
            BeanMapperUtils.copy(stockUpEntity,stockUpExportDTO);
            stockUpExportDTO.setPlatform(platformName);
            stockUpExportDTO.setSkuNo(pagingView.getSkuNo());
            stockUpExportDTO.setShopName(shopName);
            //空运
            CfgRuleLogisticsEntity oneLogisticsEntity = cfgRuleLogisticList.stream().filter(obj -> StrUtil.equals(obj.getStockUpId(), stockUpEntity.getId()) && StrUtil.equals(obj.getLogisticsMethod(), LogisticsMethodEnum.AIRFREIGHT.getCode())).findFirst().orElse(new CfgRuleLogisticsEntity());
            stockUpExportDTO.setOneLogisticsCycleDays(oneLogisticsEntity.getLogisticsCycleDays());
            stockUpExportDTO.setOneLogisticsDays(oneLogisticsEntity.getLogisticsDays());
            stockUpExportDTO.setOneIndex(oneLogisticsEntity.getIndex());

            //快递
            CfgRuleLogisticsEntity twoLogisticsEntity = cfgRuleLogisticList.stream().filter(obj -> StrUtil.equals(obj.getStockUpId(), stockUpEntity.getId()) && StrUtil.equals(obj.getLogisticsMethod(), LogisticsMethodEnum.EXPRESS.getCode())).findFirst().orElse(new CfgRuleLogisticsEntity());
            stockUpExportDTO.setTwoLogisticsCycleDays(twoLogisticsEntity.getLogisticsCycleDays());
            stockUpExportDTO.setTwoLogisticsDays(twoLogisticsEntity.getLogisticsDays());
            stockUpExportDTO.setTwoIndex(twoLogisticsEntity.getIndex());

            //海运散装
            CfgRuleLogisticsEntity threeLogisticsEntity = cfgRuleLogisticList.stream().filter(obj -> StrUtil.equals(obj.getStockUpId(), stockUpEntity.getId()) && StrUtil.equals(obj.getLogisticsMethod(), LogisticsMethodEnum.OCEAN_FREIGHT_BULK.getCode())).findFirst().orElse(new CfgRuleLogisticsEntity());
            stockUpExportDTO.setThreeLogisticsCycleDays(threeLogisticsEntity.getLogisticsCycleDays());
            stockUpExportDTO.setThreeLogisticsDays(threeLogisticsEntity.getLogisticsDays());
            stockUpExportDTO.setThreeIndex(threeLogisticsEntity.getIndex());

            //海运整柜
            CfgRuleLogisticsEntity fourLogisticsEntity = cfgRuleLogisticList.stream().filter(obj -> StrUtil.equals(obj.getStockUpId(), stockUpEntity.getId()) && StrUtil.equals(obj.getLogisticsMethod(), LogisticsMethodEnum.OCEAN_FREIGHT_FCL.getCode())).findFirst().orElse(new CfgRuleLogisticsEntity());
            stockUpExportDTO.setFourLogisticsCycleDays(fourLogisticsEntity.getLogisticsCycleDays());
            stockUpExportDTO.setFourLogisticsDays(fourLogisticsEntity.getLogisticsDays());
            stockUpExportDTO.setFourIndex(fourLogisticsEntity.getIndex());

            //铁运散装
            CfgRuleLogisticsEntity fiveLogisticsEntity = cfgRuleLogisticList.stream().filter(obj -> StrUtil.equals(obj.getStockUpId(), stockUpEntity.getId()) && StrUtil.equals(obj.getLogisticsMethod(), LogisticsMethodEnum.RAILWAY_TRANSPORTATION_BULK.getCode())).findFirst().orElse(new CfgRuleLogisticsEntity());
            stockUpExportDTO.setFiveLogisticsCycleDays(fiveLogisticsEntity.getLogisticsCycleDays());
            stockUpExportDTO.setFiveLogisticsDays(fiveLogisticsEntity.getLogisticsDays());
            stockUpExportDTO.setFiveIndex(fiveLogisticsEntity.getIndex());

            //铁运整柜
            CfgRuleLogisticsEntity sixLogisticsEntity = cfgRuleLogisticList.stream().filter(obj -> StrUtil.equals(obj.getStockUpId(), stockUpEntity.getId()) && StrUtil.equals(obj.getLogisticsMethod(), LogisticsMethodEnum.RAILWAY_TRANSPORTATION_FCL.getCode())).findFirst().orElse(new CfgRuleLogisticsEntity());
            stockUpExportDTO.setSixLogisticsCycleDays(sixLogisticsEntity.getLogisticsCycleDays());
            stockUpExportDTO.setSixLogisticsDays(sixLogisticsEntity.getLogisticsDays());
            stockUpExportDTO.setSixIndex(sixLogisticsEntity.getIndex());
            stockUpList.add(stockUpExportDTO);
        }
        return stockUpList;
    }

    /**
     * 备货系数
     */
    private List<CfgRuleStockingRatioDTO.StockingRatioExportDTO> formatExportStockingRatio (ReplenishmentSuggestionVO.PagingView pagingView,List<CfgRuleStockUpEntity> cfgRuleStockUpList,List<CfgRuleStockingRatioEntity> cfgRuleStockingRatioList,String shopName,String platformName) {
        List<CfgRuleStockingRatioDTO.StockingRatioExportDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(cfgRuleStockingRatioList)) {
            return resultList;
        }
        List<String> stockUpIdList = cfgRuleStockUpList.stream().filter(obj -> StrUtil.equals(obj.getRefId(), pagingView.getId())).map(CfgRuleStockUpEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(stockUpIdList)) {
            return resultList;
        }
        List<CfgRuleStockingRatioEntity> stockingRatioList = cfgRuleStockingRatioList.stream().filter(obj -> (stockUpIdList.contains(obj.getStockUpId()))).collect(Collectors.toList());
        for (CfgRuleStockingRatioEntity stockingRatioEntity : stockingRatioList) {
            CfgRuleStockingRatioDTO.StockingRatioExportDTO exportDTO = new CfgRuleStockingRatioDTO.StockingRatioExportDTO();
            BeanMapperUtils.copy(stockingRatioEntity,exportDTO);
            exportDTO.setPlatform(platformName);
            exportDTO.setSkuNo(pagingView.getSkuNo());
            exportDTO.setShopName(shopName);
            resultList.add(exportDTO);
        }
        return resultList;
    }

    /**
     * 默认销量
     */
    private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> formatDefaultSalesQty (ReplenishmentSuggestionVO.PagingView pagingView,List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList, List<CfgRuleSalesFormulaEntity> salesFormulaList, String shopName, String platformName) {
        List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> resultList = new ArrayList<>();

        if (CollectionUtils.isEmpty(cfgRuleSalesQtyList)) {
            return resultList;
        }
        List<String> salesQtyIdList = cfgRuleSalesQtyList.stream().map(CfgRuleSalesQtyEntity::getId).collect(Collectors.toList());

        //默认销量
        List<CfgRuleSalesFormulaEntity> defaultList = salesFormulaList.stream().filter(obj -> salesQtyIdList.contains(obj.getSalesQtyId()) && StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(defaultList)) {
            return resultList;
        }

        for (CfgRuleSalesFormulaEntity salesFormulaEntity : defaultList) {
            CfgRuleSalesFormulaDTO.SalesFormulaExportDTO exportDTO = new CfgRuleSalesFormulaDTO.SalesFormulaExportDTO();
            exportDTO.setPlatform(platformName);
            exportDTO.setSkuNo(pagingView.getSkuNo());
            exportDTO.setShopName(shopName);
            exportDTO.setDefaultTypeName(CfgRuleSalesFormulaDefaultTypeEnum.getName(salesFormulaEntity.getDefaultType()));
            exportDTO.setFixedValue(salesFormulaEntity.getFixedValue());
            //格式化动态销量百分比
            handlePercentJson(salesFormulaEntity,exportDTO);
            resultList.add(exportDTO);
        }
        return resultList;
    }
    /**
     * 动态销量
     */
    private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> formatDynamicSalesQty (ReplenishmentSuggestionVO.PagingView pagingView,List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList, List<CfgRuleSalesFormulaEntity> salesFormulaList, String shopName, String platformName) {
        List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(cfgRuleSalesQtyList)) {
            return resultList;
        }
        List<String> salesQtyIdList = cfgRuleSalesQtyList.stream().map(CfgRuleSalesQtyEntity::getId).collect(Collectors.toList());

        //默认销量
        List<CfgRuleSalesFormulaEntity> dynamicList = salesFormulaList.stream().filter(obj -> salesQtyIdList.contains(obj.getSalesQtyId()) &&  StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dynamicList)) {
            return resultList;
        }
        for (CfgRuleSalesFormulaEntity salesFormulaEntity : dynamicList) {
            CfgRuleSalesFormulaDTO.SalesFormulaExportDTO exportDTO = new CfgRuleSalesFormulaDTO.SalesFormulaExportDTO();
            exportDTO.setPlatform(platformName);
            exportDTO.setSkuNo(pagingView.getSkuNo());
            exportDTO.setShopName(shopName);
            exportDTO.setName(salesFormulaEntity.getName());
            exportDTO.setStartDate(salesFormulaEntity.getStartDate());
            exportDTO.setEndDate(salesFormulaEntity.getEndDate());
            //格式化动态销量百分比
            handlePercentJson(salesFormulaEntity,exportDTO);
            resultList.add(exportDTO);
        }
        return resultList;
    }
    /**
     * 固定销量
     */
    private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> formatFixedSalesQty (ReplenishmentSuggestionVO.PagingView pagingView,List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList,List<CfgRuleSalesFormulaEntity> salesFormulaList, String shopName, String platformName) {
        List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(cfgRuleSalesQtyList)) {
            return resultList;
        }
        List<String> salesQtyIdList = cfgRuleSalesQtyList.stream().map(CfgRuleSalesQtyEntity::getId).collect(Collectors.toList());
        //默认销量
        List<CfgRuleSalesFormulaEntity> fixedList = salesFormulaList.stream().filter(obj -> salesQtyIdList.contains(obj.getSalesQtyId()) &&  StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.FIXED.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fixedList)) {
            return resultList;
        }
        for (CfgRuleSalesFormulaEntity salesFormulaEntity :fixedList) {
            CfgRuleSalesFormulaDTO.SalesFormulaExportDTO exportDTO = new CfgRuleSalesFormulaDTO.SalesFormulaExportDTO();
            exportDTO.setPlatform(platformName);
            exportDTO.setSkuNo(pagingView.getSkuNo());
            exportDTO.setShopName(shopName);
            exportDTO.setName(salesFormulaEntity.getName());
            exportDTO.setStartDate(salesFormulaEntity.getStartDate());
            exportDTO.setEndDate(salesFormulaEntity.getEndDate());
            exportDTO.setFixedValue(salesFormulaEntity.getFixedValue());
            resultList.add(exportDTO);
        }
        return resultList;
    }

    /**
     * 销量去噪
     */
    private List<CfgRuleSalesDenoisingDTO.salesDenoisingExportDTO> formatSalesDenoising (ReplenishmentSuggestionVO.PagingView pagingView,List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList,List<CfgRuleSalesDenoisingEntity> cfgRuleSalesDenoisingList, String shopName, String platformName) {
        List<CfgRuleSalesDenoisingDTO.salesDenoisingExportDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(cfgRuleSalesDenoisingList)) {
            return resultList;
        }
        List<String> salesQtyIdList = cfgRuleSalesQtyList.stream().map(CfgRuleSalesQtyEntity::getId).collect(Collectors.toList());
        List<CfgRuleSalesDenoisingEntity> salesDenoisingList = cfgRuleSalesDenoisingList.stream().filter(obj -> salesQtyIdList.contains(obj.getSalesQtyId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(salesDenoisingList)) {
            return resultList;
        }
        for (CfgRuleSalesDenoisingEntity salesDenoisingEntity : salesDenoisingList) {
            CfgRuleSalesDenoisingDTO.salesDenoisingExportDTO  exportDTO = new CfgRuleSalesDenoisingDTO.salesDenoisingExportDTO();
            exportDTO.setPlatform(platformName);
            exportDTO.setSkuNo(pagingView.getSkuNo());
            exportDTO.setShopName(shopName);
            exportDTO.setName(salesDenoisingEntity.getName());
            exportDTO.setStartDate(salesDenoisingEntity.getStartDate());
            exportDTO.setEndDate(salesDenoisingEntity.getEndDate());
            exportDTO.setDenoisingTypeName(CfgRuleSalesDenoisingDenoisingTypeEnum.getName(salesDenoisingEntity.getDenoisingType()));
            //百分比去噪
            if (StrUtil.equals(CfgRuleSalesDenoisingDenoisingTypeEnum.PERCENTAGE.getCode(),salesDenoisingEntity.getDenoisingType())) {
                exportDTO.setPercentageValue(salesDenoisingEntity.getEffectiveValue());
            }
            //固定值去噪
            if (StrUtil.equals(CfgRuleSalesDenoisingDenoisingTypeEnum.FIXED_VALUE.getCode(),salesDenoisingEntity.getDenoisingType())) {
                exportDTO.setFixedValue(salesDenoisingEntity.getEffectiveValue());
            }
            resultList.add(exportDTO);
        }
        return resultList;
    }

    /**
     * 动态百分比格式化
     * @author will
     * @date 2024/9/8 10:22
     * @param salesFormulaEntity
     * @param exportDTO
     */
    private void handlePercentJson (CfgRuleSalesFormulaEntity salesFormulaEntity,CfgRuleSalesFormulaDTO.SalesFormulaExportDTO exportDTO) {
        //动态百分比格式化
        CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = JSONUtil.toBean(salesFormulaEntity.getPercentJson(), CfgRuleSalesFormulaDTO.PercentJsonDTO.class);
        exportDTO.setThreeDaysRatio(percentJsonDTO.getThreeDaysRatio());
        exportDTO.setSevenDaysRatio(percentJsonDTO.getSevenDaysRatio());
        exportDTO.setFourteenDaysRatio(percentJsonDTO.getFourteenDaysRatio());
        exportDTO.setThirtyDaysRatio(percentJsonDTO.getThirtyDaysRatio());
        exportDTO.setSixtyDaysRatio(percentJsonDTO.getSixtyDaysRatio());
        exportDTO.setNinetyDaysRatio(percentJsonDTO.getNinetyDaysRatio());
        exportDTO.setOneHundredEightyDaysRatio(percentJsonDTO.getOneHundredEightyDaysRatio());
        exportDTO.setTwoHundredSeventyDaysRatio(percentJsonDTO.getTwoHundredSeventyDaysRatio());
        exportDTO.setThreeHundredSixtyDaysRatio(percentJsonDTO.getThreeHundredSixtyDaysRatio());
    }
}
