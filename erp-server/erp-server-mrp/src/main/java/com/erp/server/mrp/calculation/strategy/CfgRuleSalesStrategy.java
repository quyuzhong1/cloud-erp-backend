package com.erp.server.mrp.calculation.strategy;

import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingService;
import com.erp.server.mrp.service.CfgRuleSalesFormulaService;
import com.erp.server.mrp.service.CfgRuleSalesQtyService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

import static com.erp.model.mrp.enums.CfgRuleSettingEnum.GET_SALES_QTY;

@Component
public class CfgRuleSalesStrategy implements CfgRuleSettingStrategy<CfgRuleSalesQtyDTO.StrategyDTO, CfgRuleSalesQtyDTO.StrategyResultDTO> {
    @Resource
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;
    @Resource
    private CfgRuleSalesFormulaService cfgRuleSalesFormulaService;
    @Resource
    private CfgRuleSalesDenoisingService cfgRuleSalesDenoisingService;

    @Override
    public CfgRuleSalesQtyDTO.StrategyResultDTO process(CfgRuleSalesQtyDTO.StrategyDTO dto) {
        CfgRuleSalesQtyEntity cfgRuleSalesQty = cfgRuleSalesQtyService.getByRefId(dto.getRefId());
        if (ObjectUtils.isEmpty(cfgRuleSalesQty)) {
            //获取默认配置
            return cfgRuleSalesQtyService.getDefaultCfgRuleSalesQty(dto.getPlatformType(), dto.getSkuType());
        }
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults = cfgRuleSalesFormulaService.listFormulaBySalesId(cfgRuleSalesQty.getId());
        List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> denoisingResults = cfgRuleSalesDenoisingService.listDenoisingBySalesId(cfgRuleSalesQty.getId());
        return CfgRuleSalesQtyDTO.StrategyResultDTO.buildStrategyResultDTO(cfgRuleSalesQty, formulaResults, denoisingResults);
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_SALES_QTY;
    }
}
