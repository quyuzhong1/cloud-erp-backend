package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.server.bi.constant.ChartType;
import com.erp.server.bi.constant.IsDeleted;
import com.erp.server.bi.mapper.SalesOrderServiceMapper;
import com.erp.server.bi.service.BiSkuInfoService;
import com.erp.server.bi.service.SalesOrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private BiSkuInfoService skuInfoService;

    @Override
    public StatisticalDataVO getMonthSales() {
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setChartType(ChartType.BAR);
        statistical.setName("月销售额趋势");
        ChartVO chart = new ChartVO();
        List<Map<String, Object>> resultList = baseMapper.getMonthSales();
        int initSize = CollectionUtils.isNotEmpty(resultList) ? resultList.size() : 10;
        List<Object> xAxisList = new ArrayList<>(initSize);
        List<SeriesVO<Object>> seriesList = new ArrayList<>(initSize);
        //只有一个柱子
        SeriesVO<Object> series = new SeriesVO();
        series.setName("销售额");
        List<Object> dataList = new ArrayList<>(initSize);
        for (Map<String, Object> map : resultList) {
            dataList.add(map.get("orderSales"));
            xAxisList.add(map.get("month"));
        }
        series.setData(dataList);
        seriesList.add(series);
        chart.setXAxis(xAxisList);
        chart.setSeries(seriesList);
        statistical.setData(chart);
        return statistical;
    }


    /**
     * 一级模块 sku 销售额
     *
     * @param dto
     * @return
     */
    @Override
    public List<SalesVO> getBySku(BiFilterDTO dto) {
        List<SalesVO> resultList = baseMapper.getBySku(dto);
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDate nowDate = LocalDate.now();
        LocalDateTime beforeThirtyDays = nowTime.minus(30, ChronoUnit.DAYS);
        dto.setStartTime(beforeThirtyDays);
        dto.setEndTime(nowTime);
        //查询进三十天信息
        List<SalesBaseVO> baseList = baseMapper.getLastThirtyDays(dto);
        LocalDateTime beforeSevenDays = nowTime.minus(7, ChronoUnit.DAYS);
        for (SalesVO item : resultList) {
            List<BigDecimal> salesTrend = new ArrayList<>(7);
            //近七天销售量
            Integer lastSevenDaysSalesQuantity = baseList.stream().
                    filter(b -> b.getFlagDate().isAfter(beforeSevenDays)
                            && b.getFlagDate().isBefore(nowTime)
                            && b.getFlagNo().equals(item.getName())
                    ).mapToInt(SalesBaseVO::getSalesQuantity).sum();

            Integer lastThirtyDaysSalesQuantity = baseList.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) && b.getFlagNo().equals(item.getName())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();

            item.setLastSevenDaysSalesQuantity(lastSevenDaysSalesQuantity);
            item.setLastThirtyDaysSalesQuantity(lastThirtyDaysSalesQuantity);
            for (int i = 6; i >= 0; i--) {
                LocalDate flagDay = nowDate.minus(i, ChronoUnit.DAYS);
                LocalDateTime startTime = LocalDateUtil.startLocalDateTime(flagDay);
                LocalDateTime endTime = LocalDateUtil.endLocalDateTime(flagDay);
                BigDecimal salesFlag = baseList.stream().
                        filter(b -> b.getFlagDate().isAfter(startTime)
                                && b.getFlagDate().isBefore(endTime)
                                && b.getFlagNo().equals(item.getName())
                        ).map(SalesBaseVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                salesTrend.add(salesFlag.setScale(2, RoundingMode.HALF_UP));
            }
            item.setSalesTrend(salesTrend);
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
        List<SalesVO> resultList = baseMapper.getBySpu(dto);
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDate nowDate = LocalDate.now();
        LocalDateTime beforeThirtyDays = nowTime.minus(30, ChronoUnit.DAYS);
        dto.setStartTime(beforeThirtyDays);
        dto.setEndTime(nowTime);
        //查询进三十天信息
        List<SalesBaseVO> baseList = baseMapper.getLastThirtyDays(dto);
        LocalDateTime beforeSevenDays = nowTime.minus(7, ChronoUnit.DAYS);
        for (SalesVO item : resultList) {
            List<BigDecimal> salesTrend = new ArrayList<>(7);
            //近七天销售量
            Integer lastSevenDaysSalesQuantity = baseList.stream().
                    filter(b -> b.getFlagDate().isAfter(beforeSevenDays)
                            && b.getFlagDate().isBefore(nowTime)
                            && b.getFlagNo().equals(item.getName())
                    ).mapToInt(SalesBaseVO::getSalesQuantity).sum();

            Integer lastThirtyDaysSalesQuantity = baseList.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) && b.getFlagNo().equals(item.getName())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();

            item.setLastSevenDaysSalesQuantity(lastSevenDaysSalesQuantity);
            item.setLastThirtyDaysSalesQuantity(lastThirtyDaysSalesQuantity);
            for (int i = 6; i >= 0; i--) {
                LocalDate flagDay = nowDate.minus(i, ChronoUnit.DAYS);
                LocalDateTime startTime = LocalDateUtil.startLocalDateTime(flagDay);
                LocalDateTime endTime = LocalDateUtil.endLocalDateTime(flagDay);
                BigDecimal salesFlag = baseList.stream().
                        filter(b -> b.getFlagDate().isAfter(startTime)
                                && b.getFlagDate().isBefore(endTime)
                                && b.getFlagNo().equals(item.getName())
                        ).map(SalesBaseVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                salesTrend.add(salesFlag.setScale(2, RoundingMode.HALF_UP));
            }
            item.setSalesTrend(salesTrend);
        }

        return resultList;
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
    public List<SalesByCountryVO> getByCountry(BiFilterDTO dto) {
        return baseMapper.getByCountry(dto);
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
        //获取各个平台的销售额
        List<Map<String, Object>> resultList = baseMapper.getPlatformSales(dto);
        int initSize = CollectionUtils.isNotEmpty(resultList) ? resultList.size() : 10;
        statistical.setName("平台销售额");
        statistical.setChartType(ChartType.PIE);
        ChartVO chartVO = new ChartVO();
        chartVO.setXAxis(new ArrayList<>());
        List<SeriesVO<Object>> seriesList = new ArrayList<>(initSize);
        SeriesVO<Object> series = new SeriesVO();
        series.setName("平台销售额");
        series.setData(Collections.singletonList(resultList));
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
        List<ShopSalesVO> resultList = baseMapper.getByShop(dto);
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDate nowDate = LocalDate.now();
        LocalDateTime beforeThirtyDays = nowTime.minus(30, ChronoUnit.DAYS);
        dto.setStartTime(beforeThirtyDays);
        dto.setEndTime(nowTime);
        //查询进三十天信息
        List<SalesBaseVO> baseList = baseMapper.getShopLastThirtyDays(dto);
        LocalDateTime beforeSevenDays = nowTime.minus(7, ChronoUnit.DAYS);
        for (ShopSalesVO item : resultList) {
            //七天的销售额
            List<BigDecimal> salesTrend = new ArrayList<>(7);
            //近七天销售量
            Integer lastSevenDaysSalesQuantity = baseList.stream().
                    filter(b -> b.getFlagDate().isAfter(beforeSevenDays)
                            && b.getFlagDate().isBefore(nowTime)
                            && b.getFlagNo().equals(item.getShopNo())
                    ).mapToInt(SalesBaseVO::getSalesQuantity).sum();
            //近三十天销售量
            Integer lastThirtyDaysSalesQuantity = baseList.stream().
                    filter(b -> StringUtils.isNotBlank(b.getFlagNo()) && b.getFlagNo().equals(item.getShopNo())).
                    mapToInt(SalesBaseVO::getSalesQuantity).sum();
            item.setLastSevenDaysSalesQuantity(lastSevenDaysSalesQuantity);
            item.setLastThirtyDaysSalesQuantity(lastThirtyDaysSalesQuantity);
            for (int i = 6; i >= 0; i--) {
                LocalDate flagDay = nowDate.minus(i, ChronoUnit.DAYS);
                LocalDateTime startTime = LocalDateUtil.startLocalDateTime(flagDay);
                LocalDateTime endTime = LocalDateUtil.endLocalDateTime(flagDay);
                BigDecimal salesFlag = baseList.stream().
                        filter(b -> b.getFlagDate().isAfter(startTime)
                                && b.getFlagDate().isBefore(endTime)
                                && b.getFlagNo().equals(item.getShopNo())
                        ).map(SalesBaseVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                salesTrend.add(salesFlag.setScale(2, RoundingMode.HALF_UP));
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
        StatisticalDataVO result = new StatisticalDataVO();
        result.setName("销售额TOP20店铺");
        result.setChartType(ChartType.BAR);
        List<Map<String, Object>> resultList = baseMapper.byTopShop(dto);
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
    public List<SalesGroupVO> byShopCountry(BiFilterDTO dto) {
        List<CountryCountVO> countryNameList = baseMapper.getCountryList();
        int countrySize = countryNameList.size();
        List<ShopSalesVO> list = baseMapper.byShopCountry(dto);
        Map<String, List<ShopSalesVO>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(ShopSalesVO::getShopNo));
        int initSize = groupMap.size();
        List<SalesGroupVO> resultList = new ArrayList<>(initSize);

        for (Map.Entry<String, List<ShopSalesVO>> item : groupMap.entrySet()) {
            List<ShopSalesVO> shopSalesList = item.getValue();
            String shopName = shopSalesList.get(0).getShopName();
            SalesGroupVO vo = new SalesGroupVO();
            vo.setName(shopName);
            List<SalesGroupBaseVO> baseList = new ArrayList<>(countrySize);
            for (CountryCountVO country : countryNameList) {
                SalesGroupBaseVO baseVO = new SalesGroupBaseVO();
                baseVO.setFlagName(country.getName());
                ShopSalesVO result = shopSalesList.stream().
                        filter(s -> s.getPlatformName().equals(country)).
                        findFirst().orElse(null);
                if (result != null) {
                    baseVO.setSales(result.getSales());
                } else {
                    baseVO.setSales(BigDecimal.ZERO);
                }
                baseList.add(baseVO);
            }
            vo.setList(baseList);
            resultList.add(vo);
        }
        return resultList;
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
    public List<SalesGroupVO> byShopCategory(BiFilterDTO dto) {
        //查询sku 分类以及分类下对应的skuno
        List<SkuCategoryVO> skuCategoryList = skuInfoService.getSkuCategoryList();
        int skuCategorySize = skuCategoryList.size();
        List<ShopSalesVO> list = baseMapper.byShopCategory(dto);
        Map<String, List<ShopSalesVO>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(ShopSalesVO::getShopNo));
        int initSize = groupMap.size();
        List<SalesGroupVO> resultList = new ArrayList<>(initSize);
        for (Map.Entry<String, List<ShopSalesVO>> item : groupMap.entrySet()) {
            List<ShopSalesVO> shopSalesList = item.getValue();
            String shopName = shopSalesList.get(0).getShopName();
            SalesGroupVO vo = new SalesGroupVO();
            vo.setName(shopName);
            List<SalesGroupBaseVO> baseList = new ArrayList<>(skuCategorySize);
            for (SkuCategoryVO category : skuCategoryList) {
                List<String> skuList = category.getSkuList();
                SalesGroupBaseVO baseVO = new SalesGroupBaseVO();
                baseVO.setFlagName(category.getName());
                BigDecimal sales = shopSalesList.stream().
                        filter(s -> skuList.contains(s.getSkuNo())).
                        map(ShopSalesVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);

                baseVO.setSales(sales);
                baseList.add(baseVO);
            }
            vo.setList(baseList);
            resultList.add(vo);
        }
        return resultList;
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
        return null;
    }


    /**
     * 一级模块-平台销售额
     *
     * @param dto
     * @return
     */
    @Override
    public List<SalesCountVO> byPlatform(BiFilterDTO dto) {
        List<SalesCountVO> list = baseMapper.byPlatform(dto);
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
        String chinaName = "中国";
        List<SalesCountVO> resultList = baseMapper.byHomeAndAbroad(dto);
        StatisticalDataVO statistical = new StatisticalDataVO();
        statistical.setName("国内外销售额占比");
        statistical.setChartType(ChartType.PIE);
        ChartVO chartVO = new ChartVO();
        chartVO.setXAxis(new ArrayList<>());
        List<SeriesVO<Object>> seriesList = new ArrayList<>(5);
        SeriesVO<Object> series = new SeriesVO();
        List<Map<String, Object>> list = new ArrayList<>();
        //国内
        Map<String, Object> chinaMap = new HashMap();
        chinaMap.put("name", "国内");
        BigDecimal chinaSales = resultList.stream().
                filter(s -> s.getName().contains(chinaName)).
                map(SalesCountVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);

        chinaMap.put("value", chinaSales);
        list.add(chinaMap);
        //国外
        Map<String, Object> abroadMap = new HashMap();
        abroadMap.put("name", "国内");
        BigDecimal abroadSales = resultList.stream().
                filter(s -> !s.getName().contains(chinaName)).
                map(SalesCountVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
        abroadMap.put("value", abroadSales);
        list.add(abroadMap);
        series.setData(Collections.singletonList(list));
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        statistical.setData(chartVO);
        return statistical;

    }


    /**
     * 一级模块 人员销售额
     * @param dto
     * @return
     */
    @Override
    public List<SalesCountVO> byPeople(BiFilterDTO dto) {
        List<SalesCountVO> resultList = baseMapper.byPeople(dto);

        return null;
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
        List<ShopSalesVO> shopSalesList = baseMapper.byShopNewAndOld(dto);
        int initSize = shopSalesList.size();
        List<ShopNewAndOldSalesVO> resultList = new ArrayList<>(initSize);
        Map<String, List<ShopSalesVO>> groupMap = shopSalesList.parallelStream().
                collect(Collectors.groupingBy(ShopSalesVO::getShopNo));
        //新品
        Integer newFlag = IsDeleted.YES;
        //老品
        Integer oldFlag = IsDeleted.NO;
        for (Map.Entry<String, List<ShopSalesVO>> item : groupMap.entrySet()) {
            ShopNewAndOldSalesVO vo = new ShopNewAndOldSalesVO();
            List<ShopSalesVO> salesList = item.getValue();
            ShopSalesVO newItem = salesList.stream().filter(s -> s.getFlag().
                    equals(newFlag)).findFirst().orElse(null);
            ShopSalesVO oldItem = salesList.stream().filter(s -> s.getFlag().
                    equals(oldFlag)).findFirst().orElse(null);
            ShopSalesVO salesVO = salesList.get(0);
            vo.setShopName(salesVO.getShopName());
            if (newItem != null) {
                vo.setNewSales(newItem.getSales());
                vo.setNewSalesQuantity(newItem.getSalesQuantity());
            }
            if (oldItem != null) {
                vo.setOldSales(oldItem.getSales());
                vo.setOldSalesQuantity(oldItem.getSalesQuantity());
            }
            resultList.add(vo);
        }
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
        List<SalesCountVO> resultList = baseMapper.byCountry(dto);
        BigDecimal totalSales = resultList.stream().
                map(SalesCountVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        for (SalesCountVO item : resultList) {

        }

        return resultList;
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
        List<SkuCategoryVO> skuCategoryList = skuInfoService.getSkuCategoryList();
        List<SalesBaseVO> list = baseMapper.byCategory(dto);
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
            BigDecimal totalSales = list.stream().filter(s -> skuList.contains(s.getFlagNo())).
                    map(SalesBaseVO::getSales).
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


    /**  销售相关  一级模块 人员周排行榜
     *
     * @author yl
     * @date 2022-12-27 11:05
     * @param
     * @return java.util.List<com.erp.model.bi.vo.PeopleSalesRankVO>
     */
    @Override
    public List<PeopleSalesRankVO> byPeopleWeekRank() {
        return null;
    }


    /**
     * 一级模块  事业部销售额s
     * @param dto
     * @return
     */
    @Override
    public List<SalesCountVO> byDept(BiFilterDTO dto) {
        List<SalesCountVO> list = baseMapper.byDept(dto);

        return null;
    }

}
