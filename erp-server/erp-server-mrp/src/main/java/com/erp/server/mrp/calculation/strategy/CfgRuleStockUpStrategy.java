package com.erp.server.mrp.calculation.strategy;

import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.CfgRuleSettingEnum.GET_STOCK_UP;

/**
 * 获取备货相关设置
 */
@Component
public class CfgRuleStockUpStrategy implements CfgRuleSettingStrategy<CfgRuleStockUpDTO.StrategyDTO, CfgRuleStockUpDTO.StrategyResultDTO> {


    @Override
    public CfgRuleStockUpDTO.StrategyResultDTO process(CfgRuleStockUpDTO.StrategyDTO dto) {

        CfgRuleStockUpEntity cfgRuleStockUp = dto.getCfgRuleStockUpList()
                .stream()
                .filter(v -> v.getRefId().equals(dto.getRefId()))
                .findFirst()
                .orElse(null);

        List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> cfgRuleStockingRatios = new ArrayList<>();
        if (!ObjectUtils.isEmpty(cfgRuleStockUp)) {
            cfgRuleStockingRatios = dto.getCfgRuleStockingRatioList().stream()
                    .filter(v -> v.getStockUpId().equals(cfgRuleStockUp.getId()))
                    .map(CfgRuleStockingRatioDTO.StockingRatioResultDTO::buildStockingRatioResult)
                    .collect(Collectors.toList());
        }
        CfgRuleStockUpEntity defaultStockUp = dto.getCfgRuleStockUpList()
                .stream()
                .filter(v -> ObjectUtils.isEmpty(v.getRefId()))
                .filter(v -> v.getPlatform().equals(dto.getPlatform()))
                .findFirst()
                .orElse(new CfgRuleStockUpEntity());
        CfgRuleStockUpDTO.StrategyResultDTO result = new CfgRuleStockUpDTO.StrategyResultDTO();
        List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> defaultRatio = dto.getCfgRuleStockingRatioList().stream()
                .filter(v -> v.getStockUpId().equals(defaultStockUp.getId()))
                .filter(v -> v.getType().equals(dto.getSkuType()))
                .map(CfgRuleStockingRatioDTO.StockingRatioResultDTO::buildStockingRatioResult)
                .collect(Collectors.toList());
        result.buildStrategyResultDTO(dto, cfgRuleStockUp, defaultStockUp, cfgRuleStockingRatios, defaultRatio);
        return result;
    }


    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_STOCK_UP;
    }
}
