package com.erp.server.mrp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import com.erp.server.mrp.mapper.ReplenishmentSuggestionMapper;
import com.erp.server.mrp.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public PagingVO<ReplenishmentSuggestionVO.PagingView> paging(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        Page<ReplenishmentSuggestionVO.PagingView> pagingVO = baseMapper.paging(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
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
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        List<ShopInfoEntity> shopInfos = shopInfoFeign.listShopInfoByIds(shopIds);
        List<ReplenishmentInventoryDetailEntity> replenishmentInventoryDetails = replenishmentInventoryDetailService.listByReplenishmentDetailIds(detailIds);
        List<SalesEstimateManualEntity> salesEstimateManuals = salesEstimateManualService.listByReplenishmentDetailIds(detailIds);
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
            List<LabelVO> vos = labelVOS.stream().filter(v -> v.getReplenishmentId().equals(view.getId())).collect(Collectors.toList());
            view.setLabels(vos);
            view.setShopName(shopInfoEntity.getName());
            List<ReplenishmentInventoryDetailEntity> inventoryDetails = replenishmentInventoryDetails.stream().filter(v -> v.getReplenishmentDetailId().equals(view.getDetailId())).collect(Collectors.toList());
            Map<String, String> inventoryDetailMap = inventoryDetails.stream().collect(Collectors.toMap(ReplenishmentInventoryDetailEntity::getWarehouseType, ReplenishmentInventoryDetailEntity::getInventoryAllocateType, (o1, o2) -> o1));
            //海外仓库存分配类型
            view.setOverseasInventoryAllocateType(CfgRuleInventoryAllocateTypeEnum.getName(inventoryDetailMap.get(CfgRuleWarehouseTypeEnum.OVERSEAS.getCode())));
            //本地仓库存分配类型
            view.setOverseasInventoryAllocateType(CfgRuleInventoryAllocateTypeEnum.getName(inventoryDetailMap.get(CfgRuleWarehouseTypeEnum.LOCAL.getCode())));
            List<SalesInfoEntity> salesInfoList = salesInfos.stream().filter(v -> v.getReplenishmentDetailId().equals(view.getDetailId())).sorted(Comparator.comparing(SalesInfoEntity::getDate)).collect(Collectors.toList());
            List<LocalDate> salesAnalysisDate = salesInfoList.stream().map(SalesInfoEntity::getDate).collect(Collectors.toList());
            List<Integer> salesAnalysisQty = salesInfoList.stream().map(SalesInfoEntity::getSalesQty).collect(Collectors.toList());
            //销量分析
            view.setSalesAnalysis(new ReplenishmentSuggestionVO.SalesAnalysisVO(salesAnalysisDate, salesAnalysisQty));
            SalesEstimateManualVO estimateManualVO = salesEstimateManuals.stream().filter(v -> v.getReplenishmentId().equals(view.getDetailId()))
                    .map(v -> new SalesEstimateManualVO(v.getCurrentMonthSalesQty(), v.getCurrentMonthSurplusSalesQty(), v.getNextMonthSales(), v.getFollowingMonthSales())).findFirst().orElse(null);
            //运营月销量预估
            view.setSalesEstimateManualVO(estimateManualVO);
            // 建议标识相关
            Map<String, RecentSuggestionDetailEntity> recentSuggestionDetailMap = suggestionDetails.stream().filter(v -> v.getReplenishmentDetailId().equals(view.getDetailId()))
                    .collect(Collectors.toMap(RecentSuggestionDetailEntity::getType, v -> v, (o1, o2) -> o1));
            // 最近断货日期
            RecentSuggestionDetailEntity recentOutOfStock = recentSuggestionDetailMap.get(RecentSuggestionDetailEnum.RECENT_OUT_OF_STOCK.name());
            if (!ObjectUtils.isEmpty(recentOutOfStock)){
                view.setOutOfStockDay(new ReplenishmentSuggestionVO.DateVO(recentOutOfStock.getMarkType(), recentOutOfStock.getDate(), recentOutOfStock.getDays()));
            }
            // 最近建议发货日期
            RecentSuggestionDetailEntity recentSuggestShipping = recentSuggestionDetailMap.get(RecentSuggestionDetailEnum.RECENT_SUGGESTION_SHIPPING.name());
            if (!ObjectUtils.isEmpty(recentSuggestShipping)) {
                view.setSuggestShippingDate(new ReplenishmentSuggestionVO.DateVO(recentSuggestShipping.getMarkType(), recentSuggestShipping.getDate(), recentSuggestShipping.getDays()));
                view.setSuggestShippingQty(recentSuggestShipping.getQty());
            }
            // 最近建议采购日期
            RecentSuggestionDetailEntity recentSuggestPurchase = recentSuggestionDetailMap.get(RecentSuggestionDetailEnum.RECENT_SUGGESTION_PURCHASE.name());
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
        view.setCountryName(dictCountry.getNameCn());
        view.setShopName(shopInfoEntity.getName());
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
    public PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<LocalInTransitDetailDTO> params) {
        return localInTransitDetailService.localInTransitDetail(params);
    }

    @Override
    public PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<EstimatedDeliveryDTO> params) {
        return estimatedDeliveryDetailService.estimatedDelivery(params);
    }

    @Override
    public PagingVO<EstimatedPurchaseVO> estimatedPurchase(PagingDTO<EstimatedPurchaseDTO> params) {
        return estimatedPurchaseDetailService.estimatedPurchase(params);
    }

    @Override
    public InventoryDetailVO inventoryDetail(InventoryTotalDTO params) {
        return replenishmentInventoryDetailService.inventoryDetail(params);
    }

    @Override
    public SalesAnalysisVO salesAnalysis(SalesAnalysisDTO dto) {
        SalesAnalysisVO salesAnalysisVO = new SalesAnalysisVO();
        List<SalesInfoEntity> list = salesInfoService.list(Wrappers.<SalesInfoEntity>lambdaQuery()
                .eq(SalesInfoEntity::getReplenishmentDetailId, dto.getDetailId())
                .between(SalesInfoEntity::getDate, dto.getStartDate(), dto.getEndDate())
                .orderByAsc(SalesInfoEntity::getDate)
        );
        List<Integer> originalSales = list.stream().map(SalesInfoEntity::getOriginalSalesQty).collect(Collectors.toList());
        List<Integer> sales = list.stream().map(SalesInfoEntity::getSalesQty).collect(Collectors.toList());
        List<LocalDate> dates = list.stream().map(SalesInfoEntity::getDate).collect(Collectors.toList());
        salesAnalysisVO.setDenoisingSales(new SalesAnalysisVO.SalesVO(dates, sales));
        salesAnalysisVO.setHistorySales(new SalesAnalysisVO.SalesVO(dates, originalSales));
        List<SalesEstimateEntity> estimateEntityList = salesEstimateService.list(Wrappers.<SalesEstimateEntity>lambdaQuery()
                .eq(SalesEstimateEntity::getReplenishmentDetailId, dto.getDetailId())
                .between(SalesEstimateEntity::getDate, LocalDate.now(), LocalDate.now().plusDays(TimePeriodEstimateEnum.of(dto.getTimePeriod()).getDays()))
                .orderByAsc(SalesEstimateEntity::getDate)
        );
        List<Integer> salesEstimates = estimateEntityList.stream().map(SalesEstimateEntity::getSalesQty).collect(Collectors.toList());
        List<LocalDate> salesEstimateDates = estimateEntityList.stream().map(SalesEstimateEntity::getDate).collect(Collectors.toList());
        salesAnalysisVO.setDenoisingSales(new SalesAnalysisVO.SalesVO(salesEstimateDates, salesEstimates));
        return salesAnalysisVO;
    }

    @Override
    public HistoryInventoryVO historyInventory(HistoryInventoryDTO dto) {

        List<SalesInfoEntity> list = salesInfoService.list(Wrappers.<SalesInfoEntity>lambdaQuery()
                .eq(SalesInfoEntity::getReplenishmentDetailId, dto.getDetailId())
                .between(SalesInfoEntity::getDate, dto.getStartDate(), dto.getEndDate())
                .orderByAsc(SalesInfoEntity::getDate)
        );
        List<LocalDate> dates = list.stream().map(SalesInfoEntity::getDate).collect(Collectors.toList());
        List<Integer> qty = list.stream().map(SalesInfoEntity::getOriginalInventoryQty).collect(Collectors.toList());
        return HistoryInventoryVO.buildHistoryInventoryVO(dates, qty);
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
                        rptOutOfStockVO.setSalesQty(Math.addExact(Optional.ofNullable(rptOutOfStockVO.getSalesQty()).orElse(0), v.getSalesQty()));
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
            Integer salesQty = realOutOfStock.stream().map(RealOutOfStockEntity::getSalesQty).reduce(0, Math::addExact);
            rptOutOfStock.setSalesQty(rptOutOfStock.getSalesQty() + salesQty);
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
    public void updateRule(ReplenishmentSuggestionDTO.UpdateRuleDTO dto) {
        if (ObjectUtil.isEmpty(dto.getStockUpUpdateDTO()) && ObjectUtil.isEmpty(dto.getSalesQtyUpdateDTO())) {
            throw new ServiceException("备货、销量设置不能全部为空！");
        }
        //更新备货信息
        if (ObjectUtil.isEmpty(dto.getStockUpUpdateDTO())) {
            dto.getStockUpUpdateDTO().setRefId(dto.getId());
            dto.getStockUpUpdateDTO().setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            cfgRuleStockUpService.update(dto.getStockUpUpdateDTO());
        }
        //更新销量信息
        if (ObjectUtil.isEmpty(dto.getSalesQtyUpdateDTO())) {
            dto.getSalesQtyUpdateDTO().setRefId(dto.getId());
            dto.getSalesQtyUpdateDTO().setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            cfgRuleSalesQtyService.update(dto.getSalesQtyUpdateDTO());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO batchUpdateRule(String id, CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO, CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO) {
        if (ObjectUtil.isEmpty(stockUpUpdateDTO) && ObjectUtil.isEmpty(salesQtyUpdateDTO)) {
            throw new ServiceException("备货、销量设置不能全部为空！");
        }
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));

        //更新备货信息
        if (ObjectUtil.isNotEmpty(stockUpUpdateDTO)) {
            CfgRuleStockUpEntity ruleStockUpEntity = cfgRuleStockUpService.getByRefId(id);
            if (ObjectUtil.isNotEmpty(ruleStockUpEntity)) {
                stockUpUpdateDTO.setId(ruleStockUpEntity.getId());
            }
            stockUpUpdateDTO.setRefId(id);
            stockUpUpdateDTO.setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            CfgRuleStockUpDTO.UpdateDTO updateDTO = BeanMapperUtils.map(CfgRuleStockUpDTO.UpdateDTO.class, stockUpUpdateDTO);
            cfgRuleStockUpService.update(updateDTO);
        }
        //更新销量信息
        if (ObjectUtil.isNotEmpty(salesQtyUpdateDTO)) {
            salesQtyUpdateDTO.setRefId(id);
            salesQtyUpdateDTO.setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            stockUpUpdateDTO.setIsCustom(Boolean.TRUE);
            cfgRuleSalesQtyService.update(salesQtyUpdateDTO);
        }
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO restoreRule(String id, List<String> ruleTypeList) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
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

        //单个编辑标签
        ReplenishmentRefLabelDTO.UpdateDTO dto = new ReplenishmentRefLabelDTO.UpdateDTO();
        dto.setLabelIdList(updateLabelDTO.getLabelIdList());
        dto.setType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        replenishmentRefLabelService.update(dto,entity.getId());

        //现标签
        List<LabelInfoEntity> labelInfoList = labelInfoService.listByIds(updateLabelDTO.getLabelIdList());
        String labelNames = labelInfoList.stream().map(LabelInfoEntity::getName).collect(Collectors.joining(","));
        //原标签
        List<LabelInfoDTO.ViewDTO> oldList = replenishmentRefLabelService.listLabelInfoByRefId(updateLabelDTO.getId());
        String oldLabelNames = oldList.stream().map(LabelInfoDTO.ViewDTO::getName).distinct().collect(Collectors.joining(","));
        // 操作日志
        String msg = StrUtil.format("设置了标签：从【{}}】修改为【{}}】",oldLabelNames,labelNames);
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
        List<LabelInfoEntity> labelInfoList = labelInfoService.listByIds(labelIdList);
        String labelNames = labelInfoList.stream().map(LabelInfoEntity::getName).collect(Collectors.joining(","));
        String msg = StrUtil.format("添加了标签【{}】",labelNames);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "添加标签");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelLabel(String id, List<String> labelIdList) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));

        //取消标签
        replenishmentRefLabelService.deleteLabel(labelIdList,entity.getId());

        List<LabelInfoEntity> labelInfoList = labelInfoService.listByIds(labelIdList);
        String labelNames = labelInfoList.stream().map(LabelInfoEntity::getName).collect(Collectors.joining(","));
        String msg = StrUtil.format("删除了标签【{}】",labelNames);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "删除标签");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.DELETE);
    }

    /**
     * 根据id更新是否补货和补货原因
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
                .update();
    }
    @Override
    public List<ReplenishmentSuggestionEntity> listAllSkuAndShop() {
        return list(Wrappers.<ReplenishmentSuggestionEntity>lambdaQuery()
                .select(ReplenishmentSuggestionEntity::getSkuId,
                        ReplenishmentSuggestionEntity::getSkuNo,
                        ReplenishmentSuggestionEntity::getShopId));
    }

    @Override
    public List<ReplenishmentSuggestionEntity> listByUnique(List<String> platformCodeList, List<String> shopIdList, List<String> skuIdList) {
        return baseMapper.listByUnique(platformCodeList,shopIdList,skuIdList);
    }

    @Override
    public List<ReplenishmentSuggestionEntity> listCalculationData() {
        return list(Wrappers.<ReplenishmentSuggestionEntity>lambdaQuery()
                .eq(ReplenishmentSuggestionEntity::getReplenishmentType, ReplenishmentTypeEnum.NORMAL.getCode())
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
    public PagingVO<DynamicExcelDTO> listHistorySalesQty(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        Page<ReplenishmentSuggestionVO.PagingView> pagingVO = baseMapper.paging(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        List<ReplenishmentSuggestionVO.PagingView> list = pagingVO.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>();
        }
        //历史销量数据处理
        List<LinkedHashMap> resultList = handleHistorySalesQty(list);
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
        // 结果集
        List<LinkedHashMap> convertDataList = Lists.newArrayListWithExpectedSize(list.size());

        // 公共标题字段
        Arrays.asList(SalesInfoExportHeaderEnum.values()).forEach(headerEnum -> {
            headMap.put(headerEnum.getCode(), headerEnum.getName());
        });

        //历史销量
        List<String> detailIdList = list.stream().map(ReplenishmentSuggestionVO.PagingView::getDetailId).distinct().collect(Collectors.toList());
        List<SalesInfoEntity> salesInfoList = salesInfoService.listHistorySalesInfo(detailIdList);

        // 动态字段标题
        if (CollUtil.isNotEmpty(salesInfoList)) {
            salesInfoList.forEach(obj -> headMap.put(obj.getDate().toString(),LocalDateTimeUtil.format(obj.getDate(), DateTimeFormatter.ofPattern("yyyy年MM月dd"))));
        }
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
            for (SalesInfoEntity salesInfoEntity : salesList) {
                convertMap.put(salesInfoEntity.getDate().toString(),salesInfoEntity.getSalesQty());
            }
            convertDataList.add(convertMap);
        }
        resultMap.put("head", headMap);
        resultMap.put("data", convertDataList);
        resultList.add(resultMap);
        return resultList;
    }

    @Override
    public PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> listReplenishmentRule(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        Page<ReplenishmentSuggestionVO.PagingView> pagingVO = baseMapper.paging(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        if (CollectionUtils.isEmpty(pagingVO.getRecords())) {
            return new PagingVO<>();
        }
        List<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> exportPagingVO = handleExport(pagingVO.getRecords());
        return new PagingVO(exportPagingVO,(int)pagingVO.getTotal(),(int)pagingVO.getSize(),(int)pagingVO.getCurrent());
    }

    @Override
    public List<ReplenishmentSuggestionDTO.SalesDTO> listSalesBySkuId(String skuId) {
        return null;
    }

    @Override
    public List<LocalInventoryDTO.ShopSalesDTO> getSalesByShopIds(List<String> shopIds, String skuId) {
        return baseMapper.getSalesByShopIds(shopIds, skuId);
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

            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), pagingView.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");

            //平台名称
            String platformName = platformViewList.stream().filter(obj -> StrUtil.equals(obj.getValue(), pagingView.getPlatform())).map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");

            List<CfgRuleStockUpDTO.StockUpExportDTO> stockUpExportList = formatExportStockUp(pagingView, cfgRuleStockUpList, cfgRuleLogisticList, shopName, platformName);
            exportDTO.setStockUpExportList(stockUpExportList);
            List<CfgRuleStockingRatioDTO.StockingRatioExportDTO> stockingRatioExportList = formatExportStockingRatio(pagingView, cfgRuleStockingRatioList, shopName, platformName);
            exportDTO.setStockingRatioExportList(stockingRatioExportList);
            List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> defaultSalesQtyExportList = formatDefaultSalesQty(pagingView, salesFormulaList, shopName, platformName);
            exportDTO.setDefaultSalesQtyExportList(defaultSalesQtyExportList);
            List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> dynamicSalesQtyExportList = formatDynamicSalesQty(pagingView, salesFormulaList, shopName, platformName);
            exportDTO.setDynamicSalesQtyExportList(dynamicSalesQtyExportList);
            List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> fixedSalesQtyExportList = formatFixedSalesQty(pagingView, salesFormulaList, shopName, platformName);
            exportDTO.setFixedSalesQtyExportList(fixedSalesQtyExportList);
            List<CfgRuleSalesDenoisingDTO.salesDenoisingExportDTO> salesDenoisingExportList = formatSalesDenoising(pagingView, cfgRuleSalesDenoisingList, shopName, platformName);
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
    private List<CfgRuleStockingRatioDTO.StockingRatioExportDTO> formatExportStockingRatio (ReplenishmentSuggestionVO.PagingView pagingView,List<CfgRuleStockingRatioEntity> cfgRuleStockingRatioList,String shopName,String platformName) {
        List<CfgRuleStockingRatioDTO.StockingRatioExportDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(cfgRuleStockingRatioList)) {
            return resultList;
        }
        for (CfgRuleStockingRatioEntity stockingRatioEntity : cfgRuleStockingRatioList) {
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
    private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> formatDefaultSalesQty (ReplenishmentSuggestionVO.PagingView pagingView, List<CfgRuleSalesFormulaEntity> salesFormulaList, String shopName, String platformName) {
        List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> resultList = new ArrayList<>();
        //默认销量
        List<CfgRuleSalesFormulaEntity> defaultList = salesFormulaList.stream().filter(obj -> StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode())).collect(Collectors.toList());
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
    private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> formatDynamicSalesQty (ReplenishmentSuggestionVO.PagingView pagingView, List<CfgRuleSalesFormulaEntity> salesFormulaList, String shopName, String platformName) {
        List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> resultList = new ArrayList<>();
        //默认销量
        List<CfgRuleSalesFormulaEntity> dynamicList = salesFormulaList.stream().filter(obj -> StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode())).collect(Collectors.toList());
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
    private List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> formatFixedSalesQty (ReplenishmentSuggestionVO.PagingView pagingView,List<CfgRuleSalesFormulaEntity> salesFormulaList, String shopName, String platformName) {
        List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> resultList = new ArrayList<>();
        //默认销量
        List<CfgRuleSalesFormulaEntity> fixedList = salesFormulaList.stream().filter(obj -> StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.FIXED.getCode())).collect(Collectors.toList());
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
    private List<CfgRuleSalesDenoisingDTO.salesDenoisingExportDTO> formatSalesDenoising (ReplenishmentSuggestionVO.PagingView pagingView,List<CfgRuleSalesDenoisingEntity> cfgRuleSalesDenoisingList, String shopName, String platformName) {
        List<CfgRuleSalesDenoisingDTO.salesDenoisingExportDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(cfgRuleSalesDenoisingList)) {
            return resultList;
        }
        for (CfgRuleSalesDenoisingEntity salesDenoisingEntity : cfgRuleSalesDenoisingList) {
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
