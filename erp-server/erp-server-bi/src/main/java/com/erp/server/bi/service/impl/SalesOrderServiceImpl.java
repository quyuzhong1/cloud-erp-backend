package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.vo.ChartVO;
import com.erp.model.bi.vo.SeriesVO;
import com.erp.model.bi.vo.StatisticalDataVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.server.bi.mapper.SalesOrderServiceMapper;
import com.erp.server.bi.service.SalesOrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  销售维度 模块服务
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
        statistical.setChartType("bar");
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
}
