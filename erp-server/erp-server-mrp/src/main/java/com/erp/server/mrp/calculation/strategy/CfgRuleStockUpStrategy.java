package com.erp.server.mrp.calculation.strategy;

import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.entity.CfgRuleStockingRatioEntity;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import com.erp.server.mrp.service.CfgRuleStockingRatioService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.erp.model.mrp.enums.CfgRuleSettingEnum.GET_STOCK_UP;

/**
 * 获取备货相关设置
 */
@Component
public class CfgRuleStockUpStrategy implements CfgRuleSettingStrategy<CfgRuleStockUpDTO.StrategyDTO, CfgRuleStockUpDTO.StrategyResultDTO> {

    @Resource
    private CfgRuleStockUpService cfgRuleStockUpService;

    @Resource
    private CfgRuleStockingRatioService cfgRuleStockingRatioService;

    @Resource
    private CfgRuleLogisticsService cfgRuleLogisticsService;

    @Override
    public CfgRuleStockUpDTO.StrategyResultDTO process(CfgRuleStockUpDTO.StrategyDTO dto) {
        CfgRuleStockUpEntity cfgRuleStockUpEntity = cfgRuleStockUpService.getOneByStrategy(dto);
        List<CfgRuleStockingRatioEntity> cfgRuleStockingRatios = cfgRuleStockingRatioService.listByStockUpIdAndType(cfgRuleStockUpEntity.getId(), dto.getSkuType());
        List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> stockingRatioResultDTOS = BeanMapperUtils.copyList(CfgRuleStockingRatioDTO.StockingRatioResultDTO.class, cfgRuleStockingRatios);
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult = cfgRuleLogisticsService.getLogisticsMaxPriority(cfgRuleStockUpEntity.getId(), dto);
        CfgRuleStockUpDTO.StrategyResultDTO result = new CfgRuleStockUpDTO.StrategyResultDTO();
        result.buildStrategyResultDTO(cfgRuleStockUpEntity, stockingRatioResultDTOS, logisticsResult);
        return result;
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_STOCK_UP;
    }
}
