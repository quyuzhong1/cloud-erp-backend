package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.server.bi.constant.ChartType;
import com.erp.server.bi.mapper.SalesOrderServiceMapper;
import com.erp.server.bi.service.SalesOrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
                Double salesFlag = baseList.stream().
                        filter(b -> b.getFlagDate().isAfter(startTime)
                                && b.getFlagDate().isBefore(endTime)
                                && b.getFlagNo().equals(item.getName())
                        ).mapToDouble(SalesBaseVO::getSales).sum();
                salesTrend.add(new BigDecimal(salesFlag).setScale(2, RoundingMode.HALF_UP));
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
                Double salesFlag = baseList.stream().
                        filter(b -> b.getFlagDate().isAfter(startTime)
                                && b.getFlagDate().isBefore(endTime)
                                && b.getFlagNo().equals(item.getName())
                        ).mapToDouble(SalesBaseVO::getSales).sum();
                salesTrend.add(new BigDecimal(salesFlag).setScale(2, RoundingMode.HALF_UP));
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
    public StatisticalDataVO getByPlatform(BiFilterDTO dto) {
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
                Double salesFlag = baseList.stream().
                        filter(b -> b.getFlagDate().isAfter(startTime)
                                && b.getFlagDate().isBefore(endTime)
                                && b.getFlagNo().equals(item.getShopNo())
                        ).mapToDouble(SalesBaseVO::getSales).sum();
                salesTrend.add(new BigDecimal(salesFlag).setScale(2, RoundingMode.HALF_UP));
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
}
