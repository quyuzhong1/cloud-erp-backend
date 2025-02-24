package com.erp.server.mrp.calculation.strategy;

import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.CfgRuleSettingEnum.GET_SALES_QTY;

@Component
public class CfgRuleSalesStrategy implements CfgRuleSettingStrategy<CfgRuleSalesQtyDTO.StrategyDTO, CfgRuleSalesQtyDTO.StrategyResultDTO> {

    @Override
    public CfgRuleSalesQtyDTO.StrategyResultDTO process(CfgRuleSalesQtyDTO.StrategyDTO dto) {
        CfgRuleSalesQtyEntity cfgRuleSalesQty = dto.getCfgRuleSalesQtyList()
                .stream().filter(v -> v.getRefId().equals(dto.getRefId()))
                .findFirst()
                .orElse(null);
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> formulaResults = new ArrayList<>();
        List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> denoisingResults = new ArrayList<>();
        if (!ObjectUtils.isEmpty(cfgRuleSalesQty)) {
            formulaResults = dto.getCfgRuleSalesFormulaList().stream()
                    .filter(v -> v.getSalesQtyId().equals(cfgRuleSalesQty.getId()))
                    .map(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::buildFormulaResultDTO)
                    .collect(Collectors.toList());
            denoisingResults = dto.getCfgRuleSalesDenoisingList().stream()
                    .filter(v -> v.getSalesQtyId().equals(cfgRuleSalesQty.getId()))
                    .map(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO::buildStrategyDenoisingResultDTO)
                    .collect(Collectors.toList());
        }
        CfgRuleSalesQtyEntity defaultCfgRuleSalesQty = dto.getCfgRuleSalesQtyList()
                .stream().filter(v -> v.getPlatform().equals(dto.getPlatform()))
                .findFirst()
                .orElse(new CfgRuleSalesQtyEntity());
        List<CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO> defaultDenoising = dto.getCfgRuleSalesDenoisingList().stream()
                .filter(v -> v.getSalesQtyId().equals(defaultCfgRuleSalesQty.getId()))
                .filter(v -> v.getSkuType().equals(dto.getSkuType()))
                .map(CfgRuleSalesQtyDTO.StrategyDenoisingResultDTO::buildStrategyDenoisingResultDTO)
                .collect(Collectors.toList());
        List<CfgRuleSalesQtyDTO.StrategyFormulaResultDTO> defaultFormula = dto.getCfgRuleSalesFormulaList().stream()
                .filter(v -> v.getSalesQtyId().equals(defaultCfgRuleSalesQty.getId()))
                .filter(v -> v.getSkuType().equals(dto.getSkuType()))
                .map(CfgRuleSalesQtyDTO.StrategyFormulaResultDTO::buildFormulaResultDTO)
                .collect(Collectors.toList());
        return CfgRuleSalesQtyDTO.StrategyResultDTO.buildStrategyResultDTO(defaultCfgRuleSalesQty, formulaResults, denoisingResults, defaultFormula, defaultDenoising);
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_SALES_QTY;
    }
}
