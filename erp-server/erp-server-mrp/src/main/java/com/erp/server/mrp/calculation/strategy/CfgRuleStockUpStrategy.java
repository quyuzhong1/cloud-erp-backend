package com.erp.server.mrp.calculation.strategy;

import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import com.erp.server.mrp.service.CfgRuleStockingRatioService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

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
        CfgRuleStockUpEntity cfgRuleStockUp = cfgRuleStockUpService.getByRefId(dto.getRefId());
        if (ObjectUtils.isEmpty(cfgRuleStockUp)) {
            //获取默认配置
            cfgRuleStockUp = cfgRuleStockUpService.getDefaultCfgRuleStockUp(dto.getPlatformType());
        }
        List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> cfgRuleStockingRatios = cfgRuleStockingRatioService.listByStockUpIdAndType(cfgRuleStockUp.getId(), dto.getSkuType());
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult = cfgRuleLogisticsService.getLogisticsMaxPriority(cfgRuleStockUp.getId(), dto.getPlatformType(), dto.getSkuType(), dto.getArea(), dto.getWarehouseId());
        CfgRuleStockUpDTO.StrategyResultDTO result = new CfgRuleStockUpDTO.StrategyResultDTO();
        result.buildStrategyResultDTO(cfgRuleStockUp, cfgRuleStockingRatios, logisticsResult);
        return result;
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_STOCK_UP;
    }
}
