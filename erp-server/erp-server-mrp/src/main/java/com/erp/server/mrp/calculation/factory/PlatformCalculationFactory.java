package com.erp.server.mrp.calculation.factory;

import com.erp.server.mrp.calculation.strategy.CfgRuleSettingStrategy;
import com.erp.server.mrp.calculation.strategy.platform.PlatformCalculationStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 计算平台数据工厂
 */
@Component
public class PlatformCalculationFactory {

    @Resource
    private List<PlatformCalculationStrategy> strategies;

    /**
     * 根据配置获取具体处理对象
     */
    public PlatformCalculationStrategy getPlatformCalculation(String platform) {
        for (PlatformCalculationStrategy strategy : strategies) {
            if (strategy.isMatch(platform)) {
                return strategy;
            }
        }
        return null;
    }
}
