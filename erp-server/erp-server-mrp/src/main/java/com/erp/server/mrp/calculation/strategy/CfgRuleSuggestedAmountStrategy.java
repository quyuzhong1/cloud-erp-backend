package com.erp.server.mrp.calculation.strategy;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import com.erp.server.mrp.calculation.utils.TreeUtils;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.erp.model.mrp.enums.CfgRuleSettingEnum.GET_SUGGESTED_AMOUNT;

@Component
public class CfgRuleSuggestedAmountStrategy implements CfgRuleSettingStrategy<CfgRuleCommonDTO.StrategyDTO, CfgRuleCommonDTO.StrategyResultDTO> {

    @Resource
    private CfgRuleCommonService cfgRuleCommonService;

    @Override
    public CfgRuleCommonDTO.StrategyResultDTO process(CfgRuleCommonDTO.StrategyDTO strategyDTO) {
        CfgRuleCommonDTO.StrategyResultDTO cfgRuleCommon = cfgRuleCommonService.getCfgRuleCommon(strategyDTO.getPlatformType(), CfgRuleCommonTypeEnum.SUGGEST.getCode());
        TreeUtils.initCache(cfgRuleCommon);
        return cfgRuleCommon;
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_SUGGESTED_AMOUNT;
    }
}
