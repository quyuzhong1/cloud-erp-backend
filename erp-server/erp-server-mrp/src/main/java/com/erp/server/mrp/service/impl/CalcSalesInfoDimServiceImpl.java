package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.Md5Util;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.CfgRuleSalesDenoisingDenoisingTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaDefaultTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
import com.erp.model.mrp.enums.TimePeriodEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.service.CalcSalesInfoHisEsService;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.erp.server.mrp.mapper.CalcSalesInfoDimMapper;
import com.erp.server.mrp.service.*;
import com.erp.server.mrp.utils.DataDifferenceCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Resource
    private OrderHistorySalesEsService orderHistorySalesEsService;
    @Resource
    private CfgRuleSalesFormulaCalcService cfgRuleSalesFormulaCalcService;
    @Resource
    private CfgRuleSalesDenoisingCalcService cfgRuleSalesDenoisingCalcService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private CalcSalesInfoFavoriteService calcSalesInfoFavoriteService;

    @Override
    public void calcSalesInfo(List<CalcSalesInfoDimDTO.CalcResultDTO> calcResultList) {
        for (CalcSalesInfoDimDTO.CalcResultDTO dto : calcResultList) {
            CompletableFuture.runAsync(() -> {
                CalcSalesInfoDimEntity entity = new CalcSalesInfoDimEntity();
                entity.setId(dto.getCalcSalesInfoDimId());
                List<CalcSalesInfoDenoisingEntity> allSalesList = new ArrayList<>();
                //开始计算去噪销量
                List<CalcSalesInfoDenoisingEntity> calculationSales = calculationSales(dto, allSalesList, entity);
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
        Map<String, String> dictBasicMap = getPlatformMap();;
        view.setPlatform(dictBasicMap.get(view.getPlatform()));
        return view;
    }

    @Override
    public CalcSalesInfoDimDTO.HistorySalesVO historySales(CalcSalesInfoDimDTO.HistorySalesDTO dto) {
        CalcSalesInfoDimEntity entity = validateAndFetchEntity(dto.getId());
        CfgRuleCalcEntity cfgRuleCalc = fetchCfgRuleCalcEntity(entity.getCfgRuleCalcId());
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
        CalcSalesInfoDimEntity entity = validateAndFetchEntity(dto.getId());
        CfgRuleCalcEntity cfgRuleCalc = fetchCfgRuleCalcEntity(entity.getCfgRuleCalcId());
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        if (ObjectUtils.isEmpty(dto.getStartDate()) || ObjectUtils.isEmpty(dto.getEndDate())) {
            startDate = cfgRuleCalc.getStartCalcDate();
            endDate = cfgRuleCalc.getEndCalcDate();
        }
        List<OrderHistorySalesEsEntity> orderHistorySalesList = orderHistorySalesEsService.findByShopIdAndSkuIdAndDateBetween(entity.getShopId(),
                entity.getSkuId(), startDate, endDate);
        Map<LocalDate, Integer> orderHistorySalesMap = orderHistorySalesList.stream()
                .collect(Collectors.toMap(OrderHistorySalesEsEntity::getDate, OrderHistorySalesEsEntity::getOriginalSalesQty, Integer::sum));
        List<CalcSalesInfoEstimateEntity> calcSalesInfoEstimateList = calcSalesInfoEstimateService.listByCalcSalesInfoIds(Collections.singletonList(dto.getId()));
        Map<LocalDate, BigDecimal> calcSalesInfoEstimateMap = calcSalesInfoEstimateList.stream()
                .collect(Collectors.toMap(CalcSalesInfoEstimateEntity::getDate, CalcSalesInfoEstimateEntity::getQty));
        List<LocalDate> dates = new ArrayList<>();
        List<Integer> realSalesList = new ArrayList<>();
        List<BigDecimal> salesEstimateList = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            dates.add(startDate);
            realSalesList.add(Optional.ofNullable(orderHistorySalesMap.get(startDate)).orElse(0));
            salesEstimateList.add(Optional.ofNullable(calcSalesInfoEstimateMap.get(startDate)).orElse(BigDecimal.ZERO));
            startDate = startDate.plusDays(1);
        }
        CalcSalesInfoDimDTO.SalesEstimateDTO salesEstimateDTO = new CalcSalesInfoDimDTO.SalesEstimateDTO();
        salesEstimateDTO.setDateList(dates);
        salesEstimateDTO.setSalesEstimateList(salesEstimateList);
        salesEstimateDTO.setRealSalesList(realSalesList);
        return salesEstimateDTO;
    }

    @Override
    public void exportSalesInfo(CalcSalesInfoDimDTO.ExportSalesInfoDTO dto) {
        downloadTaskFeign.saveDownloadTask("销量试算导出", FileTaskEventEnum.EXPORT_MRP_SALES_CALC.getCode(), dto);
    }

    @Override
    public PagingVO<CalcSalesInfoDimDTO.ExportResultDTO> getListExportData(PagingDTO<CalcSalesInfoDimDTO.ExportSalesInfoDTO> dto) {

        Page<CalcSalesInfoDimDTO.ExportDTO> page = baseMapper.exportData(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        List<CalcSalesInfoDimDTO.ExportResultDTO> results = processExportData(page.getRecords());
        return new PagingVO<>(results, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public PagingVO<CalcSalesInfoDimDTO.DetailViewDTO> pagingDetail(PagingDTO<CalcSalesInfoDimDTO.ParamDTO> params) {
        LoginUser user = UserContext.getDefaultLoginUser();
        Page<CalcSalesInfoDimDTO.DetailViewDTO> page = baseMapper.pagingDetail(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams(), user.getUid());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            processDetailData(page.getRecords(), user.getUid());
        }
        return new PagingVO<>(page);
    }

    /**
     * 处理分页参数
     *
     * @param records 记录
     * @param uid     用户id
     */

    private void processDetailData(List<CalcSalesInfoDimDTO.DetailViewDTO> records, String uid) {
        List<String> cfgRuleCalcIdList = calcSalesInfoFavoriteService.listByUserId(uid);
        List<String> skuIds = records.stream().map(CalcSalesInfoDimDTO.DetailViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<String> ids = records.stream().map(CalcSalesInfoDimDTO.DetailViewDTO::getId).distinct().collect(Collectors.toList());
        List<CalcSalesInfoEstimateEntity> salesInfoEstimateList = calcSalesInfoEstimateService.listByCalcSalesInfoIds(ids);
        List<String> shopIds = records.stream().map(CalcSalesInfoDimDTO.DetailViewDTO::getShopId).distinct().collect(Collectors.toList());
        List<String> country = records.stream().map(CalcSalesInfoDimDTO.DetailViewDTO::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(country)) {
            countryList = sysDictFeign.listCountryByIds(country);
        }
        List<SkuVO> skuVOS = plmTaskFeign.listSkuCategoryByIds(skuIds);
        List<ShopInfoEntity> shopInfos = shopInfoFeign.listShopInfoByIds(shopIds);
        List<String> cfgRuleCalcIds = records.stream().map(CalcSalesInfoDimDTO.DetailViewDTO::getCfgRuleCalcId).distinct().collect(Collectors.toList());
        List<CfgRuleCalcEntity> cfgRuleCalcList = cfgRuleCalcService.listByIds(cfgRuleCalcIds);
        Map<String, String> dictBasicMap = getPlatformMap();
        for (CalcSalesInfoDimDTO.DetailViewDTO record : records) {
            SkuVO skuVO = skuVOS.stream().filter(v -> v.getSkuId().equals(record.getSkuId())).findFirst().orElse(new SkuVO());
            ShopInfoEntity shopInfoEntity = shopInfos.stream().filter(v -> v.getId().equals(record.getShopId())).findFirst().orElse(new ShopInfoEntity());
            DictCountryEntity dictCountry = countryList.stream().filter(v -> v.getId().equals(record.getCountry())).findFirst().orElse(new DictCountryEntity());
            record.setProductName(skuVO.getSkuName());
            record.setSkuImgUrl(skuVO.getSkuImagesUrl());
            record.setCountryName(dictCountry.getNameCn());
            record.setCountryImgUrl(dictCountry.getFlagUrl());
            record.setPlatform(dictBasicMap.get(record.getPlatform()));
            record.setShopName(shopInfoEntity.getName());
            record.setSalesQtyList(JSON.parseObject(record.getSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>() {
            }));
            record.setAvgSalesQtyList(JSON.parseObject(record.getAvgSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>() {
            }));
            record.setMonthSalesEstimateQtyList(JSON.parseObject(record.getMonthSalesEstimateQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.MonthSalesVO>>() {
            }));
            record.setMonthRealSalesQtyList(JSON.parseObject(record.getMonthRealSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.MonthSalesVO>>() {
            }));
            CfgRuleCalcEntity cfgRuleCalc = cfgRuleCalcList.stream()
                    .filter(v -> v.getId().equals(record.getCfgRuleCalcId()))
                    .findFirst()
                    .orElse(null);
            if (!ObjectUtils.isEmpty(cfgRuleCalc)) {
                Map<LocalDate, BigDecimal> estimateMap = salesInfoEstimateList.stream()
                        .filter(v -> v.getCalcSalesInfoDimId().equals(record.getId()))
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
                record.setSalesEstimateVO(new CalcSalesInfoDimDTO.SalesEstimateVO(dates, qty));

            }
            record.setFavorite(cfgRuleCalcIdList.contains(record.getCfgRuleCalcId()));
        }

    }

    /**
     * 获取平台名字
     */
    private static Map<String, String> getPlatformMap() {
        List<DictBasicEntity> salesPlatformList = FeignQuery.create(DictBasicEntity.class)
                .eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType())
                .eq(DictBasicEntity::getStatus, Boolean.TRUE)
                .eq(DictBasicEntity::getIsDeleted, Boolean.FALSE)
                .list();
        return salesPlatformList.stream()
                .collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName));
    }

    @Override
    public PagingVO<CalcSalesInfoDimDTO.TemplateViewDTO> pagingTemplate(PagingDTO<CalcSalesInfoDimDTO.ParamDTO> params) {
        LoginUser user = UserContext.getDefaultLoginUser();
        Page<CalcSalesInfoDimDTO.TemplateViewDTO> page = baseMapper.pagingTemplate(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams(), user.getUid());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            processTemplateData(page.getRecords(), user.getUid());
        }
        return new PagingVO<>(page);
    }

    @Override
    public BatchResultDTO updateRemark(String id, String remark) {
        //新建对象
        CalcSalesInfoDimEntity entity = new CalcSalesInfoDimEntity();
        entity.setId(id);
        entity.setRemark(remark);
        this.updateById(entity);
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void downloadHistorySales(String calcSalesInfoDimId, HttpServletResponse response) {
        CalcSalesInfoDimEntity entity = Optional.ofNullable(getById(calcSalesInfoDimId)).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST, "销量试算"));
        List<CalcSalesInfoHisEsEntity> calcSalesInfoHisList = calcSalesInfoHisEsService.findByCfgRuleCalcIdAndShopIdAndSkuId(entity.getCfgRuleCalcId(), entity.getShopId(), entity.getSkuId());
        List<CfgRuleCalcDTO.HistorySaleDTO> list = new ArrayList<>();
        if (!ObjectUtil.isEmpty(calcSalesInfoHisList)) {
            ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(entity.getShopId());
            Map<String, String> platformMap = getPlatformMap();
            for (CalcSalesInfoHisEsEntity calcSalesInfoHis : calcSalesInfoHisList) {
                CfgRuleCalcDTO.HistorySaleDTO dto = BeanMapperUtils.map(CfgRuleCalcDTO.HistorySaleDTO.class, calcSalesInfoHis);
                dto.setPlatform(platformMap.get(shopInfo.getDictPlatform()));
                dto.setBillDate(calcSalesInfoHis.getDate());
                list.add(dto);
            }
        }
        StringBuilder sb = new StringBuilder();
        String excelPath = "excel/historySaleQty.xlsx";
        String name = "系统试算历史销量";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95125);
        }
    }

    @Override
    public CalcSalesInfoDimDTO.CalcCompareDTO calcCompare(CalcSalesInfoDimDTO.CalcCompareParamsDTO dto) {
        List<CalcSalesInfoDimDTO.CompareResultDTO> list;
        //查询对应数据
        if (CollectionUtils.isEmpty(dto.getIds())) {
            list = baseMapper.listBySkuAndShopAndDate(dto);
        } else {
            list = baseMapper.listCompareByIds(dto.getIds());
        }
        verifyData(list);
        //获取真实销量
        CalcSalesInfoDimDTO.CompareResultDTO resultDTO = list.get(0);
        Map<String, String> calcNameMap = list.stream().collect(Collectors.toMap(CalcSalesInfoDimDTO.CompareResultDTO::getId, CalcSalesInfoDimDTO.CompareResultDTO::getName));
        List<OrderHistorySalesEsEntity> salesInfos = orderHistorySalesEsService.findByShopIdAndSkuIdAndDateBetween(resultDTO.getShopId(), resultDTO.getSkuId(),
                resultDTO.getStartCalcDate(), resultDTO.getEndCalcDate());
        Map<LocalDate, Integer> hisSalesMap = salesInfos.stream()
                .collect(Collectors.toMap(OrderHistorySalesEsEntity::getDate, OrderHistorySalesEsEntity::getOriginalSalesQty, Integer::sum));

        LocalDate startCalcDate = resultDTO.getStartCalcDate();

        List<BigDecimal> basicData = new ArrayList<>();
        List<LocalDate> dateList = new ArrayList<>();
        //组装历史真实销量
        while (startCalcDate.isBefore(resultDTO.getEndCalcDate())) {
            basicData.add(new BigDecimal(Optional.ofNullable(hisSalesMap.get(startCalcDate)).orElse(0)));
            dateList.add(startCalcDate);
            startCalcDate = startCalcDate.plusDays(1);
        }
        List<String> dimIds = list.stream().map(CalcSalesInfoDimDTO.CompareResultDTO::getId).distinct().collect(Collectors.toList());
        //查询预估销量
        List<CalcSalesInfoEstimateEntity> calcSalesInfoEstimateList = calcSalesInfoEstimateService.listByCalcSalesInfoIds(dimIds);
        Map<String, List<BigDecimal>> calcDataList = calcSalesInfoEstimateList.stream()
                .sorted(Comparator.comparing(CalcSalesInfoEstimateEntity::getDate))
                .collect(Collectors.groupingBy(CalcSalesInfoEstimateEntity::getCalcSalesInfoDimId,
                        Collectors.mapping(CalcSalesInfoEstimateEntity::getQty, Collectors.toList())));
        List<CalcSalesInfoDimDTO.LineDTO> calcList = new ArrayList<>();
        for (Map.Entry<String, List<BigDecimal>> entry : calcDataList.entrySet()) {
            CalcSalesInfoDimDTO.LineDTO lineDTO = new CalcSalesInfoDimDTO.LineDTO();
            lineDTO.setName(calcNameMap.get(entry.getKey()));
            lineDTO.setQty(entry.getValue());
            calcList.add(lineDTO);
        }
        DataDifferenceCalculator.findTopNSimilarData(calcList, basicData, DataDifferenceCalculator.COSINE);
        List<CalcSalesInfoDimDTO.LineDTO> lineList = new ArrayList<>();
        lineList.add(new CalcSalesInfoDimDTO.LineDTO("真实销量",BigDecimal.ONE, basicData));
        lineList.addAll(calcList);
        CalcSalesInfoDimDTO.CalcCompareDTO calcCompareDTO = new CalcSalesInfoDimDTO.CalcCompareDTO();
        calcCompareDTO.setDateList(dateList);
        calcCompareDTO.setLineList(lineList);
        return calcCompareDTO;
    }

    @Override
    public CalcSalesInfoDimDTO.CalcCompareDataDTO calcCompareData(CalcSalesInfoDimDTO.CalcCompareParamsDTO dto) {
        List<CalcSalesInfoDimDTO.CompareResultDTO> list;
        //查询对应数据
        if (CollectionUtils.isEmpty(dto.getIds())) {
            list = baseMapper.listBySkuAndShopAndDate(dto);
        } else {
            list = baseMapper.listCompareByIds(dto.getIds());
        }
        verifyData(list);
        CalcSalesInfoDimDTO.CalcCompareDataDTO viewDTO = new CalcSalesInfoDimDTO.CalcCompareDataDTO();
        CalcSalesInfoDimDTO.CompareResultDTO resultDTO = list.get(0);
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(Collections.singletonList(resultDTO.getSkuId()));
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Collections.singletonList(resultDTO.getCountry()));
        SkuVO skuVO = skuVOS.stream().filter(v -> v.getSkuId().equals(resultDTO.getSkuId())).findFirst().orElse(new SkuVO());
        List<ShopInfoEntity> shopInfos = shopInfoFeign.listShopInfoByIds(Collections.singletonList(resultDTO.getShopId()));
        DictCountryEntity dictCountry = countryList.stream().filter(v -> v.getId().equals(resultDTO.getCountry())).findFirst().orElse(new DictCountryEntity());
        ShopInfoEntity shopInfoEntity = shopInfos.stream().filter(v -> v.getId().equals(resultDTO.getShopId())).findFirst().orElse(new ShopInfoEntity());
        Map<String, String> dictBasicMap = getPlatformMap();
        viewDTO.setSkuNo(resultDTO.getSkuNo());
        viewDTO.setProductName(skuVO.getSkuName());
        viewDTO.setSkuImgUrl(skuVO.getSkuImagesUrl());
        viewDTO.setShopName(shopInfoEntity.getName());
        viewDTO.setPlatform(dictBasicMap.get(resultDTO.getPlatform()));
        viewDTO.setCountryName(dictCountry.getNameCn());
        viewDTO.setStartCalcDate(resultDTO.getStartCalcDate());
        return viewDTO;
    }

    /**
     * 校验数据合法性
     *
     * @param list 参数
     */
    private void verifyData(List<CalcSalesInfoDimDTO.CompareResultDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_CALC_DATA);
        }
        // 校验是否是相同sku 店铺 试算开始时间
        long count = list.stream().map(v -> v.getSkuId() + "-" + v.getShopId() + "-" + v.getStartCalcDate())
                .distinct().count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_DATA_IS_DIFFERENT);
        }
        // 校验历史销量是否一致
        long md5Count = list.stream().map(CalcSalesInfoDimDTO.CompareResultDTO::getHisDataMd5)
                .distinct().count();
        if (md5Count > 1) {
            throw new ServiceException(ApiError.ERROR_HIS_SALES_IS_DIFFERENT);
        }
    }


    /**
     * 处理分页参数
     *
     * @param records 记录
     * @param uid     用户id
     */
    private void processTemplateData(List<CalcSalesInfoDimDTO.TemplateViewDTO> records, String uid) {
        List<String> cfgRuleCalcIds = records.stream().map(CalcSalesInfoDimDTO.TemplateViewDTO::getCfgRuleCalcId).distinct().collect(Collectors.toList());
        List<CalcSalesInfoDimEntity> list = list(Wrappers.<CalcSalesInfoDimEntity>lambdaQuery().in(CalcSalesInfoDimEntity::getCfgRuleCalcId, cfgRuleCalcIds));
        Map<String, List<String>> listMap = list.stream()
                .collect(Collectors.groupingBy(CalcSalesInfoDimEntity::getCfgRuleCalcId, Collectors.mapping(CalcSalesInfoDimEntity::getId, Collectors.toList())));
        List<String> cfgRuleCalcIdList = calcSalesInfoFavoriteService.listByUserId(uid);
        for (CalcSalesInfoDimDTO.TemplateViewDTO record : records) {
            record.setFavorite(cfgRuleCalcIdList.contains(record.getCfgRuleCalcId()));
            record.setCalcSalesInfoDimIds(listMap.get(record.getCfgRuleCalcId()));
        }
    }

    /**
     * 处理数据
     *
     * @param records 记录
     */
    private List<CalcSalesInfoDimDTO.ExportResultDTO> processExportData(List<CalcSalesInfoDimDTO.ExportDTO> records) {
        List<String> cfgRuleCalcIds = records.stream().map(CalcSalesInfoDimDTO.ExportDTO::getCfgRuleCalcId).distinct().collect(Collectors.toList());
        List<String> ids = records.stream().map(CalcSalesInfoDimDTO.ExportDTO::getId).distinct().collect(Collectors.toList());
        List<String> shopIds = records.stream().map(CalcSalesInfoDimDTO.ExportDTO::getShopId).distinct().collect(Collectors.toList());
        List<CalcSalesInfoHisEsEntity> calcSalesInfoHisList = calcSalesInfoHisEsService.findByCfgRuleCalcIdIn(cfgRuleCalcIds);
        List<CfgRuleSalesFormulaCalcEntity> formulaCalcList = cfgRuleSalesFormulaCalcService.listByCfgRuleCalcIds(cfgRuleCalcIds);
        List<CfgRuleSalesDenoisingCalcEntity> denoisingCalcList = cfgRuleSalesDenoisingCalcService.listByCfgRuleCalcIds(cfgRuleCalcIds);
        List<CalcSalesInfoEstimateEntity> calcSalesInfoEstimateList = calcSalesInfoEstimateService.listByCalcSalesInfoIds(ids);
        List<CalcSalesInfoDenoisingEntity> calcSalesInfoDenoisingList = calcSalesInfoDenoisingService.listByCalcSalesInfoIds(ids);
        List<ShopInfoEntity> shopInfoList = shopInfoFeign.listShopInfoByIds(shopIds);
        Map<String, ShopInfoEntity> shopMap = shopInfoList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, v -> v, (o1, o2) -> o1));
        Map<String, String> dictBasicMap = getPlatformMap();
        List<CalcSalesInfoDimDTO.ExportResultDTO> results = new ArrayList<>();
        for (CalcSalesInfoDimDTO.ExportDTO record : records) {
            CalcSalesInfoDimDTO.ExportResultDTO result = new CalcSalesInfoDimDTO.ExportResultDTO();
            setSalesAttribute(record, shopMap, calcSalesInfoDenoisingList, calcSalesInfoHisList, result, calcSalesInfoEstimateList, dictBasicMap);
            setCfgRuleAttribute(record.getCfgRuleCalcId(), formulaCalcList, denoisingCalcList, result);
            results.add(result);
        }
        return results;
    }

    /**
     * 保存销量参数
     *
     * @param record                     记录
     * @param shopMap                    店铺
     * @param calcSalesInfoDenoisingList 去噪销量
     * @param calcSalesInfoHisList       历史销量
     * @param result                     结果
     * @param calcSalesInfoEstimateList  销量预估
     * @param dictBasicMap               平台字典
     */
    private static void setSalesAttribute(CalcSalesInfoDimDTO.ExportDTO record, Map<String, ShopInfoEntity> shopMap,
                                          List<CalcSalesInfoDenoisingEntity> calcSalesInfoDenoisingList, List<CalcSalesInfoHisEsEntity> calcSalesInfoHisList,
                                          CalcSalesInfoDimDTO.ExportResultDTO result, List<CalcSalesInfoEstimateEntity> calcSalesInfoEstimateList, Map<String, String> dictBasicMap) {
        ShopInfoEntity shopInfo = Optional.ofNullable(shopMap.get(record.getShopId())).orElse(new ShopInfoEntity());
        Map<LocalDate, CalcSalesInfoDenoisingEntity> calcDenoisingMap = calcSalesInfoDenoisingList.stream()
                .filter(v -> v.getCalcSalesInfoDimId().equals(record.getId()))
                .collect(Collectors.toMap(CalcSalesInfoDenoisingEntity::getDate, v -> v, (o1, o2) -> o1));
        Map<LocalDate, Integer> calcSalesInfoHisMap = calcSalesInfoHisList.stream()
                .filter(v -> v.getCfgRuleCalcId().equals(record.getCfgRuleCalcId()))
                .filter(v -> v.getShopId().equals(record.getShopId()))
                .filter(v -> v.getSkuId().equals(record.getSkuId()))
                .collect(Collectors.toMap(CalcSalesInfoHisEsEntity::getDate, CalcSalesInfoHisEsEntity::getQty, Integer::sum));
        //获取去噪销量
        LocalDate startDate = record.getStartCalcDate().minusDays(361);
        LocalDate endDate = record.getStartCalcDate().minusDays(1);
        List<CalcSalesInfoDimDTO.SalesInfoDenoisingDTO> salesInfoDenoising = new ArrayList<>();
        while (startDate.isBefore(endDate)) {
            salesInfoDenoising.add(CalcSalesInfoDimDTO.SalesInfoDenoisingDTO.buildSalesInfoDenoisingDTO(record, shopInfo.getName(),
                    dictBasicMap.get(shopInfo.getDictPlatform()), startDate, calcDenoisingMap, calcSalesInfoHisMap));
            startDate = startDate.plusDays(1);
        }
        result.setSalesInfoDenoising(salesInfoDenoising);
        //获取预估销量
        List<CalcSalesInfoDimDTO.SalesInfoEstimateDTO> salesInfoEstimate = calcSalesInfoEstimateList.stream()
                .filter(v -> v.getCalcSalesInfoDimId().equals(record.getId()))
                .map(v -> CalcSalesInfoDimDTO.SalesInfoEstimateDTO.buildSalesInfoEstimateDTO(v, record, shopInfo.getName(), dictBasicMap.get(shopInfo.getDictPlatform())))
                .collect(Collectors.toList());
        result.setSalesInfoEstimate(salesInfoEstimate);
    }

    /**
     * 设置
     *
     * @param cfgRuleCalcId     试算配置id
     * @param formulaCalcList   销量配置
     * @param denoisingCalcList 去噪配置
     * @param result            结果
     */
    private void setCfgRuleAttribute(String cfgRuleCalcId, List<CfgRuleSalesFormulaCalcEntity> formulaCalcList, List<CfgRuleSalesDenoisingCalcEntity> denoisingCalcList, CalcSalesInfoDimDTO.ExportResultDTO result) {
        //获取默认配置
        List<CfgRuleSalesFormulaCalcDTO.ExportDTO> defaultSalesQtyExportList = formulaCalcList.stream()
                .filter(v -> v.getCfgRuleCalcId().equals(cfgRuleCalcId))
                .filter(v -> CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(v.getType()))
                .map(CfgRuleSalesFormulaCalcDTO.ExportDTO::buildExportDTO)
                .collect(Collectors.toList());
        //获取动态销量配置
        List<CfgRuleSalesFormulaCalcDTO.ExportDTO> dynamicSalesQtyExportList = formulaCalcList.stream()
                .filter(v -> v.getCfgRuleCalcId().equals(cfgRuleCalcId))
                .filter(v -> CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode().equals(v.getType()))
                .map(CfgRuleSalesFormulaCalcDTO.ExportDTO::buildExportDTO)
                .collect(Collectors.toList());
        //获取固定销量配置
        List<CfgRuleSalesFormulaCalcDTO.ExportDTO> fixedSalesQtyExportList = formulaCalcList.stream()
                .filter(v -> v.getCfgRuleCalcId().equals(cfgRuleCalcId))
                .filter(v -> CfgRuleSalesFormulaTypeEnum.FIXED.getCode().equals(v.getType()))
                .map(CfgRuleSalesFormulaCalcDTO.ExportDTO::buildExportDTO)
                .collect(Collectors.toList());
        //获取去噪配置
        List<CfgRuleSalesDenoisingCalcDTO.ExportDTO> cfgRuleSalesDenoising = denoisingCalcList.stream()
                .filter(v -> v.getCfgRuleCalcId().equals(cfgRuleCalcId))
                .map(CfgRuleSalesDenoisingCalcDTO.ExportDTO::buildExportDTO)
                .collect(Collectors.toList());

        result.setDefaultSalesQtyExportList(defaultSalesQtyExportList);
        result.setDynamicSalesQtyExportList(dynamicSalesQtyExportList);
        result.setFixedSalesQtyExportList(fixedSalesQtyExportList);
        result.setCfgRuleSalesDenoising(cfgRuleSalesDenoising);
    }

    /**
     * 验证并获取试算数据
     *
     * @param id id
     */
    private CalcSalesInfoDimEntity validateAndFetchEntity(String id) {
        CalcSalesInfoDimEntity entity = getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "试算数据");
        }
        return entity;
    }

    /**
     * 验证并获取试算配置
     *
     * @param cfgRuleCalcId 配置id
     */
    private CfgRuleCalcEntity fetchCfgRuleCalcEntity(String cfgRuleCalcId) {
        CfgRuleCalcEntity cfgRuleCalc = cfgRuleCalcService.getById(cfgRuleCalcId);
        if (ObjectUtils.isEmpty(cfgRuleCalc)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "试算配置");
        }
        return cfgRuleCalc;
    }

    private void processData(List<CalcSalesInfoDimDTO.PagingView> records) {
        List<String> skuIds = records.stream().map(CalcSalesInfoDimDTO.PagingView::getSkuId).distinct().collect(Collectors.toList());
        List<String> ids = records.stream().map(CalcSalesInfoDimDTO.PagingView::getId).distinct().collect(Collectors.toList());
        List<CalcSalesInfoEstimateEntity> salesInfoEstimateList = calcSalesInfoEstimateService.listByCalcSalesInfoIds(ids);
        List<String> shopIds = records.stream().map(CalcSalesInfoDimDTO.PagingView::getShopId).distinct().collect(Collectors.toList());
        List<String> country = records.stream().map(CalcSalesInfoDimDTO.PagingView::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(country)) {
            countryList = sysDictFeign.listCountryByIds(country);
        }
        List<SkuVO> skuVOS = plmTaskFeign.listSkuCategoryByIds(skuIds);
        List<ShopInfoEntity> shopInfos = shopInfoFeign.listShopInfoByIds(shopIds);
        List<String> cfgRuleCalcIds = records.stream().map(CalcSalesInfoDimDTO.PagingView::getCfgRuleCalcId).distinct().collect(Collectors.toList());
        List<CfgRuleCalcEntity> cfgRuleCalcList = cfgRuleCalcService.listByIds(cfgRuleCalcIds);
        Map<String, String> dictBasicMap = getPlatformMap();
        for (CalcSalesInfoDimDTO.PagingView view : records) {
            SkuVO skuVO = skuVOS.stream().filter(v -> v.getSkuId().equals(view.getSkuId())).findFirst().orElse(new SkuVO());
            ShopInfoEntity shopInfoEntity = shopInfos.stream().filter(v -> v.getId().equals(view.getShopId())).findFirst().orElse(new ShopInfoEntity());
            DictCountryEntity dictCountry = countryList.stream().filter(v -> v.getId().equals(view.getCountry())).findFirst().orElse(new DictCountryEntity());
            view.setProductName(skuVO.getSkuName());
            view.setSkuImgUrl(skuVO.getSkuImagesUrl());
            view.setCountryName(dictCountry.getNameCn());
            view.setCountryImgUrl(dictCountry.getFlagUrl());
            view.setPlatform(dictBasicMap.get(view.getPlatform()));
            view.setShopName(shopInfoEntity.getName());
            view.setSalesQtyList(JSON.parseObject(view.getSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>() {
            }));
            view.setAvgSalesQtyList(JSON.parseObject(view.getAvgSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>() {
            }));
            view.setMonthSalesEstimateQtyList(JSON.parseObject(view.getMonthSalesEstimateQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.MonthSalesVO>>() {
            }));
            view.setMonthRealSalesQtyList(JSON.parseObject(view.getMonthRealSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.MonthSalesVO>>() {
            }));
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
            entity.setType(formulaResult.getType());
            entity.setDefaultType(formulaResult.getDefaultType());
            entity.setFixedValue(formulaResult.getFixedValue());
            entity.setPercentJson(formulaResult.getPercentJson());
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
    private List<CalcSalesInfoDenoisingEntity> calculationSales(CalcSalesInfoDimDTO.CalcResultDTO dto, List<CalcSalesInfoDenoisingEntity> allSalesList, CalcSalesInfoDimEntity entity) {
        List<CalcSalesInfoDenoisingEntity> calcSalesInfoList = new ArrayList<>();
        LocalDate startDate = dto.getStartCalcDate().minusDays(361);
        LocalDate endDate = dto.getStartCalcDate().minusDays(1);
        List<CalcSalesInfoDimDTO.HisSalesDTO> list = new ArrayList<>();
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
            list.add(new CalcSalesInfoDimDTO.HisSalesDTO(date, originalSalesQty));
            if (ObjectUtils.isEmpty(denoisingResult)) {
                salesInfoDTO.setQty(new BigDecimal(originalSalesQty));
            } else {
                BigDecimal salesQty = getSalesQty(denoisingResult.getDenoisingType(), denoisingResult.getEffectiveValue(), originalSalesQty);
                salesInfoDTO.setQty(salesQty);
                salesInfoDTO.setDenoisingType(denoisingResult.getDenoisingType());
                salesInfoDTO.setEffectiveValue(denoisingResult.getEffectiveValue());
                calcSalesInfoList.add(salesInfoDTO);
            }
            allSalesList.add(salesInfoDTO);
            startDate = startDate.plusDays(1);
        }
        String md5 = Md5Util.md5(JSONUtil.toJsonStr(list));
        entity.setHisDataMd5(md5);
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
