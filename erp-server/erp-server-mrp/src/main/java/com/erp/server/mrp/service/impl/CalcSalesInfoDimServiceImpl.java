package com.erp.server.mrp.service.impl;


import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.dto.SalesForecastCalculatorDTO;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.CfgRuleSalesDenoisingDenoisingTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaDefaultTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
import com.erp.model.mrp.enums.TimePeriodEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;
import com.erp.server.mrp.es.service.CalcSalesInfoHisEsService;
import com.erp.server.mrp.mapper.CalcSalesInfoDimMapper;
import com.erp.server.mrp.service.CalcSalesInfoDenoisingService;
import com.erp.server.mrp.service.CalcSalesInfoDimService;
import com.erp.server.mrp.service.CalcSalesInfoEstimateService;
import com.erp.server.mrp.service.CfgRuleCalcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.CfgRuleSalesDenoisingDenoisingTypeEnum.COMPLETELY;

/**
 * <p>
 * 销量试算表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CalcSalesInfoDimServiceImpl extends SuperServiceImpl<CalcSalesInfoDimMapper, CalcSalesInfoDimEntity> implements CalcSalesInfoDimService {

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private CalcSalesInfoEstimateService calcSalesInfoEstimateService;
    @Resource
    private CalcSalesInfoDenoisingService calcSalesInfoDenoisingService;
    @Resource
    private CfgRuleCalcService cfgRuleCalcService;

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private CalcSalesInfoHisEsService calcSalesInfoHisEsService;

    @Override
    public void calcSalesInfo(List<CalcSalesInfoDimDTO.CalcResultDTO> calcResultList) {
        for (CalcSalesInfoDimDTO.CalcResultDTO dto : calcResultList) {
            CompletableFuture.runAsync(() -> {
                CalcSalesInfoDimEntity entity = new CalcSalesInfoDimEntity();
                entity.setId(dto.getCalcSalesInfoDimId());
                List<CalcSalesInfoDenoisingEntity> allSalesList = new ArrayList<>();
                //开始计算去噪销量
                List<CalcSalesInfoDenoisingEntity> calculationSales = calculationSales(dto, allSalesList);
                //开始计算分时段销量和日均
                List<CalcSalesInfoDimDTO.TimePeriodSalesDTO> avgTimePeriodSales = calculationTimePeriodSales(dto, allSalesList, entity);
                //开始计算销量预估
                List<CalcSalesInfoEstimateEntity> calcSalesInfoEstimateList = calculationSalesEstimates(dto, avgTimePeriodSales, allSalesList);
                //开始计算分时段预估
                calculationTimePeriodSalesEstimates(dto.getStartCalcDate(), entity, calcSalesInfoEstimateList, dto.getSalesHistoryMap());
                calcSalesInfoDenoisingService.saveBatch(calculationSales);
                calcSalesInfoEstimateService.saveBatch(calcSalesInfoEstimateList);
                updateById(entity);
            }, threadPoolTaskExecutor);
        }

    }

    @Override
    public PagingVO<CalcSalesInfoDimDTO.PagingView> paging(PagingDTO<CalcSalesInfoDimDTO.PagingParamDTO> params) {
        Page<CalcSalesInfoDimDTO.PagingView> pagingVO = baseMapper.paging(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        if (!CollectionUtils.isEmpty(pagingVO.getRecords())) {
            processData(pagingVO.getRecords());
        }
        return new PagingVO<>(pagingVO);
    }

    @Override
    public CalcSalesInfoDimDTO.ViewDTO view(String id) {
        CalcSalesInfoDimDTO.ViewDTO view = baseMapper.view(id);
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Collections.singletonList(view.getCountry()));
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(Collections.singletonList(view.getSkuId()));
        List<ShopInfoEntity> shopInfos = shopInfoFeign.listShopInfoByIds(Collections.singletonList(view.getShopId()));
        SkuVO skuVO = skuVOS.stream().filter(v -> v.getSkuId().equals(view.getSkuId())).findFirst().orElse(new SkuVO());
        DictCountryEntity dictCountry = countryList.stream().filter(v -> v.getId().equals(view.getCountry())).findFirst().orElse(new DictCountryEntity());
        ShopInfoEntity shopInfoEntity = shopInfos.stream().filter(v -> v.getId().equals(view.getShopId())).findFirst().orElse(new ShopInfoEntity());
        view.setSkuImgUrl(skuVO.getSkuImagesUrl());
        view.setProductName(skuVO.getSkuName());
        view.setCountryName(dictCountry.getNameCn());
        view.setShopName(shopInfoEntity.getName());
        return view;
    }

    @Override
    public CalcSalesInfoDimDTO.HistorySalesVO historySales(CalcSalesInfoDimDTO.HistorySalesDTO dto) {
        CalcSalesInfoDimEntity entity = getById(dto.getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "试算数据");
        }
        CfgRuleCalcEntity cfgRuleCalc = cfgRuleCalcService.getById(entity.getCfgRuleCalcId());
        if (ObjectUtils.isEmpty(cfgRuleCalc)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "试算配置");
        }
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        if (ObjectUtils.isEmpty(dto.getStartDate()) || ObjectUtils.isEmpty(dto.getEndDate())) {
            startDate = cfgRuleCalc.getStartCalcDate().minusDays(361);
            endDate = cfgRuleCalc.getStartCalcDate();
        }
        List<CalcSalesInfoHisEsEntity> calcSalesInfoHisList = calcSalesInfoHisEsService.findByCfgRuleCalcIdAndSkuIdAndShopIdAndDateBetween(entity.getCfgRuleCalcId(),
                entity.getSkuId(), entity.getShopId(), startDate, endDate);
        Map<LocalDate, Integer> calcSalesInfoHisMap = calcSalesInfoHisList.stream()
                .collect(Collectors.toMap(CalcSalesInfoHisEsEntity::getDate, CalcSalesInfoHisEsEntity::getQty));
        List<CalcSalesInfoDenoisingEntity> calcSalesInfoDenoisingList = calcSalesInfoDenoisingService.listByCalcSalesInfoId(dto.getId());
        Map<LocalDate, BigDecimal> calcSalesInfoDenoisingMap = calcSalesInfoDenoisingList.stream()
                .collect(Collectors.toMap(CalcSalesInfoDenoisingEntity::getDate, CalcSalesInfoDenoisingEntity::getQty));
        List<LocalDate> dates = new ArrayList<>();
        List<Integer> historySales = new ArrayList<>();
        List<BigDecimal> denoisingSales = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            dates.add(startDate);
            Integer historyQty = Optional.ofNullable(calcSalesInfoHisMap.get(startDate)).orElse(0);
            historySales.add(historyQty);
            BigDecimal denoising = Optional.ofNullable(calcSalesInfoDenoisingMap.get(startDate)).orElse(new BigDecimal(historyQty));
            denoisingSales.add(denoising);
            startDate = startDate.plusDays(1);
        }
        CalcSalesInfoDimDTO.HistorySalesVO salesVO = new CalcSalesInfoDimDTO.HistorySalesVO();
        salesVO.setDateList(dates);
        salesVO.setHistorySalesList(historySales);
        salesVO.setDenoisingSalesList(denoisingSales);
        return salesVO;
    }

    @Override
    public CalcSalesInfoDimDTO.SalesEstimateDTO salesEstimation(CalcSalesInfoDimDTO.HistorySalesDTO dto) {
        CalcSalesInfoDimEntity entity = getById(dto.getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "试算数据");
        }
        CfgRuleCalcEntity cfgRuleCalc = cfgRuleCalcService.getById(entity.getCfgRuleCalcId());
        if (ObjectUtils.isEmpty(cfgRuleCalc)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "试算配置");
        }
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        if (ObjectUtils.isEmpty(dto.getStartDate()) || ObjectUtils.isEmpty(dto.getEndDate())) {
            startDate = cfgRuleCalc.getStartCalcDate().minusDays(361);
            endDate = cfgRuleCalc.getStartCalcDate();
        }



        List<LocalDate> dates = new ArrayList<>();
        List<Integer> salesEstimateList = new ArrayList<>();
        List<BigDecimal> realSalesList = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            dates.add(startDate);



            startDate = startDate.plusDays(1);
        }
        CalcSalesInfoDimDTO.SalesEstimateDTO salesEstimateDTO = new CalcSalesInfoDimDTO.SalesEstimateDTO();
        salesEstimateDTO.setDateList(dates);
        salesEstimateDTO.setSalesEstimateList(salesEstimateList);
        salesEstimateDTO.setRealSalesList(realSalesList);
        return salesEstimateDTO;
    }


    private void processData(List<CalcSalesInfoDimDTO.PagingView> records) {
        List<String> skuIds = records.stream().map(CalcSalesInfoDimDTO.PagingView::getSkuId).distinct().collect(Collectors.toList());
        List<String> ids = records.stream().map(CalcSalesInfoDimDTO.PagingView::getId).distinct().collect(Collectors.toList());
        List<CalcSalesInfoEstimateEntity> salesInfoEstimateList = calcSalesInfoEstimateService.listByCalcSalesInfoIds(ids);
        List<String> shopIds = records.stream().map(CalcSalesInfoDimDTO.PagingView::getShopId).distinct().collect(Collectors.toList());
        List<String> country = records.stream().map(CalcSalesInfoDimDTO.PagingView::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(country);
        List<SkuVO> skuVOS = plmTaskFeign.listSkuCategoryByIds(skuIds);
        List<ShopInfoEntity> shopInfos = shopInfoFeign.listShopInfoByIds(shopIds);
        List<String> cfgRuleCalcIds = records.stream().map(CalcSalesInfoDimDTO.PagingView::getCfgRuleCalcId).distinct().collect(Collectors.toList());
        List<CfgRuleCalcEntity> cfgRuleCalcList = cfgRuleCalcService.listByIds(cfgRuleCalcIds);

        for (CalcSalesInfoDimDTO.PagingView view : records) {
            SkuVO skuVO = skuVOS.stream().filter(v -> v.getSkuId().equals(view.getSkuId())).findFirst().orElse(new SkuVO());
            ShopInfoEntity shopInfoEntity = shopInfos.stream().filter(v -> v.getId().equals(view.getShopId())).findFirst().orElse(new ShopInfoEntity());
            DictCountryEntity dictCountry = countryList.stream().filter(v -> v.getId().equals(view.getCountry())).findFirst().orElse(new DictCountryEntity());
            view.setProductName(skuVO.getSkuName());
            view.setSkuImgUrl(skuVO.getSkuImagesUrl());
            view.setCountryName(dictCountry.getNameCn());
            view.setCountryImgUrl(dictCountry.getFlagUrl());
            view.setShopName(shopInfoEntity.getName());
            view.setSalesQtyList(JSON.parseObject(view.getSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>(){}));
            view.setAvgSalesQtyList(JSON.parseObject(view.getAvgSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>(){}));
            view.setMonthSalesEstimateQtyList(JSON.parseObject(view.getMonthSalesEstimateQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.MonthSalesVO>>(){}));
            view.setMonthRealSalesQtyList(JSON.parseObject(view.getMonthRealSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.MonthSalesVO>>(){}));
            CfgRuleCalcEntity cfgRuleCalc = cfgRuleCalcList.stream()
                    .filter(v -> v.getId().equals(view.getCfgRuleCalcId()))
                    .findFirst()
                    .orElse(null);
            if (!ObjectUtils.isEmpty(cfgRuleCalc)) {
                Map<LocalDate, BigDecimal> estimateMap = salesInfoEstimateList.stream()
                        .filter(v -> v.getCalcSalesInfoDimId().equals(view.getId()))
                        .collect(Collectors.toMap(CalcSalesInfoEstimateEntity::getDate, CalcSalesInfoEstimateEntity::getQty));
                LocalDate startCalcDate = cfgRuleCalc.getStartCalcDate();
                LocalDate endDate = startCalcDate.plusDays(14);
                List<LocalDate> dates = new ArrayList<>();
                List<BigDecimal> qty = new ArrayList<>();
                while (!startCalcDate.isAfter(endDate)) {
                    dates.add(startCalcDate);
                    qty.add(Optional.ofNullable(estimateMap.get(startCalcDate)).orElse(BigDecimal.ZERO));
                    startCalcDate = startCalcDate.plusDays(1);
                }
                view.setSalesEstimateVO(new CalcSalesInfoDimDTO.SalesEstimateVO(dates, qty));
            }

        }
    }

    /**
     * 计算销量预估
     *
     * @param dto                参数
     * @param avgTimePeriodSales 日均去噪销量
     * @param allSalesList       历史销量加去噪销量（无去噪规则则历史销量，否则去噪销量）
     */
    private List<CalcSalesInfoEstimateEntity> calculationSalesEstimates(CalcSalesInfoDimDTO.CalcResultDTO dto, List<CalcSalesInfoDimDTO.TimePeriodSalesDTO> avgTimePeriodSales, List<CalcSalesInfoDenoisingEntity> allSalesList) {
        List<CalcSalesInfoEstimateEntity> calcSalesInfoEstimateList = new ArrayList<>();
        LocalDate startCalcDate = dto.getStartCalcDate();
        while (!startCalcDate.isAfter(dto.getEndCalcDate())) {
            CfgRuleSalesFormulaCalcEntity formulaResult = getStrategyFormulaResultDTO(dto.getFormulaCalcEntities(), startCalcDate);
            if (ObjectUtils.isEmpty(formulaResult)) {
                continue;
            }
            BigDecimal saleQty = getSaleQty(allSalesList, avgTimePeriodSales, formulaResult, dto.getStartCalcDate());
            if (saleQty.equals(BigDecimal.ZERO)) {
                continue;
            }
            CalcSalesInfoEstimateEntity entity = new CalcSalesInfoEstimateEntity();
            entity.setCalcSalesInfoDimId(dto.getCalcSalesInfoDimId());
            entity.setQty(saleQty);
            entity.setDate(startCalcDate);
            entity.setMonth(startCalcDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            calcSalesInfoEstimateList.add(entity);
            startCalcDate = startCalcDate.plusDays(1);
        }
        return calcSalesInfoEstimateList;
    }

    /**
     * 获取日销量规则
     *
     * @param formulaResults sku日销量规则
     * @param calcDate       计算日
     */
    private static CfgRuleSalesFormulaCalcEntity getStrategyFormulaResultDTO(List<CfgRuleSalesFormulaCalcEntity> formulaResults, LocalDate calcDate) {
        return formulaResults.stream()
                .filter(v -> !ObjectUtils.isEmpty(v.getStartDate()) && !ObjectUtils.isEmpty(v.getEndDate()) &&
                        !v.getStartDate().isAfter(calcDate) && !v.getEndDate().isBefore(calcDate))
                .min(Comparator.comparing(CfgRuleSalesFormulaCalcEntity::getPriority)
                        .thenComparing(Comparator.comparing(CfgRuleSalesFormulaCalcEntity::getIndex).reversed()))
                .orElse(formulaResults.stream()
                        .filter(v -> CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(v.getType()))
                        .findFirst().orElse(null));
    }

    /**
     * 计算分时段销量预估
     *
     * @param startCalcDate   计算开始时间
     * @param entity          参数
     * @param salesEstimates  预估销量
     * @param salesHistoryMap 历史销量
     */
    private void calculationTimePeriodSalesEstimates(LocalDate startCalcDate, CalcSalesInfoDimEntity entity,
                                                     List<CalcSalesInfoEstimateEntity> salesEstimates, Map<LocalDate, Integer> salesHistoryMap) {

        LocalDate endDate = startCalcDate.plusDays(120);
        // 从开始日期所在的月份的1号开始
        List<CalcSalesInfoDimDTO.MonthSalesVO> timePeriodSalesEstimates = new ArrayList<>();
        List<CalcSalesInfoDimDTO.MonthSalesVO> realTimePeriodSales = new ArrayList<>();
        // 从开始日期所在的月份的1号开始
        LocalDate current = startCalcDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            YearMonth currentMonth = YearMonth.from(current);
            LocalDate calcStartDate = currentMonth.equals(YearMonth.from(endDate)) ? startCalcDate : current;
            LocalDate calcEndDate = currentMonth.equals(YearMonth.from(endDate)) ? endDate : current.with(TemporalAdjusters.lastDayOfMonth());
            BigDecimal followingSales = salesEstimates.stream()
                    .filter(v -> !calcStartDate.isAfter(v.getDate()) && !calcEndDate.isBefore(v.getDate()))
                    .map(CalcSalesInfoEstimateEntity::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal realSales = salesHistoryMap.entrySet().stream()
                    .filter(v -> !calcStartDate.isAfter(v.getKey()) && !calcEndDate.isBefore(v.getKey()))
                    .map(Map.Entry::getValue)
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            timePeriodSalesEstimates.add(new CalcSalesInfoDimDTO.MonthSalesVO(currentMonth.getMonthValue(), followingSales));
            realTimePeriodSales.add(new CalcSalesInfoDimDTO.MonthSalesVO(currentMonth.getMonthValue(), realSales));
            // 移动到下一个月
            current = current.plusMonths(1);
        }
        entity.setMonthSalesEstimateQtyJson(JSONUtil.parseArray(timePeriodSalesEstimates));
        entity.setMonthRealSalesQtyJson(JSONUtil.parseArray(realTimePeriodSales));
    }

    /**
     * 获取预估销量
     *
     * @param salesInfos    建议历史销量
     * @param formulaResult 销量计算参数
     * @param basicCalcDate 计算时间
     */
    public BigDecimal getSaleQty(List<CalcSalesInfoDenoisingEntity> salesInfos, List<CalcSalesInfoDimDTO.TimePeriodSalesDTO> avgTimePeriodSales, CfgRuleSalesFormulaCalcEntity formulaResult, LocalDate basicCalcDate) {
        BigDecimal saleQty;
        if (CfgRuleSalesFormulaTypeEnum.FIXED.getCode().equals(formulaResult.getType())) {
            saleQty = MathUtil.valueOf(formulaResult.getFixedValue());
        } else if (CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode().equals(formulaResult.getType())) {
            saleQty = getDynamicSaleQty(formulaResult.getPercentJsonDTO(), salesInfos, avgTimePeriodSales, basicCalcDate);
        } else {
            if (CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getCode().equals(formulaResult.getDefaultType())) {
                saleQty = getDynamicSaleQty(formulaResult.getPercentJsonDTO(), salesInfos, avgTimePeriodSales, basicCalcDate);
            } else {
                saleQty = MathUtil.valueOf(formulaResult.getFixedValue());
            }
        }
        return saleQty;
    }

    /**
     * 计算动态规则销量
     *
     * @param dto                动态规则
     * @param salesInfos         销量
     * @param avgTimePeriodSales 去噪销量日均
     * @param basicCalcDate      计算日
     */
    private BigDecimal getDynamicSaleQty(CfgRuleSalesFormulaDTO.PercentJsonDTO dto, List<CalcSalesInfoDenoisingEntity> salesInfos, List<CalcSalesInfoDimDTO.TimePeriodSalesDTO> avgTimePeriodSales, LocalDate basicCalcDate) {

        List<SalesForecastCalculatorDTO> salesDataList = new ArrayList<>();
        LocalDate localDate = basicCalcDate.minusDays(1);
        Map<TimePeriodEnum, BigDecimal> timePeriodMap = avgTimePeriodSales
                .stream().collect(Collectors.toMap(CalcSalesInfoDimDTO.TimePeriodSalesDTO::getCode, CalcSalesInfoDimDTO.TimePeriodSalesDTO::getQty, (o1, o2) -> o1));
        // 3天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.THREE), MathUtil.valueOf(dto.getThreeDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 3)));
        // 7天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.SEVEN), MathUtil.valueOf(dto.getSevenDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 7)));
        // 14天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.FOURTEEN), MathUtil.valueOf(dto.getFourteenDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 14)));
        // 30天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.THIRTY), MathUtil.valueOf(dto.getThirtyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 30)));
        // 60天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.SIXTY), MathUtil.valueOf(dto.getSixtyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 60)));
        // 90天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.NINETY), MathUtil.valueOf(dto.getNinetyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 90)));
        // 180天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.ONE_HUNDRED_AND_EIGHTY), MathUtil.valueOf(dto.getOneHundredEightyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 180)));
        // 270天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.TWO_HUNDRED_AND_SEVENTY), MathUtil.valueOf(dto.getTwoHundredSeventyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 270)));
        // 360天日均
        salesDataList.add(new SalesForecastCalculatorDTO(timePeriodMap.get(TimePeriodEnum.THREE_HUNDRED_AND_SIXTY), MathUtil.valueOf(dto.getThreeHundredSixtyDaysRatio()).divide(MathUtil.BigDecimal_100, 2, RoundingMode.HALF_UP), getIsExcluded(salesInfos, localDate, 360)));
        // 计算预估日销量
        return SalesForecastCalculatorDTO.calculateForecastedSales(salesDataList);
    }

    /**
     * 判断是否排除权重
     *
     * @param salesInfos    销量
     * @param basicCalcDate 计算日
     * @param days          天数
     */
    private boolean getIsExcluded(List<CalcSalesInfoDenoisingEntity> salesInfos, LocalDate basicCalcDate, int days) {
        return salesInfos.stream()
                .filter(v -> !basicCalcDate.minusDays(days).isAfter(v.getDate()) && basicCalcDate.isAfter(v.getDate()))
                .allMatch(v -> CfgRuleSalesDenoisingDenoisingTypeEnum.COMPLETELY.getCode().equals(v.getDenoisingType()));
    }


    /**
     * 计算分时段销量和日均
     *
     * @param dto    参数
     * @param entity 试算参数
     */
    private List<CalcSalesInfoDimDTO.TimePeriodSalesDTO> calculationTimePeriodSales(CalcSalesInfoDimDTO.CalcResultDTO dto, List<CalcSalesInfoDenoisingEntity> calculationSales, CalcSalesInfoDimEntity entity) {

        //分时段销量
        List<CalcSalesInfoDimDTO.TimePeriodSalesDTO> timePeriodSales = new ArrayList<>();
        //分时段日均销量
        List<CalcSalesInfoDimDTO.TimePeriodSalesDTO> avgTimePeriodSales = new ArrayList<>();
        LocalDate endDate = dto.getEndCalcDate().minusDays(1);
        for (TimePeriodEnum value : TimePeriodEnum.values()) {
            BigDecimal qty = calculationSales.stream()
                    .filter(v -> !endDate.minusDays(value.getDays()).isAfter(v.getDate()) && endDate.isAfter(v.getDate()))
                    .map(CalcSalesInfoDenoisingEntity::getQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(0, RoundingMode.CEILING);
            timePeriodSales.add(new CalcSalesInfoDimDTO.TimePeriodSalesDTO(value, qty));
            long count = calculationSales.stream()
                    .filter(v -> !endDate.minusDays(value.getDays()).isAfter(v.getDate()) && endDate.isAfter(v.getDate()))
                    .filter(v -> !COMPLETELY.getCode().equals(v.getDenoisingType()))
                    .count();
            BigDecimal avgQty = new BigDecimal(0);
            if (count != 0) {
                avgQty = qty.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            }
            avgTimePeriodSales.add(new CalcSalesInfoDimDTO.TimePeriodSalesDTO(value, avgQty));
        }
        entity.setSalesQtyJson(JSONUtil.parseArray(timePeriodSales.stream().map(v -> new CalcSalesInfoDimDTO.SalesVO(v.getCode().getName(), v.getQty()))
                .collect(Collectors.toList())));
        entity.setAvgSalesQtyJson(JSONUtil.parseArray(avgTimePeriodSales.stream().map(v -> new CalcSalesInfoDimDTO.SalesVO(v.getCode().getName(), v.getQty()))
                .collect(Collectors.toList())));
        return avgTimePeriodSales;
    }

    /**
     * 计算销量
     */
    private List<CalcSalesInfoDenoisingEntity> calculationSales(CalcSalesInfoDimDTO.CalcResultDTO dto, List<CalcSalesInfoDenoisingEntity> allSalesList) {
        List<CalcSalesInfoDenoisingEntity> calcSalesInfoList = new ArrayList<>();
        LocalDate startDate = dto.getStartCalcDate().minusDays(361);
        LocalDate endDate = dto.getStartCalcDate().minusDays(1);
        while (!startDate.isAfter(endDate)) {
            LocalDate date = startDate;
            CalcSalesInfoDenoisingEntity salesInfoDTO = new CalcSalesInfoDenoisingEntity();
            salesInfoDTO.setId(IdWorker.getIdStr());
            salesInfoDTO.setCalcSalesInfoDimId(dto.getCalcSalesInfoDimId());
            salesInfoDTO.setDate(date);
            //获取符合的最大优先级销量去噪规则 (序号越小优先级越大)
            CfgRuleSalesDenoisingCalcEntity denoisingResult = dto.getSalesDenoising().stream()
                    .filter(v -> !v.getStartDate().isAfter(date) && !v.getEndDate().isBefore(date))
                    .max(Comparator.comparing(CfgRuleSalesDenoisingCalcEntity::getIndex))
                    .orElse(null);
            int originalSalesQty = Optional.ofNullable(dto.getSalesHistoryMap().get(date)).orElse(0);
            if (ObjectUtils.isEmpty(denoisingResult)) {
                salesInfoDTO.setQty(new BigDecimal(originalSalesQty));
            } else {
                BigDecimal salesQty = getSalesQty(denoisingResult.getDenoisingType(), denoisingResult.getEffectiveValue(), originalSalesQty);
                salesInfoDTO.setQty(salesQty);
                salesInfoDTO.setDenoisingType(denoisingResult.getDenoisingType());
                calcSalesInfoList.add(salesInfoDTO);
            }
            allSalesList.add(salesInfoDTO);
            startDate = startDate.plusDays(1);
        }
        return calcSalesInfoList;
    }

    /**
     * 计算去噪销量
     *
     * @param denoisingType    去噪类型
     * @param effectiveValue   值
     * @param originalSalesQty 去噪前数量
     */
    private static BigDecimal getSalesQty(String denoisingType, Integer effectiveValue, int originalSalesQty) {
        CfgRuleSalesDenoisingDenoisingTypeEnum code = CfgRuleSalesDenoisingDenoisingTypeEnum.getEnumByCode(denoisingType);
        BigDecimal salesQty = new BigDecimal(0);
        switch (Objects.requireNonNull(code)) {
            case PERCENTAGE:
                salesQty = new BigDecimal(originalSalesQty)
                        .multiply(BigDecimal.valueOf(effectiveValue))
                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                break;
            case FIXED_VALUE:
                salesQty = BigDecimal.valueOf(effectiveValue);
                break;
            default:
                break;
        }
        return salesQty;
    }
}
