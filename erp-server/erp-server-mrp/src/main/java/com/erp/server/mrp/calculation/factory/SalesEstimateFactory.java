package com.erp.server.mrp.calculation.factory;

import com.erp.server.mrp.calculation.strategy.sales.SalesEstimateStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SalesEstimateFactory<T, R> {

//    @Resource
    private List<SalesEstimateStrategy<T, R>> strategies;

    /**
     * 根据配置获取具体处理对象
     */
    public SalesEstimateStrategy<T, R> getSalesEstimate(String platform) {
        for (SalesEstimateStrategy<T, R> strategy : strategies) {
            if (strategy.isMatch(platform)) {
                return strategy;
            }
        }
        return null;
    }
}
