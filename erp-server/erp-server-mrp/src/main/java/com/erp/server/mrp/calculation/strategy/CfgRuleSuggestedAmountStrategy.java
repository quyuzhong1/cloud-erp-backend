package com.erp.server.mrp.calculation.strategy;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.erp.model.mrp.enums.CfgRuleSettingEnum.GET_SUGGESTED_AMOUNT;

@Component
public class CfgRuleSuggestedAmountStrategy implements CfgRuleSettingStrategy<CfgRuleCommonDTO.StrategyDTO, CfgRuleCommonDTO.StrategyResultDTO> {

    @Resource
    private CfgRuleCommonService cfgRuleCommonService;

    @Override
    @Cacheable(cacheNames = "cache:mrp:getSuggestedAmount",keyGenerator = "myKeyGenerator")
    public CfgRuleCommonDTO.StrategyResultDTO process(CfgRuleCommonDTO.StrategyDTO strategyDTO) {
        return cfgRuleCommonService.getCfgRuleCommon(strategyDTO.getPlatformType(), CfgRuleCommonTypeEnum.SUGGEST.getCode());
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_SUGGESTED_AMOUNT;
    }
}
