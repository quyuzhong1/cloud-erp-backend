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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults = new ArrayList<>();
        List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> denoisingResults = new ArrayList<>();
        if (!ObjectUtils.isEmpty(cfgRuleSalesQty)) {
            formulaResults = cfgRuleSalesFormulaService.listFormulaBySalesId(cfgRuleSalesQty.getId());
            denoisingResults = cfgRuleSalesDenoisingService.listDenoisingBySalesId(cfgRuleSalesQty.getId());
        }
        List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> defaultDenoising = dto.getDefaultDenoising().stream()
                .map(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO::buildStrategyDenoisingResultDTO)
                .collect(Collectors.toList());
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormula = dto.getDefaultFormula().stream()
                .map(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::buildFormulaResultDTO)
                .collect(Collectors.toList());
        return CfgRuleSalesQtyDTO.StrategyResultDTO.buildStrategyResultDTO(dto.getDefaultSalesQty(), formulaResults, denoisingResults, defaultFormula, defaultDenoising);
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_SALES_QTY;
    }
}
