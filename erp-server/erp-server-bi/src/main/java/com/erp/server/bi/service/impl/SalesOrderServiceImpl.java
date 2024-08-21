package com.erp.server.bi.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.ChartVO;
import com.common.business.vo.PagingVO;
import com.common.business.vo.SeriesVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.enums.*;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.oms.vo.CustomerInfoVO;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.SkuDTO;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.constant.BiConstant;
import com.erp.server.bi.constant.ChartType;
import com.erp.server.bi.enums.DateTypeEnum;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.enums.SiteEnum;
import com.erp.server.bi.enums.TimeTypeEnum;
import com.erp.server.bi.mapper.SalesOrderServiceMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 销售维度 模块服务
 *
 * @Classname
 * @Date 2022-12-16 11:09
 * @Created by yl
 */
@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderServiceMapper, BiOrderInfoEntity>
        implements SalesOrderService {

    @Resource
    private BiProductDetailService productDetailService;

    @Resource
    private BiShopInfoService shopInfoService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private BiTargetNewProductSettingService biTargetNewProductSettingService;

    @Resource
    private BiTargetYearService biTargetYearService;

    @Resource
    private BiDataSourceCostDetailService biDataSourceCostDetailService;


    @Autowired
    private YearMonthValueContext context;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private BiDataSourceCostService biDataSourceCostService;

    @Resource
    private BiTargetStaffSettingService biTargetStaffSettingService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    @Cacheable(cacheNames = "cache:bi:getMonthSales", keyGenerator = "myKeyGenerator")
    public StatisticalDataVO getMonthSales(BiFilterDTO dto) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setChartType(ChartType.BAR);
        statistical.setName("月销售额趋势");
        ChartVO chart = new ChartVO();
        LocalDate now = LocalDate.now();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        String timeFlag = "delivery_time";
        if (dto.getTimeType() != null && TimeTypeEnum.ORDER_TIME.getCode() == dto.getTimeType()) {
            timeFlag = "platform_create_time";
        }
        dto.setStartTime(LocalDateUtil.getThisYearStart(now));
        dto.setEndTime(LocalDateUtil.getThisYearEnd(now));
        List<SalesFlagVO> thisYearList = baseMapper.getMonthSales(dto, settleRate, timeFlag);

        //去年
        dto.setStartTime(LocalDateUtil.getLastYearStart(now));
        dto.setEndTime(LocalDateUtil.getLastYearEnd(now));
        List<SalesFlagVO> lastYearList = baseMapper.getMonthSales(dto, settleRate, timeFlag);

        int initSize = 12;
        List<String> xAxisList = new ArrayList<>(initSize);
        //有两个柱子
        List<SeriesVO<Object>> seriesList = new ArrayList<>(2);

        //去年的
        SeriesVO<Object> thisYearSeries = new SeriesVO();
        thisYearSeries.setName("销售额");
        List<Object> thisYearDataList = thisYearList.stream().map(SalesFlagVO::getSales).collect(Collectors.toList());
        thisYearSeries.setData(thisYearDataList);
        seriesList.add(thisYearSeries);
        //去年的
        SeriesVO<Object> lastYearSeries = new SeriesVO();
        lastYearSeries.setName("销售额");
        List<Object> lastYearDataList = lastYearList.stream().map(SalesFlagVO::getSales).collect(Collectors.toList());
        for (int m = 1; m <= 12; m++) {
            xAxisList.add(m + "月份");
        }
        lastYearSeries.setData(lastYearDataList);
        seriesList.add(lastYearSeries);
        chart.setXAxis(xAxisList);
        chart.setSeries(seriesList);
        statistical.setData(chart);
        return statistical;
    }


    /**
     * 获取到结算汇率
     *
     * @param code
     * @return
     */
    private String getSettleRate(Integer code) {
        SettleMethodEnum settleMethod = SettleMethodEnum.getByCode(code);
        if (settleMethod != null) {
            return settleMethod.getField();
        }
        return "";
    }

    /**
     * 存储进redis
     *
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:common", keyGenerator = "myKeyGenerator")
    public Map<String, String> getSkuItemName() {
        List<SkuItemVO> skuItemNames = baseMapper.getSkuItemName();
        if (CollectionUtils.isNotEmpty(skuItemNames)) {
            return skuItemNames.stream().collect(Collectors.toMap(SkuItemVO::getSkuNo, SkuItemVO::getItemName));
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    @Override
    public PagingVO<SkuSalesDTO.PagingSalesInfoDTO> exportSkuSales(PagingDTO<SkuSalesDTO.SearchSkuDTO> dto) {
        SkuSalesDTO.SearchSkuDTO params = dto.getParams();
        //获取到结算汇率
        String settleRate = getSettleRate(params.getSettleMethod());
        LocalDateTime paramsEndTime = params.getEndTime();
        params.setEndTime(paramsEndTime, 1);

        Page<SkuSalesDTO.PagingSalesInfoDTO> resultList = baseMapper.listSkuSalesExcel(new Page<SkuSalesDTO.PagingSalesInfoDTO>(dto.getCurrPage(),dto.getPageSize()),params, settleRate);
        List<String> skuNoList = resultList.getRecords().stream().map(SkuSalesDTO.PagingSalesInfoDTO::getSkuNo).collect(Collectors.toList());

        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime beforeThirtyDays = LocalDateUtil.getBeforeStartTime(nowTime, 29);
        params.setStartTime(beforeThirtyDays);
        params.setEndTime(nowTime);
        //查询进三十天信息
        List<SalesBaseVO> lastThirtyDays = baseMapper.getLastDays(params, settleRate);


        LocalDateTime beforeSevenDays = LocalDateUtil.getBeforeStartTime(nowTime, 6);
        params.setStartTime(beforeSevenDays);
        params.setEndTime(nowTime);
        //查询进七天信息
        List<SalesBaseVO> lastSevenDays = baseMapper.getLastDays(params, settleRate);


        Integer nowYear = LocalDate.now().getYear();
        //销售信息
        List<SkuDTO.SalesDTO> skuList = plmTaskFeign.listSkuSalesBySkuNos(skuNoList);
        for (SkuSalesDTO.PagingSalesInfoDTO item : resultList.getRecords()) {
            SkuDTO.SalesDTO skuInfo = skuList.stream().filter(s -> s.getSkuNo().equals(item.getSkuNo())).
                    findFirst().orElse(null);
            if (Objects.nonNull(skuInfo)) {
                //公司首单日期
                LocalDate firstOrderDate = skuInfo.getFirstOrderDate();
                if (firstOrderDate != null) {
                    Integer year = firstOrderDate.getYear();
                    if (nowYear.equals(year)) {
                        item.setIsNewProductName("是");
                    }
                }
                item.setFirstOrderDate(skuInfo.getFirstOrderDate());
                item.setSaleStateName(skuInfo.getSaleStateName());
            }

            //近三十天
            Integer lastThirtyDaysSalesQuantity = lastThirtyDays.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) && b.getFlagNo().equals(item.getSkuNo())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();
            //近七天
            Integer lastSevenDaysSalesQuantity = lastSevenDays.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) &&
                            b.getFlagNo().equals(item.getSkuNo())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();

            item.setLastSevenDaysSalesQty(lastSevenDaysSalesQuantity);
            item.setLastThirtyDaysSalesQty(lastThirtyDaysSalesQuantity);
        }
        return new PagingVO<>(resultList);
    }

    /**
     * 一级模块 sku 销售额
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:queryByPageBySku", keyGenerator = "myKeyGenerator")
    public PagingVO<SkuSalesDTO.PagingSalesInfoDTO> queryByPageBySku(PagingDTO<SkuSalesDTO.SearchSkuDTO> dto) {
        SkuSalesDTO.SearchSkuDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        LocalDateTime paramsEndTime = params.getEndTime();
        params.setEndTime(paramsEndTime, 1);
        LocalDate nowDate = LocalDate.now();
        //获取到结算汇率
        String settleRate = getSettleRate(params.getSettleMethod());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.getBySku(query, params, settleRate);
        List<SkuSalesDTO.PagingSalesInfoDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime beforeThirtyDays = LocalDateUtil.getBeforeStartTime(nowTime, 29);
        params.setStartTime(beforeThirtyDays);
        params.setEndTime(nowTime);
        //查询进三十天信息
        List<SalesBaseVO> lastThirtyDays = baseMapper.getLastDays(params, settleRate);


        LocalDateTime beforeSevenDays = LocalDateUtil.getBeforeStartTime(nowTime, 6);
        params.setStartTime(beforeSevenDays);
        params.setEndTime(nowTime);
        //查询进七天信息
        List<SalesBaseVO> lastSevenDays = baseMapper.getLastDays(params, settleRate);
        List<String> skuNoList = list.stream().map(SkuSalesDTO.PagingSalesInfoDTO::getSkuNo).collect(Collectors.toList());
        Integer nowYear = LocalDate.now().getYear();
        //销售信息
        List<SkuDTO.SalesDTO> skuList = plmTaskFeign.listSkuSalesBySkuNos(skuNoList);
        //标签
        Map<String, List<LabelVO>> labelMap = null;
        if (CollectionUtils.isNotEmpty(skuList)) {
            Set<String> skuIds = skuList.stream().map(SkuDTO.SalesDTO::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            List<ProductRefLabelVO> productRefLabelVOS = plmTaskFeign.getProductRelLabelBySkuIds(skuIds);
            if (CollectionUtils.isNotEmpty(productRefLabelVOS)) {
                List<LabelVO> labels = BeanMapperUtils.copyList(LabelVO.class, productRefLabelVOS);
                labelMap = labels.stream().filter(labelVO ->
                        StringUtils.isNotBlank(labelVO.getSkuNo())).collect(Collectors.groupingBy(LabelVO::getSkuNo));
            }
        }
        //获取itemName
        Map<String, String> skuItemNameMap = this.getSkuItemName();
        for (SkuSalesDTO.PagingSalesInfoDTO item : list) {
            SkuDTO.SalesDTO skuInfo = skuList.stream().filter(s -> s.getSkuNo().equals(item.getSkuNo())).
                    findFirst().orElse(null);
            if (Objects.nonNull(skuInfo)) {
                //公司首单日期
                LocalDate firstOrderDate = skuInfo.getFirstOrderDate();
                if (firstOrderDate != null) {
                    Integer year = firstOrderDate.getYear();
                    if (nowYear.equals(year)) {
                        item.setIsNewProduct(Boolean.TRUE);
                    }
                }
                item.setFirstOrderDate(skuInfo.getFirstOrderDate());
                item.setSaleStateName(skuInfo.getSaleStateName());
                item.setProductName(skuItemNameMap.get(item.getSkuNo()));
            }
            //近三十天
            Integer lastThirtyDaysSalesQuantity = lastThirtyDays.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) && b.getFlagNo().equals(item.getSkuNo())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();
            //近七天
            Integer lastSevenDaysSalesQuantity = lastSevenDays.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) &&
                            b.getFlagNo().equals(item.getSkuNo())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();

            item.setLastSevenDaysSalesQty(lastSevenDaysSalesQuantity);
            item.setLastThirtyDaysSalesQty(lastThirtyDaysSalesQuantity);
            List<SalesBaseVO> salesTrendList = new ArrayList<>(7);
            Map<LocalDateTime, SalesBaseVO> dateMap = lastSevenDays.stream().filter(salesBaseVO -> StringUtils.isNotBlank(salesBaseVO.getFlagNo()) && salesBaseVO.getFlagNo().equals(item.getSkuNo()))
                    .collect(Collectors.toMap(SalesBaseVO::getFlagDate, Function.identity()));
            for (int i = 6; i >= 0; i--) {
                LocalDate flagDay = nowDate.minus(i, ChronoUnit.DAYS);
                LocalDateTime startTime = LocalDateUtil.startLocalDateTime(flagDay);
                SalesBaseVO salesBaseVO = dateMap.get(startTime);
                if (Objects.isNull(salesBaseVO)){
                    salesBaseVO = new SalesBaseVO();
                    salesBaseVO.setSales(BigDecimal.ZERO);
                    salesBaseVO.setSalesQuantity(0);
                    salesBaseVO.setFlagDate(startTime);
                    salesBaseVO.setFlagNo(item.getSkuNo());
                }
                salesTrendList.add(salesBaseVO);
            }
            item.setSalesTrendList(salesTrendList);
            //增加标签
            if (Objects.nonNull(labelMap)) {
                item.setLabels(labelMap.get(item.getSkuNo()));
            }
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 导出sku 销售额
     *
     * @param params
     * @return
     */
    @Override
    public Boolean exportSkuSalesExcel(SkuSalesDTO.SearchSkuDTO params, HttpServletResponse response) {
        //获取到结算汇率
        String settleRate = getSettleRate(params.getSettleMethod());
        LocalDateTime paramsEndTime = params.getEndTime();
        params.setEndTime(paramsEndTime, 1);

        List<SkuSalesDTO.PagingSalesInfoDTO> resultList = baseMapper.listSkuSalesExcel(params, settleRate);
        List<String> skuNoList = resultList.stream().map(SkuSalesDTO.PagingSalesInfoDTO::getSkuNo).collect(Collectors.toList());

        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime beforeThirtyDays = LocalDateUtil.getBeforeStartTime(nowTime, 29);
        params.setStartTime(beforeThirtyDays);
        params.setEndTime(nowTime);
        //查询进三十天信息
        List<SalesBaseVO> lastThirtyDays = baseMapper.getLastDays(params, settleRate);


        LocalDateTime beforeSevenDays = LocalDateUtil.getBeforeStartTime(nowTime, 6);
        params.setStartTime(beforeSevenDays);
        params.setEndTime(nowTime);
        //查询进七天信息
        List<SalesBaseVO> lastSevenDays = baseMapper.getLastDays(params, settleRate);


        Integer nowYear = LocalDate.now().getYear();
        //销售信息
        List<SkuDTO.SalesDTO> skuList = plmTaskFeign.listSkuSalesBySkuNos(skuNoList);
        for (SkuSalesDTO.PagingSalesInfoDTO item : resultList) {
            SkuDTO.SalesDTO skuInfo = skuList.stream().filter(s -> s.getSkuNo().equals(item.getSkuNo())).
                    findFirst().orElse(null);
            if (Objects.nonNull(skuInfo)) {
                //公司首单日期
                LocalDate firstOrderDate = skuInfo.getFirstOrderDate();
                if (firstOrderDate != null) {
                    Integer year = firstOrderDate.getYear();
                    if (nowYear.equals(year)) {
                        item.setIsNewProductName("是");
                    }
                }
                item.setFirstOrderDate(skuInfo.getFirstOrderDate());
                item.setSaleStateName(skuInfo.getSaleStateName());
            }

            //近三十天
            Integer lastThirtyDaysSalesQuantity = lastThirtyDays.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) && b.getFlagNo().equals(item.getSkuNo())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();
            //近七天
            Integer lastSevenDaysSalesQuantity = lastSevenDays.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) &&
                            b.getFlagNo().equals(item.getSkuNo())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();

            item.setLastSevenDaysSalesQty(lastSevenDaysSalesQuantity);
            item.setLastThirtyDaysSalesQty(lastThirtyDaysSalesQuantity);

//            BigDecimal sales = item.getSales();
//            Integer orderCount = item.getOrderCount();
//            if (orderCount != 0 && sales != null) {
//                //客单价
//                BigDecimal perCustomerTransaction = sales.divide(new BigDecimal(orderCount), 2, BigDecimal.ROUND_HALF_UP);
//                item.setPerCustomerTransaction(perCustomerTransaction);
//            }

        }

        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/SkuSales.xlsx";
        String name = "sku销售额";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(resultList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("sku销售额导出出错 >>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 产品等级销售分析
     *
     * @param params
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:productGradeSales", keyGenerator = "myKeyGenerator")
    public StatisticalDataVO productGradeSales(BiFilterDTO params) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        //获取到结算汇率
        String settleRate = getSettleRate(params.getSettleMethod());
        LocalDateTime paramsEndTime = params.getEndTime();
        params.setEndTime(paramsEndTime, 1);
        //产品销售等级销售额
        List<Map<String, Object>> gradeSalesList = baseMapper.listProductGradeSales(params, settleRate);
        int initSize = CollectionUtils.isNotEmpty(gradeSalesList) ? gradeSalesList.size() : 10;

        statistical.setName("产品等级销售额");
        statistical.setChartType(ChartType.PIE);
        ChartVO chartVO = new ChartVO();
        chartVO.setXAxis(new ArrayList<>());
        List<SeriesVO<Object>> seriesList = new ArrayList<>(initSize);
        SeriesVO<Object> series = new SeriesVO();
        series.setName("平台销售额");
        List<Object> list = new ArrayList<>(gradeSalesList.size());
        for (Map<String, Object> map : gradeSalesList) {
            list.add(map);
        }
        series.setData(list);
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        statistical.setData(chartVO);
        return statistical;
    }

    /**
     * 一级模块 spu 销售额
     *
     * @param dto
     * @return
     */
    @Override
    public List<SalesVO> getBySpu(BiFilterDTO dto) {
        return null;
    }


    /**
     * 根据国家查询销售额
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesByCountryVO>
     * @author yl
     * @date 2022-12-20 9:15
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:getByCountry", keyGenerator = "myKeyGenerator")
    public XyAxesResultVO getByCountry(BiFilterDTO dto) {
        XyAxesResultVO result = new XyAxesResultVO();

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        //获取到数据
        List<SalesByCountryVO> list = baseMapper.getByCountry(dto, settleRate);
        //国家
        List<String> countryList = list.stream().map(SalesByCountryVO::getCountry).distinct().collect(Collectors.toList());

        //sku
        List<String> skuList = list.stream().map(SalesByCountryVO::getSku).distinct().collect(Collectors.toList());


        //国家分组
        Map<String, List<SalesByCountryVO>> countryMap = list.parallelStream().
                collect(Collectors.groupingBy(SalesByCountryVO::getCountry));


        //sku 分组
        Map<String, List<SalesByCountryVO>> skuMap = list.parallelStream().
                collect(Collectors.groupingBy(SalesByCountryVO::getSku));

        //列名
        List<XAxesVO> columnList = new LinkedList<>();
        XAxesVO shopAxes = new XAxesVO();
        shopAxes.setProp("sku");
        shopAxes.setLabel("SKU");
        columnList.add(shopAxes);

        //品名
        XAxesVO productNameAxes = new XAxesVO();
        productNameAxes.setProp("productName");
        productNameAxes.setLabel("品名");
        columnList.add(productNameAxes);

        //国家
        for (String country : countryList) {
            XAxesVO axes = new XAxesVO();
            axes.setProp(country);
            axes.setLabel(country);
            columnList.add(axes);
        }

        List<Map<String, Object>> rowAxesList = new LinkedList<>();
        for (String sku : skuList) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            rowMap.put("sku", sku);
            //产品名称
            String productName = skuMap.containsKey(sku) ? skuMap.get(sku).get(0).getProductName() : "";
            rowMap.put("productName", productName);
            for (String country : countryList) {
                if (countryMap.containsKey(country)) {
                    //国家sku
                    List<SalesByCountryVO> countrySalesList = countryMap.get(country);
                    BigDecimal countrySales = countrySalesList.stream().filter(c -> c.getSku().equals(sku)).
                            map(SalesByCountryVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                    rowMap.put(country, countrySales);
                } else {
                    rowMap.put(country, 0);
                }

            }
            rowAxesList.add(rowMap);
        }
        result.setColumnList(columnList);
        result.setRowList(rowAxesList);
        return result;
    }


    /**
     * 一级销售模块 平台
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2022-12-20 12:28
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:getByPlatformRatio", keyGenerator = "myKeyGenerator")
    public StatisticalDataVO getByPlatformRatio(BiFilterDTO dto) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        //获取各个平台的销售额
        List<Map<String, Object>> resultList = baseMapper.getPlatformSales(dto, settleRate);
        int initSize = CollectionUtils.isNotEmpty(resultList) ? resultList.size() : 10;
        statistical.setName("平台销售额与占比");
        statistical.setChartType(ChartType.PIE);
        ChartVO<BiSalesRadioDTO> chartVO = new ChartVO<>();
        chartVO.setXAxis(new ArrayList<>());
        List<SeriesVO<BiSalesRadioDTO>> seriesList = new ArrayList<>(initSize);
        SeriesVO<BiSalesRadioDTO> series = new SeriesVO<>();
        series.setName("平台销售额与占比");
        List<BiSalesRadioDTO> list = new ArrayList<>(resultList.size());
        // 总和
        BigDecimal sumNumber = BigDecimal.ZERO;
        for (Map<String, Object> map : resultList) {
            BiSalesRadioDTO itemDto = BiSalesRadioDTO.init(map);
            list.add(itemDto);
            sumNumber = sumNumber.add(new BigDecimal(itemDto.getSales()));
        }
        // 设置占比
        BigDecimal finalSumNumber = sumNumber;
        list.forEach(o -> {
            o.setRadioBySumSumNumber(finalSumNumber);
        });

        statistical.setSumNumber(sumNumber.stripTrailingZeros().toPlainString());
        series.setData(list);
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        statistical.setData(chartVO);
        return statistical;
    }

    /**
     * 一级销售模块 TOB  TOC
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2022-12-20 12:28
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byTobToc",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO byTobToc(BiFilterDTO dto) {
        StatisticalDataVO statistical = new StatisticalDataVO();

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        //获取各个平台的销售额
        List<SalesFlagVO> resultList = baseMapper.byTobToc(dto, settleRate);
        int initSize = CollectionUtils.isNotEmpty(resultList) ? resultList.size() : 10;
        statistical.setName("平台销售额");
        statistical.setChartType(ChartType.PIE);
        ChartVO chartVO = new ChartVO();
        chartVO.setXAxis(new ArrayList<>());
        List<SeriesVO<Object>> seriesList = new ArrayList<>(initSize);
        SeriesVO<Object> series = new SeriesVO();
        series.setName("平台销售额");
        List<Object> list = new ArrayList<>(2);
        Map<String, Object> tobMap = new HashMap<>();
        String b2b = "B2B";
        tobMap.put("name", "TOB");
        BigDecimal tobSales = resultList.stream().filter(
                b -> b2b.equals(b.getName()) && b.getSales() != null
        ).map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
        tobMap.put("value", tobSales);
        list.add(tobMap);

        Map<String, Object> tocMap = new HashMap<>();
        ;
        tocMap.put("name", "TOC");
        BigDecimal toCSales = resultList.stream().filter(
                b -> !b2b.equals(b.getName()) && b.getSales() != null
        ).map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
        tocMap.put("value", toCSales);
        list.add(tocMap);

        series.setData(list);
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        statistical.setData(chartVO);
        return statistical;
    }

    /**
     * 一级销售模块 店铺销售额
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.ShopSalesVO>
     * @author yl
     * @date 2022-12-21 10:33
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:getByShop",keyGenerator = "myKeyGenerator")
    public List<ShopSalesVO> getByShop(BiFilterDTO dto) {
        LocalDate nowDate = LocalDate.now();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        String findTime = "delivery_time";
        if (dto.getTimeType() != null && dto.getTimeType() == 0) {
            findTime = "platform_create_time";
        }
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<ShopSalesVO> resultList = baseMapper.getByShop(dto, settleRate);
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime beforeThirtyDays = LocalDateUtil.getBeforeStartTime(nowTime, 29);
        dto.setStartTime(beforeThirtyDays);
        dto.setEndTime(nowTime);
        //查询进三十天信息
        List<SalesBaseVO> lastThirtyList = baseMapper.getShopLastDays(dto, settleRate);
        LocalDateTime beforeSevenDays = LocalDateUtil.getBeforeStartTime(nowTime, 7);

        dto.setStartTime(beforeSevenDays);
        dto.setEndTime(nowTime);
        //查询近七天信息
        List<SalesBaseVO> lastSevenList = baseMapper.getShopLastDays(dto, settleRate);
        lastSevenList = lastSevenList.stream().filter(s -> s.getSales() != null).collect(Collectors.toList());
        for (ShopSalesVO item : resultList) {
            List<SalesBaseVO> salesTrendList = new ArrayList<>(7);

            //近七天销售量
            Integer lastSevenDaysSalesQuantity = lastSevenList.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) && b.getFlagNo().equals(item.getShopNo())
                    ).mapToInt(SalesBaseVO::getSalesQuantity).sum();
            //近三十天销售量
            Integer lastThirtyDaysSalesQuantity = lastThirtyList.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) && b.getFlagNo().equals(item.getShopNo())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();
            item.setLastSevenDaysSalesQuantity(lastSevenDaysSalesQuantity);
            item.setLastThirtyDaysSalesQuantity(lastThirtyDaysSalesQuantity);

            Map<LocalDateTime, SalesBaseVO> dateMap = lastSevenList.stream().filter(salesBaseVO -> StringUtils.isNotBlank(salesBaseVO.getFlagNo()) && salesBaseVO.getFlagNo().equals(item.getSkuNo()))
                    .collect(Collectors.toMap(SalesBaseVO::getFlagDate, Function.identity()));
            for (int i = 6; i >= 0; i--) {
                LocalDate flagDay = nowDate.minus(i, ChronoUnit.DAYS);
                LocalDateTime startTime = LocalDateUtil.startLocalDateTime(flagDay);
                SalesBaseVO salesBaseVO = dateMap.get(startTime);
                if (Objects.isNull(salesBaseVO)){
                    salesBaseVO = new SalesBaseVO();
                    salesBaseVO.setSales(BigDecimal.ZERO);
                    salesBaseVO.setSalesQuantity(0);
                    salesBaseVO.setFlagDate(startTime);
                    salesBaseVO.setFlagNo(item.getSkuNo());
                }
                salesTrendList.add(salesBaseVO);
            }
            item.setSalesTrendList(salesTrendList);
        }
        return resultList;
    }


    /**
     * 一级销售模块 销售额TOP20店铺
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.ShopSalesVO>
     * @author yl
     * @date 2022-12-21 10:33
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byTopShop",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO byTopShop(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        String shopNo = "shopNo";
        StatisticalDataVO result = new StatisticalDataVO();
        result.setName("销售额TOP20店铺");
        result.setChartType(ChartType.BAR);
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<Map<String, Object>> resultList = baseMapper.byTopShop(dto, settleRate);
        List<String> shopNoList = resultList.stream().map(obj -> obj.get(shopNo).toString()).collect(Collectors.toList());
        List<BiShopInfoEntity> shopList = shopInfoService.getByShopNoList(shopNoList);
        for (Map<String, Object> item : resultList) {
            if (item.containsKey(shopNo)) {
                String shopNoFlag = item.get(shopNo).toString();
                String shopName = shopList.stream().filter(s -> s.getPlatformShopNo().equals(shopNoFlag)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                item.put("shopName", shopName);
            } else {
                item.put("shopName", "");
            }


        }

        ChartVO chartVO = new ChartVO();
        int initSize = CollectionUtils.isNotEmpty(resultList) ? resultList.size() : 10;
        List<Object> xAxisList = new ArrayList<>(initSize);
        List<SeriesVO<Object>> seriesList = new ArrayList<>(initSize);
        //只有一个柱子
        SeriesVO<Object> series = new SeriesVO();
        series.setName("店铺销售额");
        List<Object> dataList = new ArrayList<>(initSize);
        for (Map<String, Object> map : resultList) {
            dataList.add(map.get("sales"));
            xAxisList.add(map.get("shopName"));
        }
        series.setData(dataList);
        seriesList.add(series);
        chartVO.setXAxis(xAxisList);
        chartVO.setSeries(seriesList);
        result.setData(chartVO);
        return result;
    }

    /**
     * 二级销售模块 店铺-国家销售额
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesGroupVO>
     * @author yl
     * @date 2022-12-26 10:10
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byShopCountry",keyGenerator = "myKeyGenerator")
    public XyAxesResultVO byShopCountry(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        XyAxesResultVO result = new XyAxesResultVO();
        List<ShopSalesVO> list = baseMapper.byShopCountry(dto, settleRate);
        //以店铺
        Map<String, List<ShopSalesVO>> groupShopMap = list.parallelStream().
                collect(Collectors.groupingBy(ShopSalesVO::getShopNo));

        //以国家
        Map<String, List<ShopSalesVO>> groupCountryMap = list.parallelStream().
                collect(Collectors.groupingBy(ShopSalesVO::getPlatformName));
        int countryMapSize = groupCountryMap.size();
        //列名
        List<XAxesVO> columnList = new LinkedList<>();
        XAxesVO shopAxes = new XAxesVO();
        shopAxes.setProp("name");
        shopAxes.setLabel("店铺名称");
        columnList.add(shopAxes);
        List<String> countryNameList = new ArrayList<>(countryMapSize);
        for (Map.Entry<String, List<ShopSalesVO>> item : groupCountryMap.entrySet()) {
            XAxesVO axes = new XAxesVO();
            String country = item.getKey();
            axes.setProp(country);
            axes.setLabel(country);
            columnList.add(axes);
            countryNameList.add(country);
        }

        List<Map<String, Object>> rowAxesList = new LinkedList<>();

        for (Map.Entry<String, List<ShopSalesVO>> item : groupShopMap.entrySet()) {
            Map<String, Object> rowAxes = new LinkedHashMap<>();
            List<ShopSalesVO> shopSalesList = item.getValue();
            String shopName = shopSalesList.get(0).getShopName();
            rowAxes.put("name", shopName);
            for (String country : countryNameList) {
                BigDecimal value = BigDecimal.ZERO;
                ShopSalesVO shopSales = shopSalesList.stream().
                        filter(s -> s.getPlatformName().equals(country)).
                        findFirst().orElse(null);
                if (shopSales != null) {
                    value = shopSales.getSales();
                }
                rowAxes.put(country, value);
            }

            rowAxesList.add(rowAxes);
        }
        result.setColumnList(columnList);
        result.setRowList(rowAxesList);
        return result;
    }

    /**
     * 二级销售模块 店铺-品类销售额
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesGroupVO>
     * @author yl
     * @date 2022-12-26 10:10
     */

    @Override
    @Cacheable(cacheNames = "cache:bi:byShopCategory",keyGenerator = "myKeyGenerator")
    public XyAxesResultVO byShopCategory(BiFilterDTO dto) {

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        XyAxesResultVO result = new XyAxesResultVO();
        //查询sku 分类以及分类下对应的skuno
        List<SkuCategoryVO> skuCategoryList = productDetailService.getSkuCategoryList(Collections.emptyList());
        int skuCategorySize = skuCategoryList.size();
        //列名
        List<XAxesVO> columnList = new ArrayList<>(skuCategorySize + 1);
        XAxesVO shopAxes = new XAxesVO();
        shopAxes.setProp("name");
        shopAxes.setLabel("店铺名称");
        columnList.add(shopAxes);

        for (SkuCategoryVO category : skuCategoryList) {
            XAxesVO axes = new XAxesVO();
            String categoryName = category.getName();
            axes.setProp(categoryName);
            axes.setLabel(categoryName);
            columnList.add(axes);
        }

        List<ShopSalesVO> list = baseMapper.byShopCategory(dto, settleRate);
        Map<String, List<ShopSalesVO>> groupMap = list.parallelStream()
                        .filter(e -> StringUtils.isNotEmpty(e.getShopName()))
                        .collect(Collectors.groupingBy(ShopSalesVO::getShopName));
        int initSize = groupMap.size();

        List<Map<String, Object>> rowAxesList = new ArrayList<>(initSize);

        for (Map.Entry<String, List<ShopSalesVO>> item : groupMap.entrySet()) {
            Map<String, Object> rowMap = new HashMap<>();

            List<ShopSalesVO> shopSalesList = item.getValue();
            String shopName = shopSalesList.get(0).getShopName();
            rowMap.put("name", shopName);
            for (SkuCategoryVO category : skuCategoryList) {
                List<String> skuList = category.getSkuList();
                BigDecimal sales = shopSalesList.stream().
                        filter(s -> skuList.contains(s.getSkuNo()) && s.getSales() != null).
                        map(ShopSalesVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                rowMap.put(category.getName(), sales);
            }
            rowAxesList.add(rowMap);
        }
        result.setColumnList(columnList);
        result.setRowList(rowAxesList);
        return result;
    }

    /**
     * 一级模块 - 品牌销售额
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesCountVO>
     * @author yl
     * @date 2022-12-27 9:09
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byBrand",keyGenerator = "myKeyGenerator")
    public List<SalesCountVO> byBrand(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        //品牌的话 根据sku 分了
        List<SalesCountVO> list = baseMapper.byBrand(dto, settleRate);

        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        List<SalesCountVO> chainList = baseMapper.byBrand(dto, settleRate);

        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateUtil.getStartTime(startTime.minusYears(1));
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateUtil.getEndTime(endTime.minusYears(1));
        dto.setStartTime(yearBasisStartTime);
        // 因为
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.toLocalDate(), LocalTime.MIN);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesCountVO> yearBasisList = baseMapper.byBrand(dto, settleRate);
        //总的销售额
        BigDecimal totalSales = list.stream().
                filter(s -> s.getSales() != null).
                map(SalesCountVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        for (SalesCountVO item : list) {
            BigDecimal brandSales = item.getSales();
            item.setSalesRatio(getSalesRatio(totalSales, brandSales));
            //环比
            BigDecimal brandChainSales = chainList.stream().filter(s -> s.getName().equals(item.getName())).
                    map(SalesCountVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);

            item.setChainRelativeRatio(getChainRelativeRatio(brandSales, brandChainSales));
            //同比
            BigDecimal brandYearBasisSales = yearBasisList.stream().filter(s -> s.getName().equals(item.getName())).
                    map(SalesCountVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setYearBasisRatio(getChainRelativeRatio(brandSales, brandYearBasisSales));

        }

        return list;
    }


    /**
     * 一级模块-平台销售额
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byPlatform",keyGenerator = "myKeyGenerator")
    public List<SalesCountVO> byPlatform(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<SalesCountVO> list = baseMapper.byPlatform(dto, settleRate);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        List<SalesCountVO> chainList = baseMapper.byPlatform(dto, settleRate);

        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateUtil.getStartTime(startTime.minusYears(1));
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateUtil.getEndTime(endTime.minusYears(1));
        // 因为get
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.toLocalDate(), LocalTime.MIN);
        dto.setStartTime(yearBasisStartTime);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesCountVO> yearBasisList = baseMapper.byPlatform(dto, settleRate);

        //总的销售额
        BigDecimal totalSales = list.stream().
                filter(b -> b.getSales() != null).
                map(SalesCountVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        for (SalesCountVO item : list) {
            String name = item.getName();
            BigDecimal sales = item.getSales();
            item.setSalesRatio(getSalesRatio(totalSales, item.getSales()));
            SalesCountVO chainVO = chainList.stream().filter(c -> c.getName().equals(name))
                    .findFirst().orElse(null);
            if (chainVO != null) {
                item.setChainRelativeRatio(getChainRelativeRatio(sales, chainVO.getSales()));
            }
            SalesCountVO yearBasisVO = yearBasisList.stream().filter(c -> c.getName().equals(name))
                    .findFirst().orElse(null);
            if (yearBasisVO != null) {
                item.setYearBasisRatio(getChainRelativeRatio(sales, yearBasisVO.getSales()));
            }
        }

        return list;
    }


    /**
     * 一级模块  国内/外销售占比
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2022-12-27 10:07
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byHomeAndAbroad",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO byHomeAndAbroad(BiFilterDTO dto) {

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        String cn = BiConstant.CN;
        List<BiShopInfoEntity> shopInfoList=shopInfoService.listByStoreSign();
        List<String> cnShopNoList=shopInfoList.stream().filter(s->cn.equals(s.getStoreSign())).
                map(BiShopInfoEntity::getPlatformShopNo).collect(Collectors.toList());

        List<SalesCountVO> resultList = baseMapper.byHomeAndAbroad(dto, settleRate);
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("国内外销售额占比");
        statistical.setChartType(ChartType.PIE);
        ChartVO chartVO = new ChartVO();
        chartVO.setXAxis(new ArrayList<>());
        List<SeriesVO<Object>> seriesList = new ArrayList<>(5);
        SeriesVO<Object> series = new SeriesVO();
        List<Object> list = new ArrayList<>();
        //国内
        Map<String, Object> chinaMap = new HashMap();
        chinaMap.put("name", "国内");
        BigDecimal chinaSales = resultList.stream().
                filter(s -> cnShopNoList.contains(s.getName())).
                map(SalesCountVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);

        chinaMap.put("value", chinaSales);
        list.add(chinaMap);
        //国外
        Map<String, Object> abroadMap = new HashMap();
        abroadMap.put("name", "国外");
        BigDecimal abroadSales = resultList.stream().
                filter(s -> !cnShopNoList.contains(s.getName())).
                map(SalesCountVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
        abroadMap.put("value", abroadSales);
        list.add(abroadMap);
        series.setData(list);
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        statistical.setData(chartVO);
        return statistical;

    }


    /**
     * 一级模块 人员销售额
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byPeople",keyGenerator = "myKeyGenerator")
    public List<SalesCountVO> byPeople(BiFilterDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        dto.setEndTime(endTime, 1);
        List<SalesBaseVO> list = baseMapper.byPeople(dto, settleRate);
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        if (StringUtils.isBlank(dto.getDateType())){
            dto.setDateType(DateTypeEnum.DAY.getType());
        }
        //这个是环比的查询出来的
        List<SalesBaseVO> chainList = baseMapper.byPeople(dto, settleRate);


        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateUtil.getStartTime(startTime.minusYears(1));
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateUtil.getEndTime(endTime.minusYears(1));
        dto.setStartTime(yearBasisStartTime);
        // 因为get 加一天
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.toLocalDate(), LocalTime.MIN);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesBaseVO> yearBasisList = baseMapper.byPeople(dto, settleRate);

        List<SalesCountVO> resultList = new ArrayList<>(list.size());
        //总的
        BigDecimal totalSales = list.stream().
                filter(b -> b.getSales() != null).
                map(SalesBaseVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        for (SalesBaseVO item : list) {
            String flagNo = item.getFlagNo();
            SalesCountVO vo = new SalesCountVO();
            BigDecimal sales = item.getSales();

            vo.setSales(sales);
            vo.setSalesRatio(getSalesRatio(totalSales, item.getSales()));

            SalesBaseVO chainVO = chainList.stream().filter(c -> c.getFlagNo().equals(flagNo))
                    .findFirst().orElse(null);
            if (chainVO != null) {
                vo.setChainRelativeRatio(getChainRelativeRatio(sales, chainVO.getSales()));
            }

            SalesBaseVO yearBasisVO = yearBasisList.stream().filter(c -> c.getFlagNo().equals(flagNo))
                    .findFirst().orElse(null);
            if (yearBasisVO != null) {
                vo.setYearBasisRatio(getChainRelativeRatio(sales, yearBasisVO.getSales()));
            }

            vo.setOrderCount(item.getOrderCount());
            vo.setSalesQuantity(item.getSalesQuantity());
            FindUserDTO userInfo = userList.stream().filter(u -> u.getUserId().equals(flagNo)).
                    findFirst().orElse(null);
            if (userInfo != null) {
                vo.setName(userInfo.getUserName());
            } else {
                vo.setName("无");
            }
            resultList.add(vo);
        }


        return resultList;
    }


    /**
     * 二级销售模块 店铺的新/老品销售额
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesGroupVO>
     * @author yl
     * @date 2022-12-26 10:10
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byShopNewAndOld",keyGenerator = "myKeyGenerator")
    public List<ShopNewAndOldSalesVO> byShopNewAndOld(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<ShopSalesVO> shopSalesList = baseMapper.byShopNewAndOld(dto, settleRate);

        List<String> shopNoList = shopSalesList.stream().map(ShopSalesVO::getShopNo).collect(Collectors.toList());

        List<BiShopInfoEntity> shopList = shopInfoService.getByShopNoList(shopNoList);

        int initSize = shopSalesList.size();
        List<ShopNewAndOldSalesVO> resultList = new ArrayList<>(initSize);
        Map<String, List<ShopSalesVO>> groupMap = shopSalesList.parallelStream().
                collect(Collectors.groupingBy(ShopSalesVO::getShopNo));
        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;
        for (Map.Entry<String, List<ShopSalesVO>> item : groupMap.entrySet()) {
            ShopNewAndOldSalesVO vo = new ShopNewAndOldSalesVO();
            List<ShopSalesVO> salesList = item.getValue();
            ShopSalesVO newItem = salesList.stream().filter(s -> s.getFlag().
                    equals(newFlag)).findFirst().orElse(null);
            ShopSalesVO oldItem = salesList.stream().filter(s -> s.getFlag().
                    equals(oldFlag)).findFirst().orElse(null);
            if (newItem != null) {
                vo.setNewSales(newItem.getSales());
                vo.setNewSalesQuantity(newItem.getSalesQuantity());
            }
            if (oldItem != null) {
                vo.setOldSales(oldItem.getSales());
                vo.setOldSalesQuantity(oldItem.getSalesQuantity());
            }
            String shopName = shopList.stream().filter(s -> s.getPlatformShopNo().equals(item.getKey())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            vo.setShopName(shopName);
            resultList.add(vo);
        }
        resultList.sort(Comparator.comparing(ShopNewAndOldSalesVO::getOldSales, Comparator.reverseOrder()));
        return resultList;

    }


    /**
     * 一级模块  国家销售额
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byCountry",keyGenerator = "myKeyGenerator")
    public List<SalesCountVO> byCountry(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<SalesCountVO> resultList = baseMapper.byCountry(dto, settleRate);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);

        //这个是环比的查询出来的
        List<SalesCountVO> chainList = baseMapper.byCountry(dto, settleRate);

        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateTime.of(startTime.minusYears(1).toLocalDate(), LocalTime.MIN);
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateTime.of(endTime.minusYears(1).toLocalDate(), LocalTime.MIN);
        dto.setStartTime(yearBasisStartTime);

        // 因为
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.toLocalDate(), LocalTime.MIN);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesCountVO> yearBasisList = baseMapper.byCountry(dto, settleRate);

        //总的
        BigDecimal totalSales = resultList.stream().
                filter(s -> s.getSales() != null).
                map(SalesCountVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        for (SalesCountVO item : resultList) {
            String name = item.getName();
            BigDecimal sales = item.getSales();
            item.setSalesRatio(getSalesRatio(totalSales, item.getSales()));
            SalesCountVO chainVO = chainList.stream().filter(c -> c.getName().equals(name))
                    .findFirst().orElse(null);
            if (chainVO != null) {
                item.setChainRelativeRatio(getChainRelativeRatio(sales, chainVO.getSales()));
            }
            SalesCountVO yearBasisVO = yearBasisList.stream().filter(c -> c.getName().equals(name))
                    .findFirst().orElse(null);
            if (yearBasisVO != null) {
                item.setYearBasisRatio(getChainRelativeRatio(sales, yearBasisVO.getSales()));
            }
        }
        return resultList;
    }


    /**
     * 获取销售占比
     *
     * @return
     */
    public BigDecimal getSalesRatio(BigDecimal totalSales, BigDecimal sales) {
        BigDecimal zero = BigDecimal.ZERO;
        if (totalSales.compareTo(zero) == 0 && sales.compareTo(zero) > 0){
            return BigDecimal.valueOf(100);
        }
        if (totalSales.compareTo(zero) == 0 || sales == null) {
            return zero;
        }
        BigDecimal ratio = sales.divide(totalSales, 5, BigDecimal.ROUND_HALF_EVEN);
        return ratio.multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取销售环比
     * 本期销售额-同期销售额/同期销售额 *100
     *
     * @return
     */
    public BigDecimal getChainRelativeRatio(BigDecimal sales, BigDecimal oldSales) {
        BigDecimal zero = BigDecimal.ZERO;
        if (oldSales == null || oldSales.compareTo(zero) == 0 || sales == null || oldSales == null) {
            return zero;
        }
        BigDecimal differ = sales.subtract(oldSales);

        BigDecimal ratio = differ.divide(oldSales, 4, BigDecimal.ROUND_HALF_UP);
        return ratio.multiply(new BigDecimal("100")).setScale(4, BigDecimal.ROUND_HALF_UP);
    }


    /**
     * 一级模块  品类销售额
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byCategory",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO byCategory(BiCategoryDTO.FirstCategoryParamsDTO dto) {
        //获取到一级类目列表
        List<BasicCategoryDTO> categoryList = plmTaskFeign.listCategoryTree();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        if(StringUtils.isBlank(dto.getDateType())){
            dto.setDateType(DateTypeEnum.MONTH.getType());
        }
        List<SalesBaseVO> list = baseMapper.byCategory(dto, settleRate);
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setChartType(ChartType.BAR);
        statistical.setName("销售品类排行");
        ChartVO chart = new ChartVO();
//        List<String> xAxisList = categoryList.stream().map(BasicCategoryDTO::getName).collect(Collectors.toList());
        List<SeriesVO<Object>> seriesList = new ArrayList<>(10);
        //只有一个柱子
        SeriesVO<Object> series = new SeriesVO();
        series.setName("品类销售额");
//        List<Object> dataList = new ArrayList<>(10);
        List<BasicDTO> dtos = new ArrayList<>(10);
        for (BasicCategoryDTO item : categoryList) {
            BasicDTO basicDTO = new BasicDTO();
            basicDTO.setName(item.getName());
            List<BasicCategoryDTO> childrenList = item.getChildrenList();
            if(CollectionUtils.isNotEmpty(childrenList)){
                List<String> categoryIdList = childrenList.stream().map(BasicCategoryDTO::getId).collect(Collectors.toList());
                BigDecimal totalSales = list.stream().filter(
                                s -> categoryIdList.contains(s.getFlagNo()) && s.getSales() != null
                        ).map(SalesBaseVO::getSales).
                        reduce(BigDecimal.ZERO, BigDecimal::add);
                basicDTO.setSales(totalSales);
//                dataList.add(totalSales);
            }else {
                basicDTO.setSales(BigDecimal.ZERO);
//                dataList.add(BigDecimal.ZERO);
            }
            dtos.add(basicDTO);
        }
        dtos.sort(Comparator.comparing(BasicDTO::getSales).reversed());
        series.setData(dtos.stream().map(BasicDTO::getSales).collect(Collectors.toList()));
        seriesList.add(series);
        chart.setXAxis(dtos.stream().map(BasicDTO::getName).collect(Collectors.toList()));
        chart.setSeries(seriesList);
        statistical.setData(chart);
        return statistical;
    }


    /**
     * 销售相关  一级模块 人员周排行榜
     *
     * @param
     * @return java.util.List<com.erp.model.bi.vo.PeopleSalesRankVO>
     * @author yl
     * @date 2022-12-27 11:05
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byPeopleWeekRank",keyGenerator = "myKeyGenerator")
    public List<PeopleSalesRankVO> byPeopleWeekRank(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<PeopleSalesRankVO> resultList = new ArrayList<>(10);
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        LocalDate nowDate = LocalDate.now();
        //本周开始时间
        LocalDateTime weekStart = LocalDateUtil.getThisWeekStart(nowDate);
        //本周结束时间
        LocalDateTime weekEnd = LocalDateUtil.getThisWeekEnd(nowDate);
        dto.setStartTime(weekStart);
        dto.setEndTime(weekEnd);
        if(StringUtils.isBlank(dto.getDateType())){
            dto.setDateType(DateTypeEnum.WEEK.getType());
        }
        //本周结果
        List<SalesBaseVO> list = baseMapper.byPeopleRank(dto, settleRate);

        //上周开始时间
        LocalDateTime lastWeekStart = LocalDateUtil.getLastWeekStart(nowDate);
        //上周结束时间
        LocalDateTime lastWeekEnd = LocalDateUtil.getLastWeekEnd(nowDate);
        dto.setStartTime(lastWeekStart);
        dto.setEndTime(lastWeekEnd);
        //上周查询结果
        List<SalesBaseVO> lastList = baseMapper.byPeopleRank(dto, settleRate);
        int cutSize = list.size() >= 10 ? 10 : list.size();
        //取前十
        list = list.subList(0, cutSize);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            SalesBaseVO base = list.get(i);
            String flagNo = base.getFlagNo();
            PeopleSalesRankVO vo = new PeopleSalesRankVO();
            vo.setRanking(i + 1);
            BigDecimal weekSales = base.getSales();
            vo.setSales(weekSales);
            FindUserDTO userInfo = userList.stream().filter(u -> u.getUserId().equals(flagNo)).
                    findFirst().orElse(null);
            if (userInfo != null) {
                vo.setUserName(userInfo.getUserName());
            }
            SalesBaseVO last = lastList.stream().filter(l -> l.getFlagNo().equals(flagNo)).
                    findFirst().orElse(null);
            if (last != null) {
                int lastRanking = lastList.indexOf(last) + 1;
                vo.setLastRanking(lastRanking);
                BigDecimal lastWeekSales = last.getSales();
                vo.setChainRelativeRatio(getChainRelativeRatio(weekSales, lastWeekSales));
            }

            resultList.add(vo);
        }

        return resultList;
    }

    /**
     * 销售相关  一级模块 人员 月排行榜
     *
     * @param
     * @return java.util.List<com.erp.model.bi.vo.PeopleSalesRankVO>
     * @author yl
     * @date 2022-12-27 11:05
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byPeopleMonthRank",keyGenerator = "myKeyGenerator")
    public List<PeopleSalesRankVO> byPeopleMonthRank(BiFilterDTO dto) {
        List<PeopleSalesRankVO> resultList = new ArrayList<>(10);
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());

        List<FindUserDTO> userList = sysUserFeign.getUserList();
        LocalDate nowDate = LocalDate.now();
        //本月开始时间
        LocalDateTime monthStart = LocalDateUtil.getThisMonthStart(nowDate);
        //本月结束时间
        LocalDateTime monthEnd = LocalDateUtil.getThisMonthEnd(nowDate);
        dto.setStartTime(monthStart);
        dto.setEndTime(monthEnd);
        if (StringUtils.isBlank(dto.getDateType())){
            dto.setDateType(DateTypeEnum.WEEK.getType());
        }
        //本月结果
        List<SalesBaseVO> list = baseMapper.byPeopleRank(dto, settleRate);

        //上月开始时间
        LocalDateTime lastMonthStart = LocalDateUtil.getLastMonthStart(nowDate);
        //上月结束时间
        LocalDateTime lastMonthEnd = LocalDateUtil.getLastMonthEnd(nowDate);
        dto.setStartTime(lastMonthStart);
        dto.setEndTime(lastMonthEnd);
        //上月查询结果
        List<SalesBaseVO> lastList = baseMapper.byPeopleRank(dto, settleRate);
        int cutSize = list.size() >= 10 ? 10 : list.size();
        //取前十
        list = list.subList(0, cutSize);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            SalesBaseVO base = list.get(i);
            String flagNo = base.getFlagNo();
            PeopleSalesRankVO vo = new PeopleSalesRankVO();
            vo.setRanking(i + 1);
            BigDecimal monthSales = base.getSales();
            vo.setSales(monthSales);
            FindUserDTO userInfo = userList.stream().filter(u -> u.getUserId().equals(flagNo)).
                    findFirst().orElse(null);
            if (userInfo != null) {
                vo.setUserName(userInfo.getUserName());
            }
            SalesBaseVO last = lastList.stream().filter(l -> l.getFlagNo().equals(flagNo)).
                    findFirst().orElse(null);
            if (last != null) {
                int lastRanking = lastList.indexOf(last) + 1;
                vo.setLastRanking(lastRanking);
                BigDecimal lastMonthSales = last.getSales();
                vo.setChainRelativeRatio(getChainRelativeRatio(monthSales, lastMonthSales));
            }

            resultList.add(vo);
        }

        return resultList;
    }


    /**
     * 销售相关  一级模块 人员 季度排行榜
     *
     * @param
     * @return java.util.List<com.erp.model.bi.vo.PeopleSalesRankVO>
     * @author yl
     * @date 2022-12-27 11:05
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byPeopleQuarterRank",keyGenerator = "myKeyGenerator")
    public List<PeopleSalesRankVO> byPeopleQuarterRank(BiFilterDTO dto) {
        List<PeopleSalesRankVO> resultList = new ArrayList<>(10);
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //获取汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDate nowDate = LocalDate.now();
        //本季度开始时间
        LocalDateTime quarterStart = LocalDateUtil.getThisQuarterStart(nowDate);
        //本季度结束时间
        LocalDateTime quarterEnd = LocalDateUtil.getThisQuarterEnd(nowDate);
        dto.setStartTime(quarterStart);
        dto.setEndTime(quarterEnd);
        if (StringUtils.isBlank(dto.getDateType())){
            dto.setDateType(DateTypeEnum.QUARTER.getType());
        }
        //本月结果
        List<SalesBaseVO> list = baseMapper.byPeopleRank(dto, settleRate);

        //上季度开始时间
        LocalDateTime lastQuarterStart = LocalDateUtil.getLastQuarterStart(nowDate);
        //上季度结束时间
        LocalDateTime lastQuarterEnd = LocalDateUtil.getLastQuarterEnd(nowDate);
        dto.setStartTime(lastQuarterStart);
        dto.setEndTime(lastQuarterEnd);
        //上季度查询结果
        List<SalesBaseVO> lastList = baseMapper.byPeopleRank(dto, settleRate);
        int cutSize = list.size() >= 10 ? 10 : list.size();
        //取前十
        list = list.subList(0, cutSize);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            SalesBaseVO base = list.get(i);
            String flagNo = base.getFlagNo();
            PeopleSalesRankVO vo = new PeopleSalesRankVO();
            vo.setRanking(i + 1);
            BigDecimal quarterSales = base.getSales();
            vo.setSales(quarterSales);
            FindUserDTO userInfo = userList.stream().filter(u -> u.getUserId().equals(flagNo)).
                    findFirst().orElse(null);
            if (userInfo != null) {
                vo.setUserName(userInfo.getUserName());
            }
            SalesBaseVO last = lastList.stream().filter(l -> l.getFlagNo().equals(flagNo)).
                    findFirst().orElse(null);
            if (last != null) {
                int lastRanking = lastList.indexOf(last) + 1;
                vo.setLastRanking(lastRanking);
                BigDecimal lastQuarterSales = last.getSales();
                vo.setChainRelativeRatio(getChainRelativeRatio(quarterSales, lastQuarterSales));
            }

            resultList.add(vo);
        }

        return resultList;
    }


    /**
     * 销售相关  一级模块 人员 年度排行榜
     *
     * @param
     * @return java.util.List<com.erp.model.bi.vo.PeopleSalesRankVO>
     * @author yl
     * @date 2022-12-27 11:05
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byPeopleYearRank",keyGenerator = "myKeyGenerator")
    public List<PeopleSalesRankVO> byPeopleYearRank(BiFilterDTO dto) {
        List<PeopleSalesRankVO> resultList = new ArrayList<>(10);
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        LocalDate nowDate = LocalDate.now();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        //本年度 开始时间
        LocalDateTime yearStart = LocalDateUtil.getThisYearStart(nowDate);
        //本年度 结束时间
        LocalDateTime yearEnd = LocalDateUtil.getThisYearEnd(nowDate);
        dto.setStartTime(yearStart);
        dto.setEndTime(yearEnd);
        if (StringUtils.isBlank(dto.getDateType())){
            dto.setDateType(DateTypeEnum.YEAR.getType());
        }
        //本年结果
        List<SalesBaseVO> list = baseMapper.byPeopleRank(dto, settleRate);

        //上年开始时间
        LocalDateTime lastYearStart = LocalDateUtil.getLastYearStart(nowDate);
        //上年结束时间
        LocalDateTime lastYearEnd = LocalDateUtil.getLastYearEnd(nowDate);
        dto.setStartTime(lastYearStart);
        dto.setEndTime(lastYearEnd);
        //上年查询结果
        List<SalesBaseVO> lastList = baseMapper.byPeopleRank(dto, settleRate);
        int cutSize = list.size() >= 10 ? 10 : list.size();
        //取前十
        list = list.subList(0, cutSize);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            SalesBaseVO base = list.get(i);
            String flagNo = base.getFlagNo();
            PeopleSalesRankVO vo = new PeopleSalesRankVO();
            vo.setRanking(i + 1);
            BigDecimal yearSales = base.getSales();
            vo.setSales(yearSales);
            FindUserDTO userInfo = userList.stream().filter(u -> u.getUserId().equals(flagNo)).
                    findFirst().orElse(null);
            if (userInfo != null) {
                vo.setUserName(userInfo.getUserName());
            }
            SalesBaseVO last = lastList.stream().filter(l -> l.getFlagNo().equals(flagNo)).
                    findFirst().orElse(null);
            if (last != null) {
                int lastRanking = lastList.indexOf(last) + 1;
                vo.setLastRanking(lastRanking);
                BigDecimal lastYearSales = last.getSales();
                vo.setChainRelativeRatio(getChainRelativeRatio(yearSales, lastYearSales));
            }

            resultList.add(vo);
        }

        return resultList;
    }


    /**
     * 一级模块  事业部销售额
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byDept",keyGenerator = "myKeyGenerator")
    public List<SalesCountVO> byDept(BiFilterDTO dto) {
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        dto.setEndTime(endTime, 1);
        List<SalesBaseVO> list = baseMapper.byDept(dto, settleRate);
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();

        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        //这个是环比的查询出来的
        List<SalesBaseVO> chainList = baseMapper.byDept(dto, settleRate);

        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateUtil.getStartTime(startTime.minusYears(1));
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateUtil.getEndTime(endTime.minusYears(1));
        dto.setStartTime(yearBasisStartTime);
        // 因为get 加一天 所以这里减一天
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.toLocalDate(), LocalTime.MIN);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesBaseVO> yearBasisList = baseMapper.byDept(dto, settleRate);

        List<SalesCountVO> resultList = new ArrayList<>(list.size());
        //总的
        BigDecimal totalSales = list.stream().
                filter(b -> b.getSales() != null).
                map(SalesBaseVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        for (SalesBaseVO item : list) {
            String flagNo = item.getFlagNo();

            SalesCountVO vo = new SalesCountVO();
            SysDepartmentDTO dept = deptList.stream().filter(u -> u.getId().equals(flagNo)).
                    findFirst().orElse(null);
            if (dept != null) {
                vo.setName(dept.getName());
            } else {
                vo.setName("无");
            }

            BigDecimal sales = item.getSales();
            vo.setSales(sales);
            vo.setSalesRatio(getSalesRatio(totalSales, item.getSales()));

            SalesBaseVO chainVO = chainList.stream().filter(c -> c.getFlagNo().equals(flagNo))
                    .findFirst().orElse(null);
            if (chainVO != null) {
                vo.setChainRelativeRatio(getChainRelativeRatio(sales, chainVO.getSales()));
            }

            SalesBaseVO yearBasisVO = yearBasisList.stream().filter(c -> c.getFlagNo().equals(flagNo))
                    .findFirst().orElse(null);
            if (yearBasisVO != null) {
                vo.setYearBasisRatio(getChainRelativeRatio(sales, yearBasisVO.getSales()));
            }
            vo.setOrderCount(item.getOrderCount());
            vo.setSalesQuantity(item.getSalesQuantity());
            resultList.add(vo);
        }
        return resultList;
    }


    /**
     * 二级级模块  事业部-新/老品
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byDeptNewAndOld",keyGenerator = "myKeyGenerator")
    public List<ProductNewAndOldVO> byDeptNewAndOld(BiFilterDTO dto) {
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<SalesFlagVO> list = baseMapper.byDeptNewAndOld(dto, settleRate);
        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;
        List<ProductNewAndOldVO> resultList = new ArrayList<>(list.size());
        Map<String, List<SalesFlagVO>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(SalesFlagVO::getName));

        for (Map.Entry<String, List<SalesFlagVO>> item : groupMap.entrySet()) {
            String deptId = item.getKey();
            List<SalesFlagVO> salesList = item.getValue();
            ProductNewAndOldVO vo = new ProductNewAndOldVO();
            SysDepartmentDTO dept = deptList.stream().filter(u -> u.getId().equals(deptId)).
                    findFirst().orElse(null);
            if (dept != null) {
                vo.setName(dept.getName());
            } else {
                vo.setName("无");
            }

            BigDecimal newProductSales = salesList.stream().
                    filter(s -> s.getFlag().equals(newFlag) && s.getSales() != null).
                    map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal oldProductSales = salesList.stream().
                    filter(s -> s.getFlag().equals(oldFlag) && s.getSales() != null).
                    map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setNewProductSales(newProductSales);
            vo.setOldProductSales(oldProductSales);
            Integer newSalesQuantity = salesList.stream().
                    filter(s -> s.getFlag().equals(newFlag) && s.getSalesQuantity() != null).
                    mapToInt(SalesFlagVO::getSalesQuantity).
                    sum();
            Integer oldSalesQuantity = salesList.stream().
                    filter(s -> s.getFlag().equals(oldFlag) && s.getSalesQuantity() != null).
                    mapToInt(SalesFlagVO::getSalesQuantity).
                    sum();
            vo.setNewSalesQuantity(newSalesQuantity);
            vo.setOldSalesQuantity(oldSalesQuantity);
            resultList.add(vo);
        }

        return resultList;
    }


    /**
     * 销售相关 一级模块  新/老品销售额
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byNewAndOld",keyGenerator = "myKeyGenerator")
    public List<SalesCountVO> byNewAndOld(BiFilterDTO dto) {

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<SalesCountVO> list = baseMapper.byNewAndOld(dto, settleRate);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        //这个是环比的查询出来的
        List<SalesCountVO> chainList = baseMapper.byNewAndOld(dto, settleRate);
        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateUtil.getStartTime(startTime.minusYears(1));
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateUtil.getEndTime(endTime.minusYears(1));
        dto.setStartTime(yearBasisStartTime);
        // 因为get 加一天 所以这里减一天
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.toLocalDate(), LocalTime.MIN);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesCountVO> yearBasisList = baseMapper.byNewAndOld(dto, settleRate);
        //总的
        BigDecimal totalSales = list.stream().
                filter(b -> b.getSales() != null).
                map(SalesCountVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        for (SalesCountVO item : list) {
            String name = item.getName();
            BigDecimal sales = item.getSales();
            item.setSalesRatio(getSalesRatio(totalSales, item.getSales()));
            SalesCountVO chainVO = chainList.stream().filter(c -> c.getName().equals(name))
                    .findFirst().orElse(null);
            if (chainVO != null) {
                item.setChainRelativeRatio(getChainRelativeRatio(sales, chainVO.getSales()));
            }
            SalesCountVO yearBasisVO = yearBasisList.stream().filter(c -> c.getName().equals(name))
                    .findFirst().orElse(null);
            if (yearBasisVO != null) {
                item.setYearBasisRatio(getChainRelativeRatio(sales, yearBasisVO.getSales()));
            }
        }
        return list;
    }


    /**
     * 销售相关 -各个平台新/老品销售额
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byPlatformNewAndOld",keyGenerator = "myKeyGenerator")
    public List<ProductNewAndOldVO> byPlatformNewAndOld(BiFilterDTO dto) {
        List<ProductNewAndOldVO> resultList = new ArrayList<>(10);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());

        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;

        dto.setEndTime(endTime, 1);
        List<SalesFlagVO> list = baseMapper.byPlatformNewAndOld(dto, settleRate);

        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        List<SalesFlagVO> chainList = baseMapper.byPlatformNewAndOld(dto, settleRate);

        Map<String, List<SalesFlagVO>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(SalesFlagVO::getName));

        for (Map.Entry<String, List<SalesFlagVO>> item : groupMap.entrySet()) {
            String name = item.getKey();
            List<SalesFlagVO> salesFlagList = item.getValue();
            ProductNewAndOldVO vo = new ProductNewAndOldVO();
            SalesFlagVO newItem = salesFlagList.stream().filter(s -> s.getFlag().
                    equals(newFlag)).findFirst().orElse(null);
            SalesFlagVO oldItem = salesFlagList.stream().filter(s -> s.getFlag().
                    equals(oldFlag)).findFirst().orElse(null);
            vo.setName(name);
            if (newItem != null) {
                vo.setNewProductSales(newItem.getSales());
                vo.setNewSalesQuantity(newItem.getSalesQuantity());

                BigDecimal newChainSales = chainList.stream().
                        filter(c -> name.equals(c.getName())
                                && c.getFlag().equals(newFlag)
                                && c.getSales() != null
                        ).
                        map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                vo.setNewProductChainRelativeRatio(getChainRelativeRatio(newItem.getSales(), newChainSales));
            }
            if (oldItem != null) {
                vo.setOldProductSales(oldItem.getSales());
                vo.setOldSalesQuantity(oldItem.getSalesQuantity());
                BigDecimal oldChainSales = chainList.stream().
                        filter(
                                c -> name.equals(c.getName())
                                        && c.getFlag().equals(oldFlag)
                                        && c.getSales() != null
                        ).
                        map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                vo.setOldProductChainRelativeRatio(getChainRelativeRatio(oldItem.getSales(), oldChainSales));
            }
            resultList.add(vo);
        }

        resultList.sort(Comparator.comparing(ProductNewAndOldVO::getOldProductSales, Comparator.reverseOrder()));
        return resultList;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:byPeopleNewAndOld",keyGenerator = "myKeyGenerator")
    public List<ProductNewAndOldVO> byPeopleNewAndOld(BiFilterDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<SalesFlagVO> list = baseMapper.byPeopleNewAndOld(dto, settleRate);

        Map<String, List<SalesFlagVO>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(SalesFlagVO::getName));
        List<ProductNewAndOldVO> resultList = new ArrayList<>(groupMap.size());

        for (Map.Entry<String, List<SalesFlagVO>> item : groupMap.entrySet()) {
            String name = item.getKey();
            List<SalesFlagVO> salesFlagList = item.getValue();
            ProductNewAndOldVO vo = new ProductNewAndOldVO();
            FindUserDTO userInfo = userList.stream().filter(u -> u.getUserId().equals(name)).
                    findFirst().orElse(null);
            if (userInfo != null) {
                vo.setName(userInfo.getUserName());
            }

            BigDecimal newItemSales = salesFlagList.stream().
                    filter(s -> s.getFlag().
                            equals(newFlag)
                            && s.getSales() != null).map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer newItemSalesQuantity = salesFlagList.stream().
                    filter(s -> s.getFlag().equals(newFlag)).
                    mapToInt(SalesFlagVO::getSalesQuantity).sum();
            vo.setNewProductSales(newItemSales);
            vo.setNewSalesQuantity(newItemSalesQuantity);
            BigDecimal oldItemSales = salesFlagList.stream().
                    filter(s -> s.getFlag().equals(oldFlag) && s.getSales() != null).
                    map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setOldProductSales(oldItemSales);
            Integer oldItemSalesQuantity = salesFlagList.stream().
                    filter(s -> s.getFlag().equals(oldFlag)).
                    mapToInt(SalesFlagVO::getSalesQuantity).sum();
            vo.setOldSalesQuantity(oldItemSalesQuantity);
            resultList.add(vo);
        }
        return resultList;
    }


    /**
     * 销售相关 各个品类新/老品销售额
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.ProductNewAndOldVO>
     * @author yl
     * @date 2022-12-27 16:53
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byCategoryNewAndOld",keyGenerator = "myKeyGenerator")
    public List<ProductNewAndOldVO> byCategoryNewAndOld(BiFilterDTO dto) {
        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;


        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<ProductNewAndOldVO> resultList = new ArrayList<>(10);
        //查询sku 分类以及分类下对应的skuno
        List<SkuCategoryVO> skuCategoryList = productDetailService.getSkuCategoryList(Collections.emptyList());
        List<SalesFlagVO> list = baseMapper.byCategoryNewAndOld(dto, settleRate);
        for (SkuCategoryVO item : skuCategoryList) {
            ProductNewAndOldVO vo = new ProductNewAndOldVO();
            vo.setName(item.getName());
            List<String> skuList = item.getSkuList();
            BigDecimal newProductSales = list.stream()
                    .filter(s -> skuList.contains(s.getSkuNo()) &&
                            newFlag.equals(s.getFlag()) &&
                            s.getSales() != null
                    ).map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setNewProductSales(newProductSales);
            Integer newSalesQuantity = list.stream()
                    .filter(s -> skuList.contains(s.getSkuNo()) &&
                            newFlag.equals(s.getFlag())
                    ).mapToInt(SalesFlagVO::getSalesQuantity).sum();
            vo.setNewSalesQuantity(newSalesQuantity);

            BigDecimal oldProductSales = list.stream()
                    .filter(s -> skuList.contains(s.getSkuNo()) &&
                            oldFlag.equals(s.getFlag()) &&
                            s.getSales() != null
                    ).map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setOldProductSales(oldProductSales);
            Integer oldSalesQuantity = list.stream()
                    .filter(s -> skuList.contains(s.getSkuNo()) && oldFlag.equals(s.getFlag()))
                    .mapToInt(SalesFlagVO::getSalesQuantity).sum();
            vo.setOldSalesQuantity(oldSalesQuantity);
            resultList.add(vo);
        }
        return resultList;
    }


    /**
     * 一级模块 -站点销售额
     *
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:bySite",keyGenerator = "myKeyGenerator")
    public List<SalesCountVO> bySite(BiFilterDTO dto) {
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        List<ShopSalesVO> list = baseMapper.bySite(dto, settleRate);
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);

        //这个是环比的查询出来的
        List<ShopSalesVO> chainList = baseMapper.bySite(dto, settleRate);

        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateUtil.getStartTime(startTime.minusYears(1));
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateUtil.getEndTime(endTime.minusYears(1));
        dto.setStartTime(yearBasisStartTime);
        // 因为get 加一天 所以这里减一天
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.toLocalDate(), LocalTime.MIN);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<ShopSalesVO> yearBasisList = baseMapper.bySite(dto, settleRate);

        //总的
        BigDecimal totalSales = list.stream().
                filter(s -> s.getSales() != null).
                map(ShopSalesVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ShopSiteVO> shopCategoryList = shopInfoService.getShopCategoryList();

        List<SalesCountVO> resultList = new ArrayList<>(shopCategoryList.size());
        for (ShopSiteVO item : shopCategoryList) {
            SalesCountVO vo = new SalesCountVO();
            //对应的店铺信息
            List<String> shopNameList = item.getShopName();
            String site = item.getSite();
            vo.setName(site);
            BigDecimal sales = list.stream().
                    filter(s -> shopNameList.contains(s.getShopName()) && s.getSales() != null).
                    map(ShopSalesVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);

            vo.setSalesRatio(getSalesRatio(totalSales, sales));
            vo.setSales(sales);
            Integer salesQuantity = list.stream().
                    filter(s -> shopNameList.contains(s.getShopName())).
                    mapToInt(ShopSalesVO::getSalesQuantity).
                    sum();
            vo.setSalesQuantity(salesQuantity);

            Integer orderCount = list.stream().
                    filter(s -> shopNameList.contains(s.getShopName())).
                    mapToInt(ShopSalesVO::getOrderCount).
                    sum();
            vo.setOrderCount(orderCount);

            BigDecimal chainSales = chainList.stream().
                    filter(c -> shopNameList.contains(c.getShopName()) && c.getSales() != null).
                    map(ShopSalesVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);

            vo.setChainRelativeRatio(getChainRelativeRatio(sales, chainSales));

            BigDecimal yearBasisSales = yearBasisList.stream().
                    filter(c -> shopNameList.contains(c.getShopName()) && c.getSales() != null).
                    map(ShopSalesVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setYearBasisRatio(getChainRelativeRatio(sales, yearBasisSales));
            resultList.add(vo);
        }
        return resultList;
    }

    /**
     * 一级模块 -新品自研，外采贡献分析
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2022-12-28 9:18
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byProductType",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO byProductType(BiFilterDTO dto) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        if (StringUtils.isBlank(dto.getDateType())){
            dto.setDateType(DateTypeEnum.MONTH.getType());
        }
        List<SalesBaseVO> list = baseMapper.byCategory((BiCategoryDTO.FirstCategoryParamsDTO) dto, settleRate);
        //获取到sku 属性分类
        List<SkuCategoryVO> itemPropertyList = productDetailService.getSkuPropertyList();
        //自研
        List<SkuCategoryVO> homemadeList = itemPropertyList.stream().
                filter(s -> BiConstant.HOMEMADE.equals(s.getName())).
                collect(Collectors.toList());
        List<String> skuHomemadeList = homemadeList.stream().
                flatMap(s -> s.getSkuList().stream()).collect(Collectors.toList());

        //外采
        List<SkuCategoryVO> purchaseList = itemPropertyList.stream().
                filter(s -> BiConstant.PURCHASE.equals(s.getName())).
                collect(Collectors.toList());

        List<String> skuPurchaseList = purchaseList.stream().
                flatMap(s -> s.getSkuList().stream()).collect(Collectors.toList());

        statistical.setName("新品自研/外采贡献分析");
        statistical.setChartType(ChartType.PIE);
        ChartVO chartVO = new ChartVO();
        List<SeriesVO<Object>> seriesList = new ArrayList<>(10);
        SeriesVO<Object> series = new SeriesVO();
        series.setName("销售额");
        List<Object> dataList = new ArrayList<>();
        // BiConstant.HOMEMADE
        //自研
        Map<String, Object> homemadeMap = new HashMap<>();
        homemadeMap.put("name", "自研");
        BigDecimal homemadeSales = list.stream().
                filter(s -> skuHomemadeList.contains(s.getFlagNo()) && s.getSales() != null).
                map(SalesBaseVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        homemadeMap.put("value", homemadeSales);
        dataList.add(homemadeMap);

        //外采
        Map<String, Object> purchaseMap = new HashMap<>();
        purchaseMap.put("name", "外采");
        BigDecimal purchaseSales = list.stream().
                filter(s -> skuPurchaseList.contains(s.getFlagNo()) && s.getSales() != null).
                map(SalesBaseVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        purchaseMap.put("value", purchaseSales);
        dataList.add(purchaseMap);
        series.setData(dataList);
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        chartVO.setXAxis(Arrays.asList("自研", "外采"));
        statistical.setData(chartVO);
        return statistical;
    }

    /**
     * 一级模块 销售额TOP20老品
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2022-12-28 9:24
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byOldProductTop",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO byOldProductTop(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        dto.setNewSign(0);
        List<SalesBaseVO> list = baseMapper.byProductTop(dto, settleRate);
        int initSize = CollectionUtils.isNotEmpty(list) ? list.size() : 10;
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("销售额TOP20% 老品");
        statistical.setChartType(ChartType.BAR);
        ChartVO chart = new ChartVO();
        List<Object> xAxisList = new ArrayList<>(initSize);
        List<SeriesVO<Object>> seriesList = new ArrayList<>(initSize);
        //只有一个柱子
        SeriesVO<Object> series = new SeriesVO();
        series.setName("销售额");
        List<Object> dataList = new ArrayList<>(initSize);
        for (SalesBaseVO item : list) {
            dataList.add(item.getSales());
            xAxisList.add(item.getFlagNo());
        }
        series.setData(dataList);
        seriesList.add(series);
        chart.setXAxis(xAxisList);
        chart.setSeries(seriesList);
        statistical.setData(chart);
        return statistical;

    }


    /**
     * 一级模块 销售额TOP20新品
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2022-12-28 9:24
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byNewProductTop",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO byNewProductTop(BiFilterDTO dto) {

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);
        dto.setNewSign(1);
        List<SalesBaseVO> list = baseMapper.byProductTop(dto, settleRate);
        int initSize = CollectionUtils.isNotEmpty(list) ? list.size() : 10;
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("销售额TOP20% 老品");
        statistical.setChartType(ChartType.BAR);
        ChartVO chart = new ChartVO();
        List<Object> xAxisList = new ArrayList<>(initSize);
        List<SeriesVO<Object>> seriesList = new ArrayList<>(initSize);
        //只有一个柱子
        SeriesVO<Object> series = new SeriesVO();
        series.setName("销售额");
        List<Object> dataList = new ArrayList<>(initSize);
        for (SalesBaseVO item : list) {
            dataList.add(item.getSales());
            xAxisList.add(item.getFlagNo());
        }
        series.setData(dataList);
        seriesList.add(series);
        chart.setXAxis(xAxisList);
        chart.setSeries(seriesList);
        statistical.setData(chart);
        return statistical;

    }


    /**
     * 销售相关 -一级模块 -营销中心销售额
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesCountVO>
     * @author yl
     * @date 2022-12-28 16:01
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byMarketingCenter",keyGenerator = "myKeyGenerator")
    public List<SalesCountVO> byMarketingCenter(BiFilterDTO dto) {
        LocalDate nowDate = LocalDate.now();
        List<SalesCountVO> resultList = new ArrayList<>(12);
        String deptName = "营销中心";
        //  List<String> deptIdList = sysUserFeign.getDeptIdsByName(deptName);
        String timeFlag = "delivery_time";
        if (dto.getTimeType() != null && BiConstant.OLD.equals(dto.getTimeType())) {
            timeFlag = "platform_create_time";
        }
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDate now = LocalDate.now();
        //今年
        String thisYear = String.valueOf(now.getYear());
        //去年
        int lastYear = now.minusYears(1).getYear();
        String lastYearStr = String.valueOf(lastYear);
        List<SalesFlagVO> list = baseMapper.byMarketingCenter(dto, timeFlag, settleRate, thisYear);
        List<SalesFlagVO> lastYearList = baseMapper.byLastYear(dto, timeFlag, settleRate, lastYearStr);
        //上个月开始时间
        LocalDateTime lastMonthStart = LocalDateUtil.getLastMonthStart(nowDate);
        //上个月结束时间
        LocalDateTime lastMonthEnd = LocalDateUtil.getLastMonthEnd(nowDate);
        dto.setStartTime(lastMonthStart);
        dto.setEndTime(lastMonthEnd);
        //获取上个月
        SalesFlagVO lastMonth = baseMapper.byLastMonth(dto, settleRate);

        //获取到当前月
        int nowMonth = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        //总的销售额
        BigDecimal totalSales = list.stream().
                filter(s -> s.getSales() != null).
                map(SalesFlagVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        for (int m = 1; m <= nowMonth; m++) {
            String finalM = m > 9 ? String.valueOf(m) : "0".concat(String.valueOf(m));
            SalesCountVO vo = new SalesCountVO();
            vo.setName(year + "年" + m + "月份");
            SalesFlagVO flag = list.stream().filter(s -> s.getFlag().equals(finalM)).
                    findFirst().orElse(null);
            BigDecimal sales = BigDecimal.ZERO;
            if (flag != null) {
                vo.setSalesQuantity(flag.getSalesQuantity());
                sales = flag.getSales();
                vo.setSales(sales);
                vo.setOrderCount(flag.getOrderCount());
                vo.setSalesRatio(getSalesRatio(totalSales, sales));
            }
            SalesFlagVO LastYearFlag = lastYearList.stream().filter(s -> s.getFlag().equals(finalM)).
                    findFirst().orElse(null);
            if (LastYearFlag != null) {
                vo.setYearBasisRatio(getChainRelativeRatio(sales, LastYearFlag.getSales()));
            }
            //当是第一个的时候
            if (m == 1) {
                vo.setChainRelativeRatio(getChainRelativeRatio(sales, lastMonth.getSales()));
            } else {
                String last = m - 1 > 9 ? String.valueOf(m - 1) : "0".concat(String.valueOf(m - 1));

                SalesFlagVO lastMonthFlag = list.stream().filter(s -> s.getFlag().equals(last)).
                        findFirst().orElse(null);
                if (lastMonthFlag != null) {
                    vo.setChainRelativeRatio(getChainRelativeRatio(sales, lastMonthFlag.getSales()));
                }
            }

            resultList.add(vo);
        }

        return resultList;
    }

    /**
     * @param dto
     * @return
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byEuropeAndJapanSite",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO byEuropeAndJapanSite(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        LocalDateTime paramsEndTime = dto.getEndTime();
        dto.setEndTime(paramsEndTime, 1);

        List<ShopSalesVO> list = baseMapper.byShop(dto, settleRate);
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("亚马逊欧美日占比趋势分析");
        statistical.setChartType(ChartType.PIE);
        ChartVO chartVO = new ChartVO();
        List<String> siteNameList = new ArrayList<>(3);
        siteNameList.add("欧洲站");
        siteNameList.add("美国站");
        siteNameList.add("日本站");
        //获取到 站点的 店铺
        List<ShopSiteVO> shopCategoryList = shopInfoService.getShopCategoryList();

        List<SeriesVO<Object>> seriesList = new ArrayList<>(10);
        SeriesVO<Object> series = new SeriesVO();
        series.setName("站点销售额");
        List<Object> dataList = new ArrayList<>();
        for (String siteName : siteNameList) {
            Map<String, Object> siteMap = new HashMap<>();
            siteMap.put("name", siteName);
            //根据 站点名获取大盘站点信息
            List<String> siteList = SiteEnum.getSiteList(siteName);
            List<ShopSiteVO> siteShopList = shopCategoryList.stream().filter(s -> siteList.contains(s.getSite()))
                    .collect(Collectors.toList());
            List<String> shopNameList = siteShopList.stream().flatMap(s -> s.getShopName().stream()).collect(Collectors.toList());
            BigDecimal value = list.stream().filter(s -> shopNameList.contains(s.getShopName()) && s.getSales() != null).
                    map(ShopSalesVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
            siteMap.put("value", value);
            dataList.add(siteMap);
        }
        series.setData(dataList);
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        chartVO.setXAxis(siteNameList);
        statistical.setData(chartVO);
        return statistical;
    }


    /**
     * 一级销售模块 -日期销售额-类别查询
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2023-01-06 11:19
     */
    private StatisticalDataVO byDateStackedColumnChart(DateSalesTrendDTO.SearchDTO dto, String timeFlag, String settleRate, String groupName) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("销售趋势");
        statistical.setChartType(ChartType.BAR);
        String dateType = dto.getDateType();
        List<SalesFlagVO> salesList = baseMapper.getSalesByReport(dto, timeFlag, settleRate, dto.getSearchType(),groupName);

//        //如果是季度
//        if (dateType.equals("QUARTER")) {
//            for (SalesFlagVO item : salesList) {
//                String name = item.getName();
//                String quarterName = conversionQuarterName(name);
//                item.setName(quarterName);
//            }
//        }
        List<SeriesVO<Object>> seriesList = new ArrayList<>();
        List<String> categoryList = salesList.stream().map(SalesFlagVO::getCategory).distinct().collect(Collectors.toList());
        List<String> dateList = salesList.stream().map(SalesFlagVO::getName).distinct().collect(Collectors.toList());
        for (String category : categoryList) {
            SeriesVO<Object> sales = new SeriesVO();
            List<Object> list = new ArrayList<>();
            if ("new_sign".equals(groupName) && "1".equals(category)) {
                sales.setName("新品");
            } else if ("new_sign".equals(groupName) && "0".equals(category)) {
                sales.setName("老品");
            } else {
                sales.setName(category);
            }
            sales.setType(ChartType.BAR);

            List<SalesFlagVO> salesFlagVOList = salesList.stream().filter(req -> req.getCategory() != null && req.getCategory().equals(category)).collect(Collectors.toList());
            for (String date : dateList) {
                SalesFlagVO salesFlagVO = salesFlagVOList.stream().filter(req -> req.getName().equals(date)).findFirst().orElse(null);
                switch (DateSalesTrendSearchTypeEnum.getEnumByCode(dto.getSearchType())) {
                    case SALES_AMOUNT:
                        if (ObjectUtil.isNotEmpty(salesFlagVO)) {
                            list.add(salesFlagVO.getSales());
                        } else {
                            list.add(BigDecimal.ZERO);
                        }
                        break;
                    case SALES_QUANTITY:
                        if (ObjectUtil.isNotEmpty(salesFlagVO)) {
                            list.add(salesFlagVO.getSalesQuantity());
                        } else {
                            list.add(BigDecimal.ZERO);
                        }
                        break;
                    case SALES_PRICE:
                        if (ObjectUtil.isNotEmpty(salesFlagVO)) {
                            list.add(salesFlagVO.getSalesPrice());
                        } else {
                            list.add(BigDecimal.ZERO);
                        }
                        break;
                }
            }
            sales.setData(list);
            seriesList.add(sales);
        }
        ChartVO chartVO = new ChartVO();
        chartVO.setSeries(seriesList);
        chartVO.setXAxis(dateList);
        statistical.setData(chartVO);
        return statistical;
    }

    /**
     * 一级销售模块 -日期销售额-财务销售额
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2023-01-06 11:19
     */
    private StatisticalDataVO byDateFinanceSales(DateSalesTrendDTO.SearchDTO dto) {


        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("销售趋势");
        statistical.setChartType(ChartType.BAR);
        String dateType = dto.getDateType();

        List<String> dictValues = new ArrayList<>(Arrays.asList(DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode()));

        List<DateCostVO> salesList = biDataSourceCostService.sumByDateAndCostType(dto, dictValues);

        dto.setStartTime(dto.getStartTime().minusYears(1));
        dto.setEndTime(dto.getEndTime().minusYears(1));
        List<DateCostVO> lastYearSalesList = biDataSourceCostService.sumByDateAndCostType(dto, dictValues);
        List<String> list = new ArrayList<>();
        List<String> dateList = new ArrayList<>();
        ChartVO chartVO = new ChartVO();

        List<SeriesVO<Object>> seriesList = new ArrayList<>();

        // 今年销售额
        Map<LocalDate, Map<String, BigDecimal>> costMap = salesList.stream()
                .collect(Collectors.groupingBy(x -> x.getGroupDate(),
                        Collectors.toMap(DateCostVO::getCostType, DateCostVO::getCostValue)));

        // 去年销售额
        Map<LocalDate, Map<String, BigDecimal>> lastYearCostMap = lastYearSalesList.stream()
                .collect(Collectors.groupingBy(x -> x.getGroupDate(),
                        Collectors.toMap(DateCostVO::getCostType, DateCostVO::getCostValue)));
        switch (dateType) {
            case "DAY":
            case "WEEK":
                throw new ServiceException(ApiError.ERROR_DATE_TYPE);
            case "MONTH":
                String format = "{}月";
                list = salesList.stream().map(req -> (req.getGroupDate() + "").substring(0, 7)).collect(Collectors.toList());
                // 月度分组数据销售毛利率
                Map<String, BigDecimal> monthMap = costMap.keySet().stream().collect(Collectors.groupingBy(e -> (e + "").substring(0, 7), MathUtil.summingBigDecimal(v -> {
                    Map<String, BigDecimal> tempMap = costMap.get(v);
                    BigDecimal costMainBusinessIncome = BigDecimal.ZERO;
                    if (ObjectUtil.isNotEmpty(tempMap)) {
                        costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
                    }
                    return costMainBusinessIncome;
                })));

                // 去年月度分组数据销售毛利率
                Map<String, BigDecimal> lastYearMonthMap = lastYearCostMap.keySet().stream().collect(Collectors.groupingBy(e -> (e + "").substring(0, 7), MathUtil.summingBigDecimal(v -> {
                    Map<String, BigDecimal> tempMap = lastYearCostMap.get(v);
                    BigDecimal costMainBusinessIncome = BigDecimal.ZERO;
                    if (ObjectUtil.isNotEmpty(tempMap)) {
                        costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
                    }
                    return costMainBusinessIncome;
                })));
//                dateList = IntStream.rangeClosed(1, 12).mapToObj(x -> StrUtil.format(format, x)).collect(Collectors.toList());
                dateList = list;
                byDateFinanceSalesNumber(monthMap, lastYearMonthMap, seriesList, list);
                break;
            case "QUARTER":
                format = "Q{}";
//                list = IntStream.rangeClosed(1, 4).mapToObj(x -> x).collect(Collectors.toList());

                list = salesList.stream().map(req -> req.getGroupDate().getYear() + "-" + (req.getGroupDate().getMonthValue() - 1) / 3 + 1).distinct().collect(Collectors.toList());

                // 季度分组数据销售毛利率
                Map<String, BigDecimal> quarterMap = costMap.keySet().stream().collect(Collectors.groupingBy(e -> e.getYear() + "-" + (e.getMonthValue() - 1) / 3 + 1, MathUtil.summingBigDecimal(v -> {
                    Map<String, BigDecimal> tempMap = costMap.get(v);
                    BigDecimal costMainBusinessIncome = BigDecimal.ZERO;
                    if (ObjectUtil.isNotEmpty(tempMap)) {
                        costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
                    }
                    return costMainBusinessIncome;
                })));

                // 去年季度分组数据销售毛利率
                Map<String, BigDecimal> lastYearQuarterMap = lastYearCostMap.keySet().stream().collect(Collectors.groupingBy(e -> e.getYear() + "-" + (e.getMonthValue() - 1) / 3 + 1, MathUtil.summingBigDecimal(v -> {
                    Map<String, BigDecimal> tempMap = lastYearCostMap.get(v);
                    BigDecimal costMainBusinessIncome = BigDecimal.ZERO;
                    if (ObjectUtil.isNotEmpty(tempMap)) {
                        costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
                    }
                    return costMainBusinessIncome;
                })));
//                dateList = IntStream.rangeClosed(1, 4).mapToObj(x -> StrUtil.format(format, x)).collect(Collectors.toList());

                dateList = salesList.stream().map(req -> req.getGroupDate().getYear() + "-" + StrUtil.format(format, (req.getGroupDate().getMonthValue() - 1) / 3 + 1)).distinct().collect(Collectors.toList());
                byDateFinanceSalesNumber(quarterMap, lastYearQuarterMap, seriesList, list);
                break;
            case "YEAR":
                format = "{}年";
                // 年分组数据销售毛利率
                Map<String, BigDecimal> yearMap = costMap.keySet().stream().collect(Collectors.groupingBy(e -> e.getYear() + "", MathUtil.summingBigDecimal(v -> {
                    Map<String, BigDecimal> tempMap = costMap.get(v);
                    BigDecimal costMainBusinessIncome = BigDecimal.ZERO;
                    if (ObjectUtil.isNotEmpty(tempMap)) {
                        costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
                    }
                    return costMainBusinessIncome;
                })));

                // 去年年分组数据销售毛利率
                Map<String, BigDecimal> lastYearYearMap = lastYearCostMap.keySet().stream().collect(Collectors.groupingBy(e -> e.getYear() + "", MathUtil.summingBigDecimal(v -> {
                    Map<String, BigDecimal> tempMap = lastYearCostMap.get(v);
                    BigDecimal costMainBusinessIncome = BigDecimal.ZERO;
                    if (ObjectUtil.isNotEmpty(tempMap)) {
                        costMainBusinessIncome = tempMap.getOrDefault("cost_mainBusinessIncome", BigDecimal.ZERO);
                    }
                    return costMainBusinessIncome;
                })));
                list = salesList.stream().map(req -> req.getGroupDate().getYear() + "").distinct().collect(Collectors.toList());
                dateList = list.stream().map(req -> StrUtil.format(format, req)).collect(Collectors.toList());
                byDateFinanceSalesNumber(yearMap, lastYearYearMap, seriesList, list);
                break;
            default:
                salesList = new ArrayList<>();
                break;
        }

        chartVO.setSeries(seriesList);
        chartVO.setXAxis(dateList);
        statistical.setData(chartVO);
        return statistical;
    }

    /**
     * 计算财务销售额同比环比
     *
     * @param monthMap
     * @param lastYearMonthMap
     * @param seriesList
     * @param dateList
     */
    private void byDateFinanceSalesNumber(Map<String, BigDecimal> monthMap, Map<String, BigDecimal> lastYearMonthMap, List<SeriesVO<Object>> seriesList, List<String> dateList) {

        SeriesVO<Object> sales = new SeriesVO();
        sales.setName("今年销售额");
        sales.setType(ChartType.BAR);
        List<Object> orderSalesList = new ArrayList<>();
        for (String date : dateList) {
            if (monthMap.get(date) != null) {
                orderSalesList.add(monthMap.get(date));
            } else {
                orderSalesList.add(BigDecimal.ZERO);
            }
        }
        sales.setData(orderSalesList);
        seriesList.add(sales);

        SeriesVO<Object> lastYearSales = new SeriesVO();
        lastYearSales.setName("去年销售额");
        lastYearSales.setType(ChartType.BAR);
        List<Object> lastYearOrderSalesList = new ArrayList<>();
        for (String date : dateList) {
            String[] split = date.split("-");
            String dateStr = "";
            if (split.length > 1) {
                dateStr = (Integer.valueOf(split[0]) - 1) + "-" + split[1];
            } else {
                dateStr = Integer.valueOf(date) - 1 + "";
            }
            if (lastYearMonthMap.get(dateStr) != null) {
                lastYearOrderSalesList.add(lastYearMonthMap.get(dateStr));
            } else {
                lastYearOrderSalesList.add(BigDecimal.ZERO);
            }
        }
        lastYearSales.setData(lastYearOrderSalesList);
        seriesList.add(lastYearSales);

        SeriesVO<Object> basisRatio = new SeriesVO();
        basisRatio.setName("同比");
        basisRatio.setType(ChartType.LINE);
        List<Object> basisRatioList = new ArrayList<>();
        for (String date : dateList) {
            String[] split = date.split("-");
            String dateStr = "";
            if (split.length > 1) {
                dateStr = (Integer.valueOf(split[0]) - 1) + "-" + split[1];
            } else {
                dateStr = Integer.valueOf(date) - 1 + "";
            }
            if (lastYearMonthMap.get(dateStr) != null && lastYearMonthMap.get(dateStr).compareTo(BigDecimal.ZERO) > 0) {
                basisRatioList.add(monthMap.get(date)
                        .subtract(lastYearMonthMap.get(dateStr))
                        .divide(lastYearMonthMap.get(dateStr), 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(MathUtil.BigDecimal_100)
                );
            } else {
                basisRatioList.add(BigDecimal.ZERO);
            }
        }

        basisRatio.setData(basisRatioList);
        seriesList.add(basisRatio);

        SeriesVO<Object> chainRelativeRatio = new SeriesVO();
        chainRelativeRatio.setName("环比");
        chainRelativeRatio.setType(ChartType.LINE);
        List<Object> chainRelativeRatioList = new ArrayList<>();
        for (int i = 0; i < dateList.size(); i++) {
            if (i == 0) {
                if (lastYearMonthMap.get(dateList.get(dateList.size() - 1)) != null) {
                    chainRelativeRatioList.add(monthMap.get(dateList.get(i)) == null ? BigDecimal.ZERO : monthMap.get(dateList.get(i))
                            .subtract(lastYearMonthMap.get(dateList.get(dateList.size() - 1)))
                            .divide(lastYearMonthMap.get(dateList.get(dateList.size() - 1)), 2, BigDecimal.ROUND_HALF_UP)
                            .multiply(MathUtil.BigDecimal_100)
                    );
                } else {
                    chainRelativeRatioList.add(BigDecimal.ZERO);
                }
            } else {
                if (monthMap.get(dateList.get(i - 1)) != null) {
                    chainRelativeRatioList.add(monthMap.get(dateList.get(i)) == null ? BigDecimal.ZERO : monthMap.get(dateList.get(i))
                            .subtract(monthMap.get(dateList.get(i - 1)))
                            .divide(monthMap.get(dateList.get(i - 1)), 2, BigDecimal.ROUND_HALF_UP)
                            .multiply(MathUtil.BigDecimal_100)
                    );
                } else {
                    chainRelativeRatioList.add(BigDecimal.ZERO);
                }
            }
        }
        chainRelativeRatio.setData(chainRelativeRatioList);
        seriesList.add(chainRelativeRatio);
    }

    /**
     * 一级销售模块 -日期销售额
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2023-01-06 11:19
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:byDate",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO byDate(DateSalesTrendDTO.SearchDTO dto) {

        //如果查询财务销售额
        if (DateSalesTrendSearchTypeEnum.FINANCE_SALES_QUANTITY.getCode().equals(dto.getSearchType())) {
            return this.byDateFinanceSales(dto);
        }
        //维度列表
        String dateType = dto.getDateType();
        List<DateDimensionVO> dateList = baseMapper.getDateList(dto.getStartTime(), dto.getEndTime(), dateType);
        dto.setEndTime(dto.getEndTime(), 1);
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("销售趋势");
        statistical.setChartType(ChartType.BAR);
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());

        //查找的日期
        String timeFlag = "delivery_time";
        if (dto.getTimeType() != null && BiConstant.OLD.equals(dto.getTimeType())) {
            timeFlag = "platform_create_time";
        }
        String groupName = "";
        //类别查询
        if (CollectionUtils.isNotEmpty(dto.getCategory())) {
            groupName = "category_name";
        }

        //部门查询
        if (CollectionUtils.isNotEmpty(dto.getDepartment())) {
            groupName = "dept_name";
        }

        //用户查询
        if (CollectionUtils.isNotEmpty(dto.getUserId())) {
            groupName = "charge_name";
        }

        //店铺查询
        if (CollectionUtils.isNotEmpty(dto.getShopName())) {
            groupName = "shop_name";
        }

        //sku查询
        if (CollectionUtils.isNotEmpty(dto.getSku())) {
            groupName = "sku_no";
        }

        //新/老品查询
        if (dto.getNewSign() != null) {
            groupName = "new_sign";
        }

        //产品属性id查询
        if (CollectionUtils.isNotEmpty(dto.getPropertyIdList())) {
            groupName = "property";
        }

        //平台查询
        if (CollectionUtils.isNotEmpty(dto.getPlatform())) {
            groupName = "source_platform";
        }

        //站点查询
        if (CollectionUtils.isNotEmpty(dto.getSite())) {
            groupName = "site";
        }

        if (StringUtils.isNotBlank(groupName)) {
            return this.byDateStackedColumnChart(dto, timeFlag, settleRate, groupName);
        }

        List<SalesFlagVO> salesList = new ArrayList();
        List<SalesFlagVO> lastYearSalesList = new ArrayList();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        switch (dateType) {
            case "DAY":
                long days = Duration.between(dto.getStartTime(), dto.getEndTime()).toDays();
                if (days > 31) {
                    throw new ServiceException(ApiError.ERROR_DATE_RANGE_THIRTY_ONE);
                }
                dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            case "WEEK":
                long weekDay = Duration.between(dto.getStartTime(), dto.getEndTime()).toDays();
                if (weekDay > 90) {
                    throw new ServiceException(ApiError.ERROR_DATE_RANGE_WEEK_DAY);
                }
                dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            case "MONTH":
                dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            case "QUARTER":
                dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M");
                break;
            case "YEAR":
                dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy");
                break;
            default:
                salesList = new ArrayList<>();
                break;
        }
        salesList = baseMapper.getSalesByReport(dto, timeFlag, settleRate, dto.getSearchType(),null);
        dto.setStartTime(dto.getStartTime().minusYears(1));
        dto.setEndTime(dto.getEndTime().minusYears(1));
        List<DateDimensionVO> lastDateList = baseMapper.getDateList(dto.getStartTime(), dto.getEndTime(), dateType);
        lastYearSalesList = baseMapper.getSalesByReport(dto, timeFlag, settleRate, dto.getSearchType(), null);

        List<SalesFlagVO> newSales = new ArrayList<>(dateList.size());
        List<SalesFlagVO> newLastSales = new ArrayList<>(dateList.size());
        dateSalesTrendRatio(dateList,lastDateList,salesList, lastYearSalesList, dateTimeFormatter, dateType, newSales, newLastSales);

        ChartVO chartVO = new ChartVO();
        List<String> siteNameList = dateList.stream().map(DateDimensionVO::getDateName).collect(Collectors.toList());
        //有两个
        List<SeriesVO<Object>> seriesList = new ArrayList<>();

        switch (DateSalesTrendSearchTypeEnum.getEnumByCode(dto.getSearchType())) {
            case SALES_AMOUNT:
                dateSalesTrendSalesAmount(newSales, newLastSales, seriesList);
                break;
            case SALES_QUANTITY:
                dateSalesTrendSalesQuantity(newSales, newLastSales, seriesList);
                break;
            case SALES_PRICE:
                dateSalesTrendSalesPrice(newSales, newLastSales, seriesList);
                break;
        }

        chartVO.setSeries(seriesList);
        chartVO.setXAxis(siteNameList);
        statistical.setData(chartVO);
        return statistical;
    }

    /**
     * 计算销售趋势同比/环比
     *
     * @param salesList
     * @param lastYearSalesList
     * @param dateTimeFormatter
     * @param dateType
     */
    private void dateSalesTrendRatio(List<DateDimensionVO> dateList,List<DateDimensionVO> lastDateList,List<SalesFlagVO> salesList,
                                     List<SalesFlagVO> lastYearSalesList, DateTimeFormatter dateTimeFormatter, String dateType,
                                     List<SalesFlagVO> newSales,List<SalesFlagVO> newLastSales) {
        if (CollectionUtils.isEmpty(dateList) || CollectionUtils.isEmpty(lastDateList)){
            return;
        }
        List<SalesFlagVO> list = new ArrayList<>();
        list.addAll(salesList);
        list.addAll(lastYearSalesList);
        IntStream.range(0,dateList.size()).forEach(index ->{
            DateDimensionVO dateDimensionVO = dateList.get(index);
            DateDimensionVO lastDateDimensionVO = lastDateList.get(index);
            SalesFlagVO salesFlagVO = salesList.stream().filter(e -> e.getName().equals(dateDimensionVO.getDateName())).findFirst().orElse(new SalesFlagVO());
            salesFlagVO.setName(dateDimensionVO.getDateName());
            //上一期的日期
            String parse = null;
            //去年同期日期
            String prevYearDate = lastDateDimensionVO.getDateName();
            if ("DAY".equals(dateType)) {
                parse = LocalDate.parse(dateDimensionVO.getDateName(), dateTimeFormatter).minusDays(1) + "";
//                prevYearDate = LocalDate.parse(dateDimensionVO.getDateName(), dateTimeFormatter).minusYears(1) + "";
            }
            if ("WEEK".equals(dateType)) {
                String[] split = dateDimensionVO.getDateName().split("~");
                parse = LocalDate.parse(split[0], dateTimeFormatter).minusDays(7) + "~" + LocalDate.parse(split[0], dateTimeFormatter).minusDays(1);
            }
            if ("MONTH".equals(dateType)) {
                Date date = DateUtil.strToDate(dateDimensionVO.getDateName(), DateUtil.fmt_month);
                parse = DateUtil.getPrevMonthDate(date, DateUtil.fmt_month, 1);
//                prevYearDate = DateUtil.getPrevYearDate(date, DateUtil.fmt_month, 1);
            }
            if ("QUARTER".equals(dateType)) {
                parse = dateDimensionVO.getDateName();
//                Date date = DateUtil.strToDate(dateDimensionVO.getDateName(), DateUtil.fmt_quarter);
//                prevYearDate = DateUtil.getPrevYearDate(date, DateUtil.fmt_quarter, 1);
            }
            if ("YEAR".equals(dateType)) {
                Date date = DateUtil.strToDate(dateDimensionVO.getDateName(), DateUtil.FMT_YEAR4);
                parse = DateUtil.getPrevYearDate(date, DateUtil.FMT_YEAR4, 1);
//                prevYearDate = DateUtil.getPrevYearDate(date, DateUtil.FMT_YEAR4, 1);
            }

            //获取销售环比
            String finalParse = parse;
            SalesFlagVO lastYearSalesFlagVo = list.stream().filter(req -> req.getName().equals(finalParse)).findFirst().orElse(new SalesFlagVO());
            if (ObjectUtils.isNotEmpty(lastYearSalesFlagVo) && lastYearSalesFlagVo.getSales().compareTo(BigDecimal.ZERO) > 0) {
                salesFlagVO.setSalesChainRelativeRatio(salesFlagVO.getSales()
                        .subtract(lastYearSalesFlagVo.getSales())
                        .divide(lastYearSalesFlagVo.getSales(), 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(MathUtil.BigDecimal_100)
                );
            }else if (salesFlagVO.getSales().compareTo(BigDecimal.ZERO) > 0){
                salesFlagVO.setSalesChainRelativeRatio(MathUtil.BigDecimal_100);
            }

            //获取销量环比
            if (ObjectUtils.isNotEmpty(lastYearSalesFlagVo) && lastYearSalesFlagVo.getSalesQuantity() > 0) {
                salesFlagVO.setSalesQuantityChainRelativeRatio(MathUtil.valueOf(salesFlagVO.getSalesQuantity() + "")
                        .subtract(MathUtil.valueOf(lastYearSalesFlagVo.getSalesQuantity() + ""))
                        .divide(MathUtil.valueOf(lastYearSalesFlagVo.getSalesQuantity() + ""), 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(MathUtil.BigDecimal_100)
                );
            }else if (salesFlagVO.getSalesQuantity() > 0){
                salesFlagVO.setSalesQuantityChainRelativeRatio(MathUtil.BigDecimal_100);
            }

            //获取客单价环比
            if (ObjectUtils.isNotEmpty(lastYearSalesFlagVo) && lastYearSalesFlagVo.getSalesPrice().compareTo(BigDecimal.ZERO) > 0) {
                salesFlagVO.setSalesPriceChainRelativeRatio(salesFlagVO.getSalesPrice()
                        .subtract(lastYearSalesFlagVo.getSalesPrice())
                        .divide(lastYearSalesFlagVo.getSalesPrice(), 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(MathUtil.BigDecimal_100)
                );
            }else if (salesFlagVO.getSalesPrice().compareTo(BigDecimal.ZERO) > 0){
                salesFlagVO.setSalesPriceChainRelativeRatio(MathUtil.BigDecimal_100);
            }

            //获取销售同比
            String finalPrevYearDate = prevYearDate;
            SalesFlagVO lastYearSalesFlag = lastYearSalesList.stream().filter(req -> req.getName().equals(finalPrevYearDate)).findFirst().orElse(new SalesFlagVO());
            lastYearSalesFlag.setName(finalPrevYearDate);
            if (ObjectUtils.isNotEmpty(lastYearSalesFlag) && lastYearSalesFlag.getSales().compareTo(BigDecimal.ZERO) > 0) {
                salesFlagVO.setSalesBasisRatio(salesFlagVO.getSales()
                        .subtract(lastYearSalesFlag.getSales())
                        .divide(lastYearSalesFlag.getSales(), 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(MathUtil.BigDecimal_100)
                );
            }

            //获取销量同比
            if (ObjectUtils.isNotEmpty(lastYearSalesFlag) && lastYearSalesFlag.getSalesQuantity() > 0) {
                salesFlagVO.setSalesQuantityBasisRatio(MathUtil.valueOf(salesFlagVO.getSalesQuantity() + "")
                        .subtract(MathUtil.valueOf(lastYearSalesFlag.getSalesQuantity() + ""))
                        .divide(MathUtil.valueOf(lastYearSalesFlag.getSalesQuantity() + ""), 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(MathUtil.BigDecimal_100)
                );
            }

            //获取客单价同比
            if (ObjectUtils.isNotEmpty(lastYearSalesFlag) && lastYearSalesFlag.getSalesPrice().compareTo(BigDecimal.ZERO) > 0) {
                salesFlagVO.setSalesPriceBasisRatio(salesFlagVO.getSalesPrice()
                        .subtract(lastYearSalesFlag.getSalesPrice())
                        .divide(lastYearSalesFlag.getSalesPrice(), 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(MathUtil.BigDecimal_100)
                );
            }
            newSales.add(salesFlagVO);
            newLastSales.add(lastYearSalesFlag);
        });
    }

    /**
     * 设置销售趋势的销售额数据
     *
     * @param salesList
     * @param lastYearSalesList
     * @param seriesList
     */
    private void dateSalesTrendSalesAmount(List<SalesFlagVO> salesList, List<SalesFlagVO> lastYearSalesList, List<SeriesVO<Object>> seriesList) {
        SeriesVO<Object> sales = new SeriesVO();
        sales.setName("今年销售额");
        sales.setType(ChartType.BAR);
        List<Object> orderSalesList = salesList.stream().map(SalesFlagVO::getSales).collect(Collectors.toList());
        sales.setData(orderSalesList);
        seriesList.add(sales);

        SeriesVO<Object> lastYearSales = new SeriesVO();
        lastYearSales.setName("去年销售额");
        lastYearSales.setType(ChartType.BAR);
        List<Object> salesAmountList = lastYearSalesList.stream().map(SalesFlagVO::getSales).collect(Collectors.toList());
        lastYearSales.setData(salesAmountList);
        seriesList.add(lastYearSales);

        SeriesVO<Object> salesBasisRatio = new SeriesVO();
        salesBasisRatio.setName("同比");
        salesBasisRatio.setType(ChartType.LINE);
        List<Object> salesBasisRatioList = salesList.stream().map(SalesFlagVO::getSalesBasisRatio).collect(Collectors.toList());
        salesBasisRatio.setData(salesBasisRatioList);
        seriesList.add(salesBasisRatio);

        SeriesVO<Object> salesChainRelativeRatio = new SeriesVO();
        salesChainRelativeRatio.setName("环比");
        salesChainRelativeRatio.setType(ChartType.LINE);
        List<Object> salesChainRelativeRatioList = salesList.stream().map(SalesFlagVO::getSalesChainRelativeRatio).collect(Collectors.toList());
        salesChainRelativeRatio.setData(salesChainRelativeRatioList);
        seriesList.add(salesChainRelativeRatio);
    }

    /**
     * 设置销售趋势的销量数据
     *
     * @param salesList
     * @param lastYearSalesList
     * @param seriesList
     */
    private void dateSalesTrendSalesQuantity(List<SalesFlagVO> salesList, List<SalesFlagVO> lastYearSalesList, List<SeriesVO<Object>> seriesList) {
        SeriesVO<Object> salesQuantity = new SeriesVO();
        salesQuantity.setName("今年销量");
        salesQuantity.setType(ChartType.BAR);
        List<Object> salesQuantityList = salesList.stream().map(SalesFlagVO::getSalesQuantity).collect(Collectors.toList());
        salesQuantity.setData(salesQuantityList);
        seriesList.add(salesQuantity);

        SeriesVO<Object> lastYearSalesQuantity = new SeriesVO();
        lastYearSalesQuantity.setName("去年销量");
        lastYearSalesQuantity.setType(ChartType.BAR);
        List<Object> lastYearSalesQuantityList = lastYearSalesList.stream().map(SalesFlagVO::getSalesQuantity).collect(Collectors.toList());
        lastYearSalesQuantity.setData(lastYearSalesQuantityList);
        seriesList.add(lastYearSalesQuantity);

        SeriesVO<Object> basisRatio = new SeriesVO();
        basisRatio.setName("同比");
        basisRatio.setType(ChartType.LINE);
        List<Object> basisRatioList = salesList.stream().map(SalesFlagVO::getSalesQuantityBasisRatio).collect(Collectors.toList());
        basisRatio.setData(basisRatioList);
        seriesList.add(basisRatio);

        SeriesVO<Object> chainRelativeRatio = new SeriesVO();
        chainRelativeRatio.setName("环比");
        chainRelativeRatio.setType(ChartType.LINE);
        List<Object> chainRelativeRatioList = salesList.stream().map(SalesFlagVO::getSalesQuantityChainRelativeRatio).collect(Collectors.toList());
        chainRelativeRatio.setData(chainRelativeRatioList);
        seriesList.add(chainRelativeRatio);
    }

    /**
     * 设置销售趋势的客单价数据
     *
     * @param salesList
     * @param lastYearSalesList
     * @param seriesList
     */
    private void dateSalesTrendSalesPrice(List<SalesFlagVO> salesList, List<SalesFlagVO> lastYearSalesList, List<SeriesVO<Object>> seriesList) {
        SeriesVO<Object> salesQuantity = new SeriesVO();
        salesQuantity.setName("今年客单价");
        salesQuantity.setType(ChartType.BAR);
        List<Object> salesQuantityList = salesList.stream().map(SalesFlagVO::getSalesPrice).collect(Collectors.toList());
        salesQuantity.setData(salesQuantityList);
        seriesList.add(salesQuantity);

        SeriesVO<Object> lastYearSalesQuantity = new SeriesVO();
        lastYearSalesQuantity.setName("去年客单价");
        lastYearSalesQuantity.setType(ChartType.BAR);
        List<Object> lastYearSalesQuantityList = lastYearSalesList.stream().map(SalesFlagVO::getSalesPrice).collect(Collectors.toList());
        lastYearSalesQuantity.setData(lastYearSalesQuantityList);
        seriesList.add(lastYearSalesQuantity);

        SeriesVO<Object> basisRatio = new SeriesVO();
        basisRatio.setName("同比");
        basisRatio.setType(ChartType.LINE);
        List<Object> basisRatioList = salesList.stream().map(SalesFlagVO::getSalesPriceBasisRatio).collect(Collectors.toList());
        basisRatio.setData(basisRatioList);
        seriesList.add(basisRatio);

        SeriesVO<Object> chainRelativeRatio = new SeriesVO();
        chainRelativeRatio.setName("环比");
        chainRelativeRatio.setType(ChartType.LINE);
        List<Object> chainRelativeRatioList = salesList.stream().map(SalesFlagVO::getSalesPriceChainRelativeRatio).collect(Collectors.toList());
        chainRelativeRatio.setData(chainRelativeRatioList);
        seriesList.add(chainRelativeRatio);
    }

    private String conversionQuarterName(String name) {
        if (StringUtils.isNotBlank(name)) {
            String dateStr[] = name.split("-");
            if (dateStr.length > 0) {
                String year = dateStr[0];
                String month = dateStr[1];
                String quarter = "1";
                if (month.contains("4")) {
                    quarter = "2";
                }
                if (month.contains("7")) {
                    quarter = "3";
                }
                if (month.contains("10")) {
                    quarter = "4";
                }

                return year + "." + quarter + "季度";
            }

        }
        return "";
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:newAndOldSalesAmount",keyGenerator = "myKeyGenerator")
    public List<NewAndOldSalesSearchDTO.PagingDTO> newAndOldSalesAmount(NewAndOldSalesSearchDTO.SearchDTO dto) {
        TargetMetricsSearchTypeEnum enumByCode = TargetMetricsSearchTypeEnum.getEnumByCode(dto.getSearchType());
        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter();
        if (StringUtils.isNotBlank(dto.getYearMonth())) {
            LocalDateTime localDateTime = LocalDate.parse(dto.getYearMonth(), fmt).atStartOfDay();
            dto.setStartTime(localDateTime);
            dto.setEndTime(localDateTime.plusMonths(1));
        } else {
            LocalDate now = LocalDate.now();
            LocalDateTime localDateTime = LocalDate.of(now.getYear(), now.getMonth(), 1).atStartOfDay();
            dto.setStartTime(localDateTime);
            dto.setEndTime(localDateTime.plusMonths(1));
        }
        switch (enumByCode) {
            case DEPT:
                return deptNewAndOldSalesAmount(dto);
            case USER:
                return userNewAndOldSalesAmount(dto);
            default:
                throw new ServiceException(ApiError.SEARCH_TYPE_EXIST);
        }
    }

    @Override
    public Boolean newAndOldSalesExportExcel(NewAndOldSalesSearchDTO.SearchDTO dto, HttpServletResponse response) {
        List<NewAndOldSalesSearchDTO.PagingDTO> pagingDTOS = newAndOldSalesAmount(dto);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/biNewAndOldSalesExport.xlsx";
        String name = "新老品销售额";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(pagingDTOS, response, sb.toString(), excelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }

    /**
     * 战略目标达成
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.dto.BiTargetYearDTO.TargetMetricsFinishDTO>
     * @author yl
     * @date 2023-09-18 11:01
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:listTargetMetrics",keyGenerator = "myKeyGenerator")
    public List<BiTargetYearDTO.TargetMetricsFinishDTO> listTargetMetrics(BiTargetYearDTO.SearchDTO dto) {
        List<BiTargetYearDTO.TargetMetricsFinishDTO> resultList = new ArrayList<>(2);
        /**
         * 年月
         */
        String yearMonth = dto.getYearMonth();
        //年
        Integer year = LocalDate.now().getYear();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        //月
        Integer month = LocalDate.now().getMonthValue();
        if (StringUtils.isNotBlank(yearMonth) && yearMonth.length() >= 7) {
            DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM")
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                    .toFormatter();
            LocalDate yearMonthDate = LocalDate.parse(yearMonth, fmt);
            year = yearMonthDate.getYear();
            month = yearMonthDate.getMonthValue();

            LocalDateTime localDateTime = LocalDate.parse(dto.getYearMonth(), fmt).atStartOfDay();
            dto.setStartTime(localDateTime);
            dto.setEndTime(localDateTime.plusMonths(1));
        } else {
            LocalDate now = LocalDate.now();
            LocalDateTime localDateTime = LocalDate.of(now.getYear(), now.getMonth(), 1).atStartOfDay();
            dto.setStartTime(localDateTime);
            dto.setEndTime(localDateTime.plusMonths(1));
        }

        //指标
        MetricsEnum metricsEnum = dto.getMetrics();
        String metrics = metricsEnum.getCode();
        String metricsName = metricsEnum.getName();
        //获取到对应设置的目标值
        List<String> deptIdList = dto.getDepartment();
        ListYearMonthValueStrategy strategy = null;
        List<BiTargetYearDTO.YearMonthValueDTO> yearMonthValueList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(deptIdList)) {
            strategy = context.getBean(DeptTargetValueStrategy.class);
            if (Objects.nonNull(strategy)) {
                yearMonthValueList = strategy.ListYearMonthValue(year, metrics, deptIdList);
            }
        }
        //员工id
        List<String> staffIdList = dto.getUserId();
        if (CollectionUtils.isNotEmpty(staffIdList)) {
            strategy = context.getBean(StaffTargetValueStrategy.class);
            if (Objects.nonNull(strategy)) {
                yearMonthValueList = strategy.ListYearMonthValue(year, metrics, staffIdList);
            }
        }
        //店铺id
        List<String> shopNameList = dto.getShopName();
        if (CollectionUtils.isNotEmpty(shopNameList)) {
            List<BiShopInfoEntity> shopInfoList = shopInfoService.listByNames(shopNameList);
            List<String> shopIdList = shopInfoList.stream().map(BiShopInfoEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(shopIdList)) {
                strategy = context.getBean(ShopTargetValueStrategy.class);
                if (Objects.nonNull(strategy)) {
                    yearMonthValueList = strategy.ListYearMonthValue(year, metrics, shopIdList);
                }
            }
        }
        //为空就是店铺
        if (Objects.isNull(strategy)) {
            strategy = context.getBean(ShopTargetValueStrategy.class);
            if (Objects.nonNull(strategy)) {
                yearMonthValueList = strategy.ListYearMonthValue(year, metrics, Collections.emptyList());
            }
        }
        if (Objects.isNull(strategy)) {
            throw new ServiceException("条件未匹配");
        }
        //月度
        BiTargetYearDTO.TargetMetricsFinishDTO monthMetrics = new BiTargetYearDTO.TargetMetricsFinishDTO();
        monthMetrics.setMetrics(metrics);
        monthMetrics.setMetricsName(metricsName);
        Integer finalMonth = month;
        Integer finalYear = year;
        //是否毛利率 true 是
        Boolean isGrossProfitRate = MetricsEnum.GROSS_PROFIT_RATE.equals(metricsEnum);
        //月度目标值
        BigDecimal monthMetricsValue = yearMonthValueList.stream().
                filter(y -> y.getMonth().equals(finalMonth) && y.getYear().equals(finalYear)).
                findFirst().map(BiTargetYearDTO.YearMonthValueDTO::getMetricsValue).orElse(BigDecimal.ZERO);
        BigDecimal multiplyValue = MathUtil.BigDecimal_100;
        if (isGrossProfitRate) {
            monthMetricsValue = MathUtil.multiply(monthMetricsValue, multiplyValue, 2);
        }
        monthMetrics.setMetricsValue(monthMetricsValue);
        //完成值
        BigDecimal monthFinishValue = biTargetYearService.getMetricsFinishValue(dto, "month", yearMonth, settleRate);
        if (Objects.isNull(monthFinishValue)) {
            monthFinishValue = BigDecimal.ZERO;
        }
        monthMetrics.setFinishValue(monthFinishValue);
        //完成占比
        BigDecimal monthFinishRate = getSalesRatio(monthMetricsValue, monthFinishValue);
        monthMetrics.setFinishRate(monthFinishRate);
        resultList.add(monthMetrics);


        //年度
        BiTargetYearDTO.TargetMetricsFinishDTO yearMetrics = new BiTargetYearDTO.TargetMetricsFinishDTO();
        yearMetrics.setMetrics(metrics);
        yearMetrics.setMetricsName(metricsName);

        //年度目标值
        BigDecimal yearMetricsValue = yearMonthValueList.stream().map(BiTargetYearDTO.YearMonthValueDTO::getMetricsValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (isGrossProfitRate) {
            yearMetricsValue = MathUtil.multiply(yearMetricsValue, multiplyValue, 2);
        }
        yearMetrics.setMetricsValue(yearMetricsValue);
        BigDecimal yearFinishValue = biTargetYearService.getMetricsFinishValue(dto, "year", yearMonth, settleRate);
        if (Objects.isNull(yearFinishValue)) {
            yearFinishValue = BigDecimal.ZERO;
        }
        yearMetrics.setFinishValue(yearFinishValue);
        //完成占比
        BigDecimal yearFinishRate = getSalesRatio(yearMetricsValue, yearFinishValue);
        yearMetrics.setFinishRate(yearFinishRate);
        resultList.add(yearMetrics);
        return resultList;
    }


    private List<NewAndOldSalesSearchDTO.PagingDTO> deptNewAndOldSalesAmount(NewAndOldSalesSearchDTO.SearchDTO dto) {
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
//        dto.setDateType(DateTypeEnum.MONTH.getType());
        List<SalesFlagVO> list = baseMapper.newAndOldSalesAmount(dto, settleRate,"dept");
        LocalDateTime startTime = dto.getStartTime();
        BiTargetNewProductSettingDTO.TargetParamDTO paramDTO = new BiTargetNewProductSettingDTO.TargetParamDTO();
        paramDTO.setYear(startTime.getYear());
        paramDTO.setMonth(startTime.getMonthValue());
        paramDTO.setMetricsList(Arrays.asList(MetricsEnum.SALES_AMOUNT.getCode(), MetricsEnum.SALES_QTY.getCode()));
        List<String> deptIds = list.stream().map(req -> req.getName()).distinct().collect(Collectors.toList());
        paramDTO.setDeptIdList(deptIds);
        List<BiTargetNewProductSettingDTO.DeptTargetDTO> deptTargetDTOS = biTargetNewProductSettingService.listDeptTarget(paramDTO);

        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;
        List<NewAndOldSalesSearchDTO.PagingDTO> resultList = new ArrayList<>(list.size());
        Map<String, List<SalesFlagVO>> groupMap = list.parallelStream().collect(Collectors.groupingBy(SalesFlagVO::getName));
        for (Map.Entry<String, List<SalesFlagVO>> item : groupMap.entrySet()) {
            String deptId = item.getKey();
            List<SalesFlagVO> salesList = item.getValue();
            NewAndOldSalesSearchDTO.PagingDTO vo = new NewAndOldSalesSearchDTO.PagingDTO();
            SysDepartmentDTO dept = deptList.stream().filter(u -> u.getId().equals(deptId)).findFirst().orElse(null);
            if (dept != null) {
                vo.setName(dept.getName());
            } else {
                vo.setName("无");
            }
            BigDecimal newProductSales = salesList.stream().
                    filter(s -> s.getFlag().equals(newFlag) && s.getSales() != null).
                    map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal oldProductSales = salesList.stream().
                    filter(s -> s.getFlag().equals(oldFlag) && s.getSales() != null).
                    map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);

            Integer newSalesQuantity = salesList.stream().
                    filter(s -> s.getFlag().equals(newFlag) && s.getSalesQuantity() != null).
                    mapToInt(SalesFlagVO::getSalesQuantity).
                    sum();
            Integer oldSalesQuantity = salesList.stream().
                    filter(s -> s.getFlag().equals(oldFlag) && s.getSalesQuantity() != null).
                    mapToInt(SalesFlagVO::getSalesQuantity).
                    sum();
            vo.setNewProductSales(newProductSales);
            vo.setOldProductSales(oldProductSales);
            vo.setNewSalesQuantity(newSalesQuantity);
            vo.setOldSalesQuantity(oldSalesQuantity);
            //新品销售额占比
            if (newProductSales.compareTo(BigDecimal.ZERO) > 0) {
                vo.setNewProductSalesRatio(newProductSales.divide(newProductSales.add(oldProductSales), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
            }
            //老品销售额占比
            if (oldProductSales.compareTo(BigDecimal.ZERO) > 0) {
                vo.setOldProductSalesRatio(oldProductSales.divide(newProductSales.add(oldProductSales), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
            }
            List<BiTargetNewProductSettingDTO.DeptTargetDTO> targetNewProductSettingEntities = deptTargetDTOS.stream().filter(req -> req.getDeptId().equals(deptId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(targetNewProductSettingEntities)) {
                BiTargetNewProductSettingDTO.DeptTargetDTO targetNewProductSalesAmount = targetNewProductSettingEntities.stream().filter(req -> MetricsEnum.SALES_AMOUNT.getCode().equals(req.getMetrics())).findFirst().orElse(new BiTargetNewProductSettingDTO.DeptTargetDTO());
                BiTargetNewProductSettingDTO.DeptTargetDTO targetNewProductSalesQty = targetNewProductSettingEntities.stream().filter(req -> MetricsEnum.SALES_QTY.getCode().equals(req.getMetrics())).findFirst().orElse(new BiTargetNewProductSettingDTO.DeptTargetDTO());
                if (targetNewProductSalesAmount.getValue() != null && targetNewProductSalesAmount.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    vo.setNewSalesAmountFinishRate(newProductSales.divide(targetNewProductSalesAmount.getValue(), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
                }
                if (targetNewProductSalesQty.getValue() != null && targetNewProductSalesQty.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    vo.setNewSalesQuantityFinishRate(MathUtil.valueOf(newSalesQuantity + "").divide(targetNewProductSalesQty.getValue(), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
                }
                if (targetNewProductSalesAmount.getRate() != null && targetNewProductSalesAmount.getRate().compareTo(BigDecimal.ZERO) > 0) {
                    vo.setNewSalesRateFinishRate(vo.getNewProductSalesRatio().divide(targetNewProductSalesAmount.getRate(), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
                }
                vo.setNewSalesAmountTarget(targetNewProductSalesAmount.getValue());
                vo.setNewSalesQuantityTarget(targetNewProductSalesQty.getValue());
                vo.setNewSalesRateTarget(targetNewProductSalesAmount.getRate());
            }

            resultList.add(vo);
        }
        return resultList;
    }

    private List<NewAndOldSalesSearchDTO.PagingDTO> userNewAndOldSalesAmount(NewAndOldSalesSearchDTO.SearchDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;
        dto.setDateType(DateTypeEnum.MONTH.getType());
        List<SalesFlagVO> list = baseMapper.newAndOldSalesAmount(dto, settleRate, "user");
        LocalDateTime startTime = dto.getStartTime();
        BiTargetNewProductSettingDTO.TargetParamDTO paramDTO = new BiTargetNewProductSettingDTO.TargetParamDTO();
        paramDTO.setYear(startTime.getYear());
        paramDTO.setMonth(startTime.getMonthValue());
        paramDTO.setMetricsList(Arrays.asList(MetricsEnum.SALES_AMOUNT.getCode(), MetricsEnum.SALES_QTY.getCode()));
        List<String> userIds = list.stream().map(req -> req.getName()).distinct().collect(Collectors.toList());
        paramDTO.setUserIdList(userIds);

        List<BiTargetNewProductSettingDTO.UserTargetDTO> userTargetDTOS = biTargetNewProductSettingService.listUserTarget(paramDTO);

        Map<String, List<SalesFlagVO>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(SalesFlagVO::getName));
        List<NewAndOldSalesSearchDTO.PagingDTO> resultList = new ArrayList<>(list.size());
        for (Map.Entry<String, List<SalesFlagVO>> item : groupMap.entrySet()) {
            String userId = item.getKey();
            List<SalesFlagVO> salesFlagList = item.getValue();
            NewAndOldSalesSearchDTO.PagingDTO vo = new NewAndOldSalesSearchDTO.PagingDTO();
            FindUserDTO userInfo = userList.stream().filter(u -> u.getUserId().equals(userId)).
                    findFirst().orElse(null);
            if (userInfo != null) {
                vo.setName(userInfo.getUserName());
            }
            BigDecimal newItemSales = salesFlagList.stream().
                    filter(s -> s.getFlag().
                            equals(newFlag)
                            && s.getSales() != null).map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer newItemSalesQuantity = salesFlagList.stream().
                    filter(s -> s.getFlag().equals(newFlag)).
                    mapToInt(SalesFlagVO::getSalesQuantity).sum();
            BigDecimal oldItemSales = salesFlagList.stream().
                    filter(s -> s.getFlag().equals(oldFlag) && s.getSales() != null).
                    map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer oldItemSalesQuantity = salesFlagList.stream().
                    filter(s -> s.getFlag().equals(oldFlag)).
                    mapToInt(SalesFlagVO::getSalesQuantity).sum();

            vo.setOldProductSales(oldItemSales);
            vo.setNewProductSales(newItemSales);
            vo.setOldSalesQuantity(oldItemSalesQuantity);
            vo.setNewSalesQuantity(newItemSalesQuantity);

            if (newItemSales.compareTo(BigDecimal.ZERO) > 0) {
                vo.setNewProductSalesRatio(newItemSales.divide(newItemSales.add(oldItemSales), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
            }

            if (oldItemSales.compareTo(BigDecimal.ZERO) > 0) {
                vo.setOldProductSalesRatio(oldItemSales.divide(newItemSales.add(oldItemSales), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
            }

            List<BiTargetNewProductSettingDTO.UserTargetDTO> targetNewProductSettingEntities = userTargetDTOS.stream().filter(req -> req.getUserId().equals(userId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(targetNewProductSettingEntities)) {
                BiTargetNewProductSettingDTO.UserTargetDTO targetNewProductSalesAmount = targetNewProductSettingEntities.stream().filter(req -> MetricsEnum.SALES_AMOUNT.getCode().equals(req.getMetrics())).findFirst().orElse(new BiTargetNewProductSettingDTO.UserTargetDTO());
                BiTargetNewProductSettingDTO.UserTargetDTO targetNewProductSalesQty = targetNewProductSettingEntities.stream().filter(req -> MetricsEnum.SALES_QTY.getCode().equals(req.getMetrics())).findFirst().orElse(new BiTargetNewProductSettingDTO.UserTargetDTO());
                if (targetNewProductSalesAmount.getValue() != null && targetNewProductSalesAmount.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    vo.setNewSalesAmountFinishRate(newItemSales.divide(targetNewProductSalesAmount.getValue(), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
                }
                if (targetNewProductSalesQty.getValue() != null && targetNewProductSalesQty.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    vo.setNewSalesQuantityFinishRate(MathUtil.valueOf(newItemSalesQuantity + "").divide(targetNewProductSalesQty.getValue(), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
                }
                if (targetNewProductSalesAmount.getRate() != null && targetNewProductSalesAmount.getRate().compareTo(BigDecimal.ZERO) > 0) {
                    vo.setNewSalesRateFinishRate(vo.getNewProductSalesRatio().divide(targetNewProductSalesAmount.getRate(), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
                }
                vo.setNewSalesAmountTarget(targetNewProductSalesAmount.getValue());
                vo.setNewSalesQuantityTarget(targetNewProductSalesQty.getValue());
                vo.setNewSalesRateTarget(targetNewProductSalesAmount.getRate());
            }

            resultList.add(vo);
        }
        return resultList;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:listCompletionRateRanking",keyGenerator = "myKeyGenerator")
    public List<CompletionRateRankingDTO.PagingDTO> listCompletionRateRanking(CompletionRateRankingDTO.SearchDTO dto) {
        TargetMetricsSearchTypeEnum enumByCode = TargetMetricsSearchTypeEnum.getEnumByCode(dto.getSearchType());
        // 获取当月第一天
        LocalDateTime firstDay = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        // 获取下月第一天
        LocalDateTime lastDay = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay().plusMonths(1);
        dto.setStartTime(firstDay);
        dto.setEndTime(lastDay);

        switch (enumByCode) {
            case DEPT:
                return deptCompletionRateRanking(dto);
            case USER:
                return userCompletionRateRanking(dto);
            default:
                throw new ServiceException(ApiError.SEARCH_TYPE_EXIST);
        }
    }

    /**
     * 部门完成率排行
     *
     * @param dto
     * @return
     */
    private List<CompletionRateRankingDTO.PagingDTO> deptCompletionRateRanking(CompletionRateRankingDTO.SearchDTO dto) {
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        LocalDateTime startTime = dto.getStartTime();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<CompletionRateRankingDTO.PagingDTO> list = new ArrayList<>();

        if (CompletionRateRankingEnum.FINANCE_SALES_AMOUNT.getCode().equals(dto.getCompletionRateRankingType())) {
            list = biDataSourceCostService.deptCompletionRateRanking(dto, settleRate);
        } else {
            list = baseMapper.deptCompletionRateRanking(dto, settleRate);
        }

        TargetFinishDTO.ParamDTO paramDTO = new TargetFinishDTO.ParamDTO();
        paramDTO.setYear(dto.getStartTime().getYear() + "");
        paramDTO.setMetrics(MetricsEnum.SALES_AMOUNT.getCode());
        List<String> deptIds = list.stream().map(req -> req.getName()).distinct().collect(Collectors.toList());
        paramDTO.setDepartment(deptIds);
        List<TargetFinishDTO.ViewDTO> viewDTOS = biTargetStaffSettingService.listDeptTargetFinish(paramDTO);
        LocalDateTime localDateTime = dto.getStartTime().minusMonths(1);
        dto.setEndTime(dto.getStartTime());
        dto.setStartTime(localDateTime);
        List<CompletionRateRankingDTO.PagingDTO> lastMonthList = null;
        if (CompletionRateRankingEnum.FINANCE_SALES_AMOUNT.getCode().equals(dto.getCompletionRateRankingType())) {
            lastMonthList = biDataSourceCostService.deptCompletionRateRanking(dto, settleRate);
        } else {
            lastMonthList = baseMapper.deptCompletionRateRanking(dto, settleRate);
        }

        for (CompletionRateRankingDTO.PagingDTO pagingDTO : list) {
            SysDepartmentDTO dept = deptList.stream().filter(u -> u.getId().equals(pagingDTO.getName())).findFirst().orElse(null);
            //计算本月完成率
            List<TargetFinishDTO.ViewDTO> targetFinishList = viewDTOS.stream().filter(req -> req.getTypeId().equals(pagingDTO.getName())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(targetFinishList)) {
                TargetFinishDTO.ViewDTO viewDTO = targetFinishList.stream().filter(req -> req.getTypeId().equals(pagingDTO.getName()) && req.getMonth().equals(startTime.getMonthValue())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(viewDTO)) {
                    pagingDTO.setMonthCompletionRate(pagingDTO.getMonthSales().divide(viewDTO.getValue(), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
                }else if (pagingDTO.getMonthSales().compareTo(BigDecimal.ZERO) > 0){
                    pagingDTO.setMonthCompletionRate(MathUtil.BigDecimal_100);
                }
            }else if (pagingDTO.getMonthSales().compareTo(BigDecimal.ZERO) > 0){
                pagingDTO.setMonthCompletionRate(MathUtil.BigDecimal_100);
            }
            //获取上月排行
            CompletionRateRankingDTO.PagingDTO lastPagingDTO = lastMonthList.stream().filter(req -> req.getName().equals(pagingDTO.getName())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(lastPagingDTO)) {
                pagingDTO.setLastMonthRanking(lastPagingDTO.getRanking());
            }
            if (dept != null) {
                pagingDTO.setName(dept.getName());
            } else {
                pagingDTO.setName("无");
            }
        }
        return list;
    }

    /**
     * 用户完成率排行
     *
     * @param dto
     * @return
     */
    private List<CompletionRateRankingDTO.PagingDTO> userCompletionRateRanking(CompletionRateRankingDTO.SearchDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        LocalDateTime startTime = dto.getStartTime();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<CompletionRateRankingDTO.PagingDTO> list = new ArrayList<>();
        if (CompletionRateRankingEnum.FINANCE_SALES_AMOUNT.getCode().equals(dto.getCompletionRateRankingType())) {
            list = biDataSourceCostService.userCompletionRateRanking(dto, settleRate);
        } else {
            list = baseMapper.userCompletionRateRanking(dto, settleRate);
        }
        TargetFinishDTO.ParamDTO paramDTO = new TargetFinishDTO.ParamDTO();
        paramDTO.setYear(dto.getStartTime().getYear() + "");
        paramDTO.setMetrics(MetricsEnum.SALES_AMOUNT.getCode());
        List<String> userIds = list.stream().map(req -> req.getName()).distinct().collect(Collectors.toList());
        paramDTO.setUserId(userIds);
        List<TargetFinishDTO.ViewDTO> viewDTOS = biTargetStaffSettingService.listUserTargetFinish(paramDTO);
        LocalDateTime localDateTime = dto.getStartTime().minusMonths(1);
        dto.setEndTime(dto.getStartTime());
        dto.setStartTime(localDateTime);
        List<CompletionRateRankingDTO.PagingDTO> lastMonthList = null;
        if (CompletionRateRankingEnum.FINANCE_SALES_AMOUNT.getCode().equals(dto.getCompletionRateRankingType())) {
            lastMonthList = biDataSourceCostService.userCompletionRateRanking(dto, settleRate);
        } else {
            lastMonthList = baseMapper.userCompletionRateRanking(dto, settleRate);
        }

        for (CompletionRateRankingDTO.PagingDTO pagingDTO : list) {

            //计算本月完成率
            List<TargetFinishDTO.ViewDTO> targetFinishList = viewDTOS.stream().filter(req -> req.getTypeId().equals(pagingDTO.getName())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(targetFinishList)) {
                TargetFinishDTO.ViewDTO viewDTO = targetFinishList.stream().filter(req -> req.getTypeId().equals(pagingDTO.getName()) && req.getMonth().equals(startTime.getMonthValue())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(viewDTO)) {
                    pagingDTO.setMonthCompletionRate(pagingDTO.getMonthSales().divide(viewDTO.getValue(), 2, BigDecimal.ROUND_HALF_UP).multiply(MathUtil.BigDecimal_100));
                }else if (pagingDTO.getMonthSales().compareTo(BigDecimal.ZERO) > 0){
                    pagingDTO.setMonthCompletionRate(MathUtil.BigDecimal_100);
                }
            }else if (pagingDTO.getMonthSales().compareTo(BigDecimal.ZERO) > 0){
                pagingDTO.setMonthCompletionRate(MathUtil.BigDecimal_100);
            }
            //获取上月排行
            CompletionRateRankingDTO.PagingDTO lastPagingDTO = lastMonthList.stream().filter(req -> req.getName().equals(pagingDTO.getName())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(lastPagingDTO)) {
                pagingDTO.setLastMonthRanking(lastPagingDTO.getRanking());
            }
            FindUserDTO findUserDTO = userList.stream().filter(u -> u.getUserId().equals(pagingDTO.getName())).findFirst().orElse(null);
            if (findUserDTO != null) {
                pagingDTO.setName(findUserDTO.getUserName());
            } else {
                pagingDTO.setName("无");
            }
        }
        return list;
    }

    @Override
    public Boolean completionRateRankingExportExcel(CompletionRateRankingDTO.SearchDTO dto, HttpServletResponse response) {
        List<CompletionRateRankingDTO.PagingDTO> pagingDTOS = listCompletionRateRanking(dto);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/biCompletionRateRankingExport.xlsx";
        String name = "完成率排行榜";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(pagingDTOS, response, sb.toString(), excelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }


    /**
     * 毛利额 毛利率 模块
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2023-09-21 17:18
     */
    @Override
    @Cacheable(cacheNames = "cache:bi:grossProfit",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO grossProfit(BiDataSourceCostDTO.GrossProfitDTO dto) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        String dateType = dto.getDateType();
        //毛利额
        String grossProfit = MetricsEnum.GROSS_PROFIT.getCode();
        //毛利率
        String grossProfitRate = MetricsEnum.GROSS_PROFIT_RATE.getCode();

        List<BiDataSourceCostDTO.DataValueDTO> dataValueList = Collections.emptyList();
        switch (dateType) {
            //月
            case "MONTH":
                dataValueList = biDataSourceCostDetailService.listGrossMonth(dto);
                break;
            //季度
            case "QUARTER":
                dataValueList = biDataSourceCostDetailService.listGrossQuarter(dto);
                break;
            //年
            case "YEAR":
                dataValueList = biDataSourceCostDetailService.listGrossYear(dto);
                break;

        }

        statistical.setName("毛利额&毛利率");
        ChartVO chartVO = new ChartVO();
        List<String> dataStrList = dataValueList.stream().filter(d -> d.getType().
                        equals(grossProfit)).map(BiDataSourceCostDTO.DataValueDTO::getDateStr).
                sorted().collect(Collectors.toList());

        //如果是季度
        if (dateType.equals("QUARTER")) {
            List<String> quarterList = new ArrayList<>(12);
            for (String dateStr : dataStrList) {
                String quarterStr = conversionQuarterName(dateStr);
                quarterList.add(quarterStr);
            }
            chartVO.setXAxis(quarterList);
        } else {
            chartVO.setXAxis(dataStrList);
        }

        //对应值
        List<SeriesVO> seriesList = new ArrayList<>(2);
        //毛利额
        SeriesVO grossProfitSeries = new SeriesVO();
        grossProfitSeries.setName(MetricsEnum.GROSS_PROFIT.getName());
        grossProfitSeries.setType(ChartType.BAR);
        List<BigDecimal> grossProfitValueList = new ArrayList<>(2);
        for (String str : dataStrList) {
            BigDecimal dataValue = dataValueList.stream().filter(d ->
                            d.getType().equals(grossProfit) && d.getDateStr().equals(str)
                    ).map(BiDataSourceCostDTO.DataValueDTO::getValue).findFirst().
                    orElse(BigDecimal.ZERO);
            grossProfitValueList.add(dataValue);
        }

        grossProfitSeries.setData(grossProfitValueList);
        seriesList.add(grossProfitSeries);

        //毛利率
        SeriesVO grossProfitRateSeries = new SeriesVO();
        grossProfitRateSeries.setName(MetricsEnum.GROSS_PROFIT_RATE.getName());
        grossProfitRateSeries.setType(ChartType.LINE);
        List<BigDecimal> grossProfitRateValueList = new ArrayList<>(12);
        for (String str : dataStrList) {
            BigDecimal dataValue = dataValueList.stream().filter(d ->
                    d.getType().equals(grossProfitRate) && d.getDateStr().equals(str)
            ).map(BiDataSourceCostDTO.DataValueDTO::getValue).findFirst().orElse(BigDecimal.ZERO);
            grossProfitRateValueList.add(dataValue);
        }
        grossProfitRateSeries.setData(grossProfitRateValueList);
        seriesList.add(grossProfitRateSeries);
        chartVO.setSeries(seriesList);
        statistical.setData(chartVO);
        return statistical;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:customerPropertyAnalysis",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO customerPropertyAnalysis(BiFilterDTO params) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("B2B客户属性销售额占比");
        statistical.setChartType(ChartType.PIE);
        List<CustomerInfoVO> customerInfoVOS = customerFeign.listCustomerByProperty();
        if (CollectionUtils.isEmpty(customerInfoVOS)) {
            return statistical;
        }
        //分组
        Map<String, List<CustomerInfoVO>> map = customerInfoVOS.stream().collect(Collectors.groupingBy(CustomerInfoVO::getCustomerProperty));
        if (map.isEmpty()) {
            return statistical;
        }
        LocalDateTime paramsEndTime = params.getEndTime();
        params.setEndTime(paramsEndTime, 1);
        //获取到结算汇率
        String settleRate = getSettleRate(params.getSettleMethod());
        List<CustomerBiFilterDTO> filterDTOS = new ArrayList<>(map.size());
        map.keySet().forEach(s->{
            CustomerBiFilterDTO biFilterDTO = new CustomerBiFilterDTO();
            BeanMapperUtils.copy(params, biFilterDTO);
            List<CustomerInfoVO> customerInfoVOS1 = map.get(s);
            Set<String> shopNo = customerInfoVOS1.stream().map(CustomerInfoVO::getCode).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
            biFilterDTO.setShopNo(new ArrayList<>(shopNo));
            biFilterDTO.setCustomerProperty(customerInfoVOS1.get(0).getCustomerProperty());
            filterDTOS.add(biFilterDTO);
        });
        ChartVO chartVO = new ChartVO();
        chartVO.setXAxis(new ArrayList<>());
        List<SeriesVO<CustomerSaleVO>> seriesList = new ArrayList<>();
        SeriesVO<CustomerSaleVO> series = new SeriesVO();
        series.setName("销售额");
        List<CustomerSaleVO> customerSaleVOS = baseMapper.customerLevelProportion(filterDTOS, settleRate);
        series.setData(customerSaleVOS);
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        statistical.setData(chartVO);
        return statistical;
    }

    @Override
    @Cacheable(cacheNames = "cache:bi:customerLevelProportion",keyGenerator = "myKeyGenerator")
    public StatisticalDataVO customerLevelProportion(BiFilterDTO params) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("B2B客户等级销售额占比");
        statistical.setChartType(ChartType.PIE);
        List<CustomerInfoVO> customerInfoVOS = customerFeign.listCustomerByGroup();
        if (CollectionUtils.isEmpty(customerInfoVOS)) {
            return statistical;
        }
        //分组
        Map<String, List<CustomerInfoVO>> map = customerInfoVOS.stream().collect(Collectors.groupingBy(CustomerInfoVO::getGroupId));
        if (map.isEmpty()) {
            return statistical;
        }
        LocalDateTime paramsEndTime = params.getEndTime();
        params.setEndTime(paramsEndTime, 1);
        //获取到结算汇率
        String settleRate = getSettleRate(params.getSettleMethod());
        List<CustomerBiFilterDTO> filterDTOS = new ArrayList<>(map.size());
        map.keySet().forEach(s->{
            CustomerBiFilterDTO biFilterDTO = new CustomerBiFilterDTO();
            BeanMapperUtils.copy(params, biFilterDTO);
            List<CustomerInfoVO> customerInfoVOS1 = map.get(s);
            Set<String> shopNo = customerInfoVOS1.stream().map(CustomerInfoVO::getCode).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
            biFilterDTO.setShopNo(new ArrayList<>(shopNo));
            biFilterDTO.setCustomerProperty(customerInfoVOS1.get(0).getGroupName());
            filterDTOS.add(biFilterDTO);
        });
        ChartVO chartVO = new ChartVO();
        chartVO.setXAxis(new ArrayList<>());
        List<SeriesVO<CustomerSaleVO>> seriesList = new ArrayList<>();
        SeriesVO<CustomerSaleVO> series = new SeriesVO();
        series.setName("销售额");
        List<CustomerSaleVO> customerSaleVOS = baseMapper.customerLevelProportion(filterDTOS, settleRate);
        series.setData(customerSaleVOS);
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        statistical.setData(chartVO);
        return statistical;
    }
}
