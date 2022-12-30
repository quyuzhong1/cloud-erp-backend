package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.constant.BiConstant;
import com.erp.server.bi.constant.ChartType;
import com.erp.server.bi.constant.IsDeleted;
import com.erp.server.bi.enums.SiteEnum;
import com.erp.server.bi.mapper.SalesOrderServiceMapper;
import com.erp.server.bi.service.BiSkuInfoService;
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
        LocalDateTime now = LocalDateTime.now();

        dto.setStartTime(now.minusYears(3));
        dto.setEndTime(now);
        List<Map<String, Object>> resultList = baseMapper.getMonthSales(dto);
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
            BigDecimal sales = item.getSales();
            Integer orderCount = item.getOrderCount();
            //客单价
            BigDecimal perCustomerTransaction = sales.divide(new BigDecimal(orderCount), 2, BigDecimal.ROUND_HALF_UP);
            item.setPerCustomerTransaction(perCustomerTransaction);
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
        List<SalesCountVO> list = baseMapper.byBrand(dto);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = startTime;
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        List<SalesCountVO> chainList = baseMapper.byBrand(dto);

        //同比开始时间
        LocalDateTime yearBasisStartTime = startTime.minusYears(1);
        //同比开始时间
        LocalDateTime yearBasisEndTime = endTime.minusYears(1);
        dto.setStartTime(yearBasisStartTime);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesCountVO> yearBasisList = baseMapper.byPlatform(dto);
        //总的销售额
        BigDecimal totalSales = list.stream().
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
                item.setYearBasisRatio(getChainRelativeRatio(sales, chainVO.getSales()));
            }
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
        List<SalesCountVO> list = baseMapper.byPlatform(dto);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = startTime;
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        List<SalesCountVO> chainList = baseMapper.byPlatform(dto);

        //同比开始时间
        LocalDateTime yearBasisStartTime = startTime.minusYears(1);
        //同比开始时间
        LocalDateTime yearBasisEndTime = endTime.minusYears(1);
        dto.setStartTime(yearBasisStartTime);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesCountVO> yearBasisList = baseMapper.byPlatform(dto);

        //总的销售额
        BigDecimal totalSales = list.stream().
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
                item.setYearBasisRatio(getChainRelativeRatio(sales, chainVO.getSales()));
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
     *
     * @param dto
     * @return
     */
    @Override
    public List<SalesCountVO> byPeople(BiFilterDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        List<SalesBaseVO> list = baseMapper.byPeople(dto);

        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = startTime;
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        //这个是环比的查询出来的
        List<SalesBaseVO> chainList = baseMapper.byPeople(dto);


        //同比开始时间
        LocalDateTime yearBasisStartTime = startTime.minusYears(1);
        //同比开始时间
        LocalDateTime yearBasisEndTime = endTime.minusYears(1);
        dto.setStartTime(yearBasisStartTime);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesBaseVO> yearBasisList = baseMapper.byPeople(dto);

        List<SalesCountVO> resultList = new ArrayList<>(list.size());
        //总的
        BigDecimal totalSales = list.stream().
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
                vo.setYearBasisRatio(getChainRelativeRatio(sales, chainVO.getSales()));
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
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = startTime;
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);

        //这个是环比的查询出来的
        List<SalesCountVO> chainList = baseMapper.byCountry(dto);

        //同比开始时间
        LocalDateTime yearBasisStartTime = startTime.minusYears(1);
        //同比开始时间
        LocalDateTime yearBasisEndTime = endTime.minusYears(1);
        dto.setStartTime(yearBasisStartTime);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesCountVO> yearBasisList = baseMapper.byCountry(dto);

        //总的
        BigDecimal totalSales = resultList.stream().
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
                item.setYearBasisRatio(getChainRelativeRatio(sales, chainVO.getSales()));
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
        BigDecimal ratio = sales.divide(totalSales, 5, BigDecimal.ROUND_HALF_UP);
        return ratio.multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取销售环比
     * 本期销售额-同期销售额/同期销售额 *100
     *
     * @return
     */
    public BigDecimal getChainRelativeRatio(BigDecimal sales, BigDecimal oldSales) {
        BigDecimal differ = sales.subtract(oldSales);
        BigDecimal zero = BigDecimal.ZERO;
        if (oldSales.compareTo(zero) == 0) {
            return zero;
        }
        BigDecimal ratio = differ.divide(oldSales, 5, BigDecimal.ROUND_HALF_UP);
        return ratio.multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
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
        List<SalesBaseVO> list = baseMapper.byPeopleRank(dto);

        //上周开始时间
        LocalDateTime lastWeekStart = LocalDateUtil.getLastWeekStart(nowDate);
        //上周结束时间
        LocalDateTime lastWeekEnd = LocalDateUtil.getLastWeekEnd(nowDate);
        dto.setStartTime(lastWeekStart);
        dto.setEndTime(lastWeekEnd);
        //上周查询结果
        List<SalesBaseVO> lastList = baseMapper.byPeopleRank(dto);
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
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        LocalDate nowDate = LocalDate.now();
        //本月开始时间
        LocalDateTime monthStart = LocalDateUtil.getThisMonthStart(nowDate);
        //本月结束时间
        LocalDateTime monthEnd = LocalDateUtil.getThisMonthEnd(nowDate);
        dto.setStartTime(monthStart);
        dto.setEndTime(monthEnd);
        //本月结果
        List<SalesBaseVO> list = baseMapper.byPeopleRank(dto);

        //上月开始时间
        LocalDateTime lastMonthStart = LocalDateUtil.getLastMonthStart(nowDate);
        //上月结束时间
        LocalDateTime lastMonthEnd = LocalDateUtil.getLastMonthEnd(nowDate);
        dto.setStartTime(lastMonthStart);
        dto.setEndTime(lastMonthEnd);
        //上月查询结果
        List<SalesBaseVO> lastList = baseMapper.byPeopleRank(dto);
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
        LocalDate nowDate = LocalDate.now();
        //本季度开始时间
        LocalDateTime quarterStart = LocalDateUtil.getThisQuarterStart(nowDate);
        //本季度结束时间
        LocalDateTime quarterEnd = LocalDateUtil.getThisQuarterEnd(nowDate);
        dto.setStartTime(quarterStart);
        dto.setEndTime(quarterEnd);
        //本月结果
        List<SalesBaseVO> list = baseMapper.byPeopleRank(dto);

        //上季度开始时间
        LocalDateTime lastQuarterStart = LocalDateUtil.getLastQuarterStart(nowDate);
        //上季度结束时间
        LocalDateTime lastQuarterEnd = LocalDateUtil.getLastQuarterEnd(nowDate);
        dto.setStartTime(lastQuarterStart);
        dto.setEndTime(lastQuarterEnd);
        //上季度查询结果
        List<SalesBaseVO> lastList = baseMapper.byPeopleRank(dto);
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
        //本年度 开始时间
        LocalDateTime yearStart = LocalDateUtil.getThisYearStart(nowDate);
        //本年度 结束时间
        LocalDateTime yearEnd = LocalDateUtil.getThisYearEnd(nowDate);
        dto.setStartTime(yearStart);
        dto.setEndTime(yearEnd);
        //本年结果
        List<SalesBaseVO> list = baseMapper.byPeopleRank(dto);

        //上年开始时间
        LocalDateTime lastYearStart = LocalDateUtil.getLastYearStart(nowDate);
        //上年结束时间
        LocalDateTime lastYearEnd = LocalDateUtil.getLastYearEnd(nowDate);
        dto.setStartTime(lastYearStart);
        dto.setEndTime(lastYearEnd);
        //上年查询结果
        List<SalesBaseVO> lastList = baseMapper.byPeopleRank(dto);
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
        List<SalesBaseVO> list = baseMapper.byDept(dto);
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();

        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = startTime;
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        //这个是环比的查询出来的
        List<SalesBaseVO> chainList = baseMapper.byDept(dto);

        //同比开始时间
        LocalDateTime yearBasisStartTime = startTime.minusYears(1);
        //同比开始时间
        LocalDateTime yearBasisEndTime = endTime.minusYears(1);
        dto.setStartTime(yearBasisStartTime);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesBaseVO> yearBasisList = baseMapper.byDept(dto);

        List<SalesCountVO> resultList = new ArrayList<>(list.size());
        //总的
        BigDecimal totalSales = list.stream().
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
                vo.setYearBasisRatio(getChainRelativeRatio(sales, chainVO.getSales()));
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
        List<SalesFlagVO> list = baseMapper.byDeptNewAndOld(dto);
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
                    filter(s -> s.getFlag().equals(newFlag)).
                    map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal oldProductSales = salesList.stream().
                    filter(s -> s.getFlag().equals(oldFlag)).
                    map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setNewProductSales(newProductSales);
            vo.setOldProductSales(oldProductSales);
            Integer newSalesQuantity=salesList.stream().
                    filter(s -> s.getFlag().equals(newFlag)).
                    mapToInt(SalesFlagVO::getSalesQuantity).
                    sum();
            Integer oldSalesQuantity=salesList.stream().
                    filter(s -> s.getFlag().equals(oldFlag)).
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
        List<SalesCountVO> list = baseMapper.byNewAndOld(dto);
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = startTime;
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        //这个是环比的查询出来的
        List<SalesCountVO> chainList = baseMapper.byNewAndOld(dto);
        //同比开始时间
        LocalDateTime yearBasisStartTime = startTime.minusYears(1);
        //同比开始时间
        LocalDateTime yearBasisEndTime = endTime.minusYears(1);
        dto.setStartTime(yearBasisStartTime);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<SalesCountVO> yearBasisList = baseMapper.byNewAndOld(dto);
        //总的
        BigDecimal totalSales = list.stream().
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
                item.setYearBasisRatio(getChainRelativeRatio(sales, chainVO.getSales()));
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

        //新品
        Integer newFlag = IsDeleted.YES;
        //老品
        Integer oldFlag = IsDeleted.NO;

        List<SalesFlagVO> list = baseMapper.byPlatformNewAndOld(dto);

        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = startTime;
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);
        List<SalesFlagVO> chainList = baseMapper.byPlatformNewAndOld(dto);

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
                        filter(c -> name.equals(c.getName()) && c.getFlag().equals(newFlag)).
                        map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                vo.setNewProductChainRelativeRatio(getChainRelativeRatio(newItem.getSales(), newChainSales));
            }
            if (oldItem != null) {
                vo.setOldProductSales(oldItem.getSales());
                vo.setOldSalesQuantity(oldItem.getSalesQuantity());
                BigDecimal oldChainSales = chainList.stream().
                        filter(c -> name.equals(c.getName()) && c.getFlag().equals(oldFlag)).
                        map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
                vo.setOldProductChainRelativeRatio(getChainRelativeRatio(oldItem.getSales(), oldChainSales));
            }
            resultList.add(vo);
        }
        return resultList;
    }

    @Override
    public List<ProductNewAndOldVO> byPeopleNewAndOld(BiFilterDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();

        //新品
        Integer newFlag = BiConstant.NEW;
        //老品
        Integer oldFlag = BiConstant.OLD;

        List<SalesFlagVO> list = baseMapper.byPeopleNewAndOld(dto);

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
                            equals(newFlag)).map(SalesFlagVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer newItemSalesQuantity = salesFlagList.stream().
                    filter(s -> s.getFlag().equals(newFlag)).
                    mapToInt(SalesFlagVO::getSalesQuantity).sum();
            vo.setNewProductSales(newItemSales);
            vo.setNewSalesQuantity(newItemSalesQuantity);
            BigDecimal oldItemSales = salesFlagList.stream().
                    filter(s -> s.getFlag().equals(oldFlag)).
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
        Integer newFlag = IsDeleted.YES;
        //老品
        Integer oldFlag = IsDeleted.NO;

        List<ProductNewAndOldVO> resultList = new ArrayList<>(10);
        //查询sku 分类以及分类下对应的skuno
        List<SkuCategoryVO> skuCategoryList = skuInfoService.getSkuCategoryList();
        List<SalesFlagVO> list = baseMapper.byCategoryNewAndOld(dto);
        for (SkuCategoryVO item : skuCategoryList) {
            ProductNewAndOldVO vo = new ProductNewAndOldVO();
            vo.setName(item.getName());
            List<String> skuList = item.getSkuList();
            BigDecimal newProductSales = list.stream()
                    .filter(s -> skuList.contains(s.getSkuNo()) && newFlag.equals(s.getFlag()))
                    .map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setNewProductSales(newProductSales);
            Integer newSalesQuantity = list.stream()
                    .filter(s -> skuList.contains(s.getSkuNo()) && newFlag.equals(s.getFlag()))
                    .mapToInt(SalesFlagVO::getSalesQuantity).sum();
            vo.setNewSalesQuantity(newSalesQuantity);

            BigDecimal oldProductSales = list.stream()
                    .filter(s -> skuList.contains(s.getSkuNo()) && oldFlag.equals(s.getFlag()))
                    .map(SalesFlagVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
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
        List<ShopSalesVO> list = baseMapper.bySite(dto);
        //获取到环比的开始日期
        LocalDateTime ringRatioStartDate = LocalDateUtil.getRingRatioDate(startTime, endTime);
        //获取到环比的结束日期
        LocalDateTime ringRatioEndDate = startTime;
        dto.setStartTime(ringRatioStartDate);
        dto.setEndTime(ringRatioEndDate);

        //这个是环比的查询出来的
        List<ShopSalesVO> chainList = baseMapper.bySite(dto);

        //同比开始时间
        LocalDateTime yearBasisStartTime = startTime.minusYears(1);
        //同比开始时间
        LocalDateTime yearBasisEndTime = endTime.minusYears(1);
        dto.setStartTime(yearBasisStartTime);
        dto.setEndTime(yearBasisEndTime);
        //这是同比查询出来的
        List<ShopSalesVO> yearBasisList = baseMapper.bySite(dto);

        //总的
        BigDecimal totalSales = list.stream().
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
                    filter(s -> shopNoList.contains(s.getShopNo())).
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
                    filter(c -> shopNoList.contains(c.getShopNo())).
                    map(ShopSalesVO::getSales).
                    reduce(BigDecimal.ZERO, BigDecimal::add);

            vo.setChainRelativeRatio(getChainRelativeRatio(sales, chainSales));

            BigDecimal yearBasisSales = yearBasisList.stream().
                    filter(c -> shopNoList.contains(c.getShopNo())).
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

        List<SalesBaseVO> list = baseMapper.byCategory(dto);
        //获取到sku 属性分类
        List<SkuCategoryVO> itemPropertyList = skuInfoService.getSkuPropertyList();
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
        List<Map<String, Object>> dataList = new ArrayList<>();
        // BiConstant.HOMEMADE
        //自研
        Map<String, Object> homemadeMap = new HashMap<>();
        homemadeMap.put("name", "自研");
        BigDecimal homemadeSales = list.stream().
                filter(s -> skuHomemadeList.contains(s.getFlagNo())).
                map(SalesBaseVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        homemadeMap.put("value", homemadeSales);
        dataList.add(homemadeMap);

        //外采
        Map<String, Object> purchaseMap = new HashMap<>();
        purchaseMap.put("name", "外采");
        BigDecimal purchaseSales = list.stream().
                filter(s -> skuPurchaseList.contains(s.getFlagNo())).
                map(SalesBaseVO::getSales).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        purchaseMap.put("value", purchaseSales);
        dataList.add(purchaseMap);
        series.setData(Collections.singletonList(dataList));
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
        List<SalesBaseVO> list = baseMapper.byOldProductTop(dto);
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
        List<SalesBaseVO> list = baseMapper.byNewProductTop(dto);
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
        String deptName = "营销中心";
        List<String> deptIdList = sysUserFeign.getDeptIdsByName(deptName);
        return null;
    }

    /**
     * @param dto
     * @return
     */
    @Override
    public StatisticalDataVO byEuropeAndJapanSite(BiFilterDTO dto) {
        List<ShopSalesVO> list = baseMapper.byShop(dto);
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
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (String siteName : siteNameList) {
            Map<String, Object> siteMap = new HashMap<>();
            siteMap.put("name", siteName);
            //根据 站点名获取大盘站点信息
            List<String> siteList = SiteEnum.getSiteList(siteName);
            List<ShopSiteVO> siteShopList = shopCategoryList.stream().filter(s -> siteList.contains(s.getSite()))
                    .collect(Collectors.toList());
            List<String> shopNoList = siteShopList.stream().flatMap(s -> s.getShopNo().stream()).collect(Collectors.toList());
            BigDecimal value = list.stream().filter(s -> shopNoList.contains(s.getShopNo())).
                    map(ShopSalesVO::getSales).reduce(BigDecimal.ZERO, BigDecimal::add);
            siteMap.put("value", value);
            dataList.add(siteMap);
        }
        series.setData(Collections.singletonList(dataList));
        seriesList.add(series);
        chartVO.setSeries(seriesList);
        chartVO.setXAxis(siteNameList);
        statistical.setData(chartVO);
        return statistical;
    }


}
