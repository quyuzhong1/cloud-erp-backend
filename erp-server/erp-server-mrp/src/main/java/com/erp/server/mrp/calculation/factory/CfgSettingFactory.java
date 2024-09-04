package com.erp.server.mrp.calculation.factory;

import com.erp.server.mrp.calculation.strategy.CfgRuleSettingStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class CfgSettingFactory {

    @Resource
    private List<CfgRuleSettingStrategy<?,?>> handlers;

    /**
     * 根据配置获取具体处理对象
     * @param cfgRuleSetting 配置，
     */
    public CfgRuleSettingStrategy<?,?> getCfgRuleSettingHandler(String cfgRuleSetting) {
        for (CfgRuleSettingStrategy<?,?> handler : handlers) {
            if (handler.isMatch(cfgRuleSetting)) {
                return handler;
            }
        }
        return null;
    }
}
