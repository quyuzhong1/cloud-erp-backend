package com.erp.server.mrp.calculation.strategy;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import com.erp.server.mrp.calculation.utils.TreeUtils;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.erp.model.mrp.enums.CfgRuleSettingEnum.GET_INVENTORY;

@Component
public class CfgRuleInventoryStrategy implements CfgRuleSettingStrategy<CfgRuleCommonDTO.StrategyDTO, List<CfgRuleCommonDTO.StrategyResultDTO>> {

    @Resource
    private CfgRuleCommonService cfgRuleCommonService;

    @Override
    public List<CfgRuleCommonDTO.StrategyResultDTO> process(CfgRuleCommonDTO.StrategyDTO strategyDTO) {
        List<CfgRuleCommonDTO.StrategyResultDTO> cfgRuleCommon = cfgRuleCommonService.getCfgRuleCommon(strategyDTO.getPlatformType(), CfgRuleCommonTypeEnum.INVENTORY.getCode());
        TreeUtils.initCache(cfgRuleCommon);
        return cfgRuleCommon;
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_INVENTORY;
    }
}
