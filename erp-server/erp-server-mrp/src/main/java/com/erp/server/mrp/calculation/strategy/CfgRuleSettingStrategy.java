package com.erp.server.mrp.calculation.strategy;

import com.erp.model.mrp.enums.CfgRuleSettingEnum;

public interface CfgRuleSettingStrategy<T, R> {

    /**
     * 处理逻辑
     *
     * @param t 参数
     */
    R process(T t);

    default boolean isMatch(String cfgRuleSetting) {
        return getCfgRuleSetting().getCode().equals(cfgRuleSetting);
    }

    /**
     * 任务来源
     *
     * @return {@link CfgRuleSettingEnum}
     */
    CfgRuleSettingEnum getCfgRuleSetting();
}
