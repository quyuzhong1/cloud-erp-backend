package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.CfgSettingDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgSettingEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SalesEstimateHandler extends AbstractSkuCalculationHandler {
    @Resource
    private OverseasUsableHandler overseasUsableHandler;

    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return overseasUsableHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults = cfgRuleStrategyDTO.getSalesQtyResult().getFormulaResults();
        //计算天数
        int days = cfgRuleStrategyDTO.getSettings()
                .stream().filter(v -> v.getKey().equals(CfgSettingEnum.CALCULATION_DAYS.getCode()))
                .map(CfgSettingDTO::getDataJson)
                .map(Integer::parseInt)
                .findFirst().orElse(0);
        for (int i = 0; i < days; i++) {



        }

    }
}
