package com.erp.server.mrp.calculation.factory;

import com.erp.server.mrp.calculation.strategy.sales.SalesEstimateStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SalesEstimateFactory {

    @Resource
    private List<SalesEstimateStrategy> strategies;

    /**
     * 根据配置获取具体处理对象
     */
    public SalesEstimateStrategy getSalesEstimate(String platform) {
        for (SalesEstimateStrategy strategy : strategies) {
            if (strategy.isMatch(platform)) {
                return strategy;
            }
        }
        return null;
    }
}
