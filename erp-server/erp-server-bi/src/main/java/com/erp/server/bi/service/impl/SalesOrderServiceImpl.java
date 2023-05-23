package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.vo.ChartVO;
import com.common.business.vo.SeriesVO;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.DateFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.constant.BiConstant;
import com.erp.server.bi.constant.ChartType;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.enums.SiteEnum;
import com.erp.server.bi.enums.TimeTypeEnum;
import com.erp.server.bi.mapper.SalesOrderServiceMapper;
import com.erp.server.bi.service.BiProductDetailService;
import com.erp.server.bi.service.DmpShopInfoService;
import com.erp.server.bi.service.SalesOrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 销售维度 模块服务
 *
 * @Classname
 * @Description TODO
 * @Date 2022-12-16 11:09
 * @Created by yl
 */
@Service
public class SalesOrderServiceImpl extends ServiceImpl<SalesOrderServiceMapper, DmpOrderInfoEntity>
        implements SalesOrderService {

    @Resource
    private BiProductDetailService productDetailService;

    @Resource
    private DmpShopInfoService shopInfoService;

    @Resource
    private SysUserFeign sysUserFeign;


    @Override
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
        List<Object> thisYearDataList = new ArrayList<>(initSize);
        for (int m = 1; m <= 12; m++) {
            String finalM = m > 9 ? String.valueOf(m) : "0".concat(String.valueOf(m));
            SalesFlagVO salesFlag = thisYearList.stream().filter(s -> s.getFlag().equals(finalM)).
                    findFirst().orElse(null);
            if (salesFlag != null) {
                thisYearDataList.add(salesFlag.getSales());
            } else {
                thisYearDataList.add(BigDecimal.ZERO);
            }
        }
        thisYearSeries.setData(thisYearDataList);
        seriesList.add(thisYearSeries);
        //去年的
        SeriesVO<Object> lastYearSeries = new SeriesVO();
        lastYearSeries.setName("销售额");
        List<Object> lastYearDataList = new ArrayList<>(initSize);
        for (int m = 1; m <= 12; m++) {
            xAxisList.add(m + "月份");
            String finalM = m > 9 ? String.valueOf(m) : "0".concat(String.valueOf(m));
            SalesFlagVO salesFlag = lastYearList.stream().filter(s -> s.getFlag().equals(finalM)).
                    findFirst().orElse(null);
            if (salesFlag != null) {
                lastYearDataList.add(salesFlag.getSales());
            } else {
                lastYearDataList.add(BigDecimal.ZERO);
            }
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
     * 一级模块 sku 销售额
     *
     * @param dto
     * @return
     */
    @Override
    public List<SalesVO> getBySku(BiFilterDTO dto) {
        LocalDate nowDate = LocalDate.now();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalesVO> resultList = baseMapper.getBySku(dto, settleRate);
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime beforeThirtyDays = LocalDateUtil.getBeforeStartTime(nowTime, 29);
        dto.setStartTime(beforeThirtyDays);
        dto.setEndTime(nowTime);
        String findTime = "delivery_time";
        if (dto.getTimeType() != null && dto.getTimeType() == 0) {
            findTime = "platform_create_time";
        }
        //查询进三十天信息
        List<SalesBaseVO> lastThirtyDays = baseMapper.getLastDays(dto, settleRate, findTime);
        LocalDateTime beforeSevenDays = LocalDateUtil.getBeforeStartTime(nowTime, 6);
        dto.setStartTime(beforeSevenDays);
        dto.setEndTime(nowTime);

        //查询进七天信息
        List<SalesBaseVO> lastSevenDays = baseMapper.getLastDays(dto, settleRate, findTime);
        for (SalesVO item : resultList) {
            List<Integer> salesTrend = new ArrayList<>(7);
            //近三十天
            Integer lastThirtyDaysSalesQuantity = lastThirtyDays.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) && b.getFlagNo().equals(item.getName())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();
            //近七天
            Integer lastSevenDaysSalesQuantity = lastSevenDays.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) &&
                            b.getFlagNo().equals(item.getName())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();

            item.setLastSevenDaysSalesQuantity(lastSevenDaysSalesQuantity);
            item.setLastThirtyDaysSalesQuantity(lastThirtyDaysSalesQuantity);

            for (int i = 6; i >= 0; i--) {
                LocalDate flagDay = nowDate.minus(i, ChronoUnit.DAYS);
                LocalDateTime startTime = LocalDateUtil.startLocalDateTime(flagDay);
                LocalDateTime endTime = LocalDateUtil.endLocalDateTime(flagDay);
                Integer salesQuantity = lastSevenDays.stream().
                        filter(b -> b.getFlagDate().isAfter(startTime)
                                && b.getFlagDate().isBefore(endTime)
                                && b.getFlagNo().equals(item.getName())
                                && b.getSales() != null
                        ).mapToInt(SalesBaseVO::getSalesQuantity).sum();
                salesTrend.add(salesQuantity);
            }
            item.setSalesTrend(salesTrend);
            BigDecimal sales = item.getSales();
            Integer orderCount = item.getOrderCount();
            if (orderCount != 0 && sales != null) {
                //客单价
                BigDecimal perCustomerTransaction = sales.divide(new BigDecimal(orderCount), 2, BigDecimal.ROUND_HALF_UP);
                item.setPerCustomerTransaction(perCustomerTransaction);
            }

        }

        return resultList;
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
    public XyAxesResultVO getByCountry(BiFilterDTO dto) {
        XyAxesResultVO result = new XyAxesResultVO();

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
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
    public StatisticalDataVO getByPlatformRatio(BiFilterDTO dto) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        //获取各个平台的销售额
        List<Map<String, Object>> resultList = baseMapper.getPlatformSales(dto, settleRate);
        int initSize = CollectionUtils.isNotEmpty(resultList) ? resultList.size() : 10;
        statistical.setName("平台销售额");
        statistical.setChartType(ChartType.PIE);
        ChartVO chartVO = new ChartVO();
        chartVO.setXAxis(new ArrayList<>());
        List<SeriesVO<Object>> seriesList = new ArrayList<>(initSize);
        SeriesVO<Object> series = new SeriesVO();
        series.setName("平台销售额");
        List<Object> list = new ArrayList<>(resultList.size());
        for (Map<String, Object> map : resultList) {
            list.add(map);
        }
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
    public StatisticalDataVO byTobToc(BiFilterDTO dto) {
        StatisticalDataVO statistical = new StatisticalDataVO();

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
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
    public List<ShopSalesVO> getByShop(BiFilterDTO dto) {
        LocalDate nowDate = LocalDate.now();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        String findTime = "delivery_time";
        if (dto.getTimeType() != null && dto.getTimeType() == 0) {
            findTime = "platform_create_time";
        }
        List<ShopSalesVO> resultList = baseMapper.getByShop(dto, settleRate);
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime beforeThirtyDays = LocalDateUtil.getBeforeStartTime(nowTime, 30);
        dto.setStartTime(beforeThirtyDays);
        dto.setEndTime(nowTime);
        //查询进三十天信息
        List<SalesBaseVO> lastThirtyList = baseMapper.getShopLastDays(dto, settleRate, findTime);
        LocalDateTime beforeSevenDays = LocalDateUtil.getBeforeStartTime(nowTime, 7);

        dto.setStartTime(beforeSevenDays);
        dto.setEndTime(nowTime);
        //查询近七天信息
        List<SalesBaseVO> lastSevenList = baseMapper.getShopLastDays(dto, settleRate, findTime);
        lastSevenList = lastSevenList.stream().filter(s -> s.getSales() != null).collect(Collectors.toList());
        for (ShopSalesVO item : resultList) {
            List<BigDecimal> salesTrend = new ArrayList<>(7);

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


            for (int i = 6; i >= 0; i--) {
                LocalDate flagDay = nowDate.minus(i, ChronoUnit.DAYS);
                LocalDateTime startTime = LocalDateUtil.startLocalDateTime(flagDay);
                LocalDateTime endTime = LocalDateUtil.endLocalDateTime(flagDay);
                BigDecimal salesFlag = lastSevenList.stream().
                        filter(b -> b.getFlagDate().isAfter(startTime)
                                && b.getFlagDate().isBefore(endTime)
                                && b.getFlagNo().equals(item.getShopNo())
                                && b.getSales() != null
                        ).map(SalesBaseVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                salesTrend.add(salesFlag.setScale(2, RoundingMode.HALF_UP));
            }

            BigDecimal sales = item.getSales();
            Integer orderCount = item.getOrderCount();
            if (orderCount != 0 && sales != null) {
                //客单价
                BigDecimal perCustomerTransaction = sales.divide(new BigDecimal(orderCount), 2, BigDecimal.ROUND_HALF_UP);
                item.setPerCustomerTransaction(perCustomerTransaction);
            }
            item.setSalesTrend(salesTrend);


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
    public StatisticalDataVO byTopShop(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        String shopNo = "shopNo";
        StatisticalDataVO result = new StatisticalDataVO();
        result.setName("销售额TOP20店铺");
        result.setChartType(ChartType.BAR);
        List<Map<String, Object>> resultList = baseMapper.byTopShop(dto, settleRate);
        List<String> shopNoList = resultList.stream().map(obj -> obj.get(shopNo).toString()).collect(Collectors.toList());
        List<DmpShopInfoEntity> shopList = shopInfoService.getByShopNoList(shopNoList);
        for (Map<String, Object> item : resultList) {
            if (item.containsKey(shopNo)) {
                String shopNoFlag = item.get(shopNo).toString();
                String shopName = shopList.stream().filter(s -> s.getPlarformShopNo().equals(shopNoFlag)).
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
    public XyAxesResultVO byShopCountry(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
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
    public XyAxesResultVO byShopCategory(BiFilterDTO dto) {

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        XyAxesResultVO result = new XyAxesResultVO();
        //查询sku 分类以及分类下对应的skuno
        List<SkuCategoryVO> skuCategoryList = productDetailService.getSkuCategoryList();
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
        Map<String, List<ShopSalesVO>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(ShopSalesVO::getShopNo));
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
    public List<SalesCountVO> byBrand(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        //品牌的话 根据sku 分了
        List<SalesCountVO> list = baseMapper.byBrand(dto, settleRate);

        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期 因为get 加一天 所以这里减一天
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        List<SalesCountVO> chainList = baseMapper.byBrand(dto, settleRate);

        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateUtil.getStartTime(startTime.minusYears(1));
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateUtil.getEndTime(endTime.minusYears(1));
        dto.setStartTime(yearBasisStartTime);
        // 因为get 加一天 所以这里减一天
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
    public List<SalesCountVO> byPlatform(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalesCountVO> list = baseMapper.byPlatform(dto, settleRate);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        //获取到环比的结束日期 因为get 加一天 所以这里减一天
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        List<SalesCountVO> chainList = baseMapper.byPlatform(dto, settleRate);

        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateUtil.getStartTime(startTime.minusYears(1));
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateUtil.getEndTime(endTime.minusYears(1));
        // 因为get 加一天 所以这里减一天
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
    public StatisticalDataVO byHomeAndAbroad(BiFilterDTO dto) {

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        String chinaName = "中国";
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
                filter(s -> s.getName().contains(chinaName) && s.getSales() != null).
                map(SalesCountVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);

        chinaMap.put("value", chinaSales);
        list.add(chinaMap);
        //国外
        Map<String, Object> abroadMap = new HashMap();
        abroadMap.put("name", "国外");
        BigDecimal abroadSales = resultList.stream().
                filter(s -> !s.getName().contains(chinaName) && s.getSales() != null).
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
    public List<SalesCountVO> byPeople(BiFilterDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalesBaseVO> list = baseMapper.byPeople(dto, settleRate);
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期 因为get 加一天 所以这里减一天
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        //这个是环比的查询出来的
        List<SalesBaseVO> chainList = baseMapper.byPeople(dto, settleRate);


        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateUtil.getStartTime(startTime.minusYears(1));
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateUtil.getEndTime(endTime.minusYears(1));
        dto.setStartTime(yearBasisStartTime);
        // 因为get 加一天 所以这里减一天
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
    public List<ShopNewAndOldSalesVO> byShopNewAndOld(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<ShopSalesVO> shopSalesList = baseMapper.byShopNewAndOld(dto, settleRate);

        List<String> shopNoList = shopSalesList.stream().map(ShopSalesVO::getShopNo).collect(Collectors.toList());

        List<DmpShopInfoEntity> shopList = shopInfoService.getByShopNoList(shopNoList);

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
            String shopName = shopList.stream().filter(s -> s.getPlarformShopNo().equals(item.getKey())).
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
    public List<SalesCountVO> byCountry(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalesCountVO> resultList = baseMapper.byCountry(dto, settleRate);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期 因为get 加一天 所以这里减一天
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);

        //这个是环比的查询出来的
        List<SalesCountVO> chainList = baseMapper.byCountry(dto, settleRate);

        //同比开始时间
        LocalDateTime yearBasisStartTime = LocalDateTime.of(startTime.minusYears(1).toLocalDate(), LocalTime.MIN);
        //同比开始时间
        LocalDateTime yearBasisEndTime = LocalDateTime.of(endTime.minusYears(1).toLocalDate(), LocalTime.MIN);
        dto.setStartTime(yearBasisStartTime);

        // 因为get 加一天 所以这里减一天
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
    public StatisticalDataVO byCategory(BiFilterDTO dto) {
        //查询sku 分类以及分类下对应的skuno
        List<SkuCategoryVO> skuCategoryList = productDetailService.getSkuCategoryList();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalesBaseVO> list = baseMapper.byCategory(dto, settleRate);
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setChartType(ChartType.BAR);
        statistical.setName("销售品类排行");
        ChartVO chart = new ChartVO();
        List<Object> xAxisList = new ArrayList<>(skuCategoryList.size());
        List<SeriesVO<Object>> seriesList = new ArrayList<>(10);
        //只有一个柱子
        SeriesVO<Object> series = new SeriesVO();
        series.setName("品类销售额");
        List<Object> dataList = new ArrayList<>(10);
        for (SkuCategoryVO item : skuCategoryList) {
            List<String> skuList = item.getSkuList();
            xAxisList.add(item.getName());
            BigDecimal totalSales = list.stream().filter(
                    s -> skuList.contains(s.getFlagNo()) && s.getSales() != null
            ).map(SalesBaseVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);

            dataList.add(totalSales);
        }
        series.setData(dataList);
        seriesList.add(series);
        chart.setXAxis(xAxisList);
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
    public List<PeopleSalesRankVO> byPeopleWeekRank(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<PeopleSalesRankVO> resultList = new ArrayList<>(10);
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        LocalDate nowDate = LocalDate.now();
        //本周开始时间
        LocalDateTime weekStart = LocalDateUtil.getThisWeekStart(nowDate);
        //本周结束时间
        LocalDateTime weekEnd = LocalDateUtil.getThisWeekEnd(nowDate);
        dto.setStartTime(weekStart);
        dto.setEndTime(weekEnd);
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
    public List<SalesCountVO> byDept(BiFilterDTO dto) {
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalesBaseVO> list = baseMapper.byDept(dto, settleRate);
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();

        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期 因为get 加一天 所以这里减一天
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
    public List<ProductNewAndOldVO> byDeptNewAndOld(BiFilterDTO dto) {
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
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
    public List<SalesCountVO> byNewAndOld(BiFilterDTO dto) {

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalesCountVO> list = baseMapper.byNewAndOld(dto, settleRate);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期 因为get 加一天 所以这里减一天
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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

        List<SalesFlagVO> list = baseMapper.byPlatformNewAndOld(dto, settleRate);

        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期 因为get 加一天 所以这里减一天
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
    public List<ProductNewAndOldVO> byPeopleNewAndOld(BiFilterDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;

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
    public List<ProductNewAndOldVO> byCategoryNewAndOld(BiFilterDTO dto) {
        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;


        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<ProductNewAndOldVO> resultList = new ArrayList<>(10);
        //查询sku 分类以及分类下对应的skuno
        List<SkuCategoryVO> skuCategoryList = productDetailService.getSkuCategoryList();
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
    public List<SalesCountVO> bySite(BiFilterDTO dto) {
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<ShopSalesVO> list = baseMapper.bySite(dto, settleRate);
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期 因为get 加一天 所以这里减一天
        LocalDateTime ringRatioEndDate = LocalDateTime.of(startTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
        yearBasisEndTime = LocalDateTime.of(yearBasisEndTime.plusDays(-1).toLocalDate(), LocalTime.MIN);
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
            List<String> shopNoList = item.getShopNo();
            String site = item.getSite();
            vo.setName(site);
            BigDecimal sales = list.stream().
                    filter(s -> shopNoList.contains(s.getShopNo()) && s.getSales() != null).
                    map(ShopSalesVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);

            vo.setSalesRatio(getSalesRatio(totalSales, sales));
            vo.setSales(sales);
            Integer salesQuantity = list.stream().
                    filter(s -> shopNoList.contains(s.getShopNo())).
                    mapToInt(ShopSalesVO::getSalesQuantity).
                    sum();
            vo.setSalesQuantity(salesQuantity);

            Integer orderCount = list.stream().
                    filter(s -> shopNoList.contains(s.getShopNo())).
                    mapToInt(ShopSalesVO::getOrderCount).
                    sum();
            vo.setOrderCount(orderCount);

            BigDecimal chainSales = chainList.stream().
                    filter(c -> shopNoList.contains(c.getShopNo()) && c.getSales() != null).
                    map(ShopSalesVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);

            vo.setChainRelativeRatio(getChainRelativeRatio(sales, chainSales));

            BigDecimal yearBasisSales = yearBasisList.stream().
                    filter(c -> shopNoList.contains(c.getShopNo()) && c.getSales() != null).
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
    public StatisticalDataVO byProductType(BiFilterDTO dto) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalesBaseVO> list = baseMapper.byCategory(dto, settleRate);
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
    public StatisticalDataVO byOldProductTop(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        List<SalesBaseVO> list = baseMapper.byOldProductTop(dto, settleRate);
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
    public StatisticalDataVO byNewProductTop(BiFilterDTO dto) {

        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());


        List<SalesBaseVO> list = baseMapper.byNewProductTop(dto, settleRate);
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
                String last = m-1 > 9 ? String.valueOf(m-1) : "0".concat(String.valueOf(m-1));

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
    public StatisticalDataVO byEuropeAndJapanSite(BiFilterDTO dto) {
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());


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
            List<String> shopNoList = siteShopList.stream().flatMap(s -> s.getShopNo().stream()).collect(Collectors.toList());
            BigDecimal value = list.stream().filter(s -> shopNoList.contains(s.getShopNo()) && s.getSales() != null).
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
     * 一级销售模块 -日期销售额
     *
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     * @author yl
     * @date 2023-01-06 11:19
     */
    @Override
    public StatisticalDataVO byDate(DateFilterDTO dto) {
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("销售趋势");
        statistical.setChartType(ChartType.PIE);
        String dateType = dto.getDateType();
        //获取到结算汇率
        String settleRate = getSettleRate(dto.getSettleMethod());
        //查找的日期
        String timeFlag = "delivery_time";
        if (dto.getTimeType() != null && BiConstant.OLD.equals(dto.getTimeType())) {
            timeFlag = "platform_create_time";
        }
        List<SalesFlagVO> salesList = new ArrayList();
        switch (dateType) {
            case "DAY":
                salesList = baseMapper.getByDay(dto, timeFlag, settleRate);
                break;
            case "MONTH":
                salesList = baseMapper.getByMonth(dto, timeFlag, settleRate);
                break;
            case "QUARTER":
                salesList = baseMapper.getByQuarter(dto, timeFlag, settleRate);
                break;
            case "YEAR":
                salesList = baseMapper.getByYear(dto, timeFlag, settleRate);
                break;
            default:
                salesList = new ArrayList<>();
                break;
        }

        //如果是季度
        if (dateType.equals("QUARTER")) {
            for (SalesFlagVO item : salesList) {
                String name = item.getName();
                String quarterName = conversionQuarterName(name);
                item.setName(quarterName);
            }
        }

        ChartVO chartVO = new ChartVO();
        List<String> siteNameList = salesList.stream().map(SalesFlagVO::getName).collect(Collectors.toList());
        //有两个
        List<SeriesVO<Object>> seriesList = new ArrayList<>(2);

        SeriesVO<Object> salesQuantity = new SeriesVO();
        salesQuantity.setName("销售量");
        List<Object> salesQuantityList = salesList.stream().map(SalesFlagVO::getSalesQuantity).collect(Collectors.toList());
        salesQuantity.setData(salesQuantityList);
        seriesList.add(salesQuantity);
        SeriesVO<Object> sales = new SeriesVO();
        sales.setName("销售额");
        List<Object> orderSalesList = salesList.stream().map(SalesFlagVO::getSales).collect(Collectors.toList());
        sales.setData(orderSalesList);
        seriesList.add(sales);
        chartVO.setSeries(seriesList);
        chartVO.setXAxis(siteNameList);
        statistical.setData(chartVO);
        return statistical;
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

}
