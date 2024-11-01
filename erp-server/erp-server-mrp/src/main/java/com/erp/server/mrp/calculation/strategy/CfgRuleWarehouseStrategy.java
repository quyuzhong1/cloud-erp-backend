package com.erp.server.mrp.calculation.strategy;

import com.erp.model.mrp.dto.CfgRuleWarehouseDTO;
import com.erp.model.mrp.entity.CfgRuleWarehouseDetailEntity;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import com.erp.model.mrp.enums.CfgRuleWarehouseTypeEnum;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.server.mrp.service.CfgRuleWarehouseDetailService;
import com.erp.server.mrp.service.CfgRuleWarehouseService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.CfgRuleSettingEnum.GET_WAREHOUSE;

@Component
public class CfgRuleWarehouseStrategy implements CfgRuleSettingStrategy<CfgRuleWarehouseDTO.StrategyDTO, CfgRuleWarehouseDTO.StrategyResultDTO> {

    @Resource
    private CfgRuleWarehouseService cfgRuleWarehouseService;
    @Resource
    private CfgRuleWarehouseDetailService cfgRuleWarehouseDetailService;

    @Override
    @Cacheable(cacheNames = "cache:mrp:getWarehouse",keyGenerator = "myKeyGenerator")
    public CfgRuleWarehouseDTO.StrategyResultDTO process(CfgRuleWarehouseDTO.StrategyDTO strategyDTO) {
        CfgRuleWarehouseDTO.StrategyResultDTO strategyResultDTO = new CfgRuleWarehouseDTO.StrategyResultDTO();
        CfgRuleWarehouseEntity cfgRuleWarehouse = cfgRuleWarehouseService.getByPlatformType(strategyDTO.getPlatformType());
        strategyResultDTO.setIsEnableOverseas(cfgRuleWarehouse.getIsEnableOverseas());
        strategyResultDTO.setIsEnableVirtual(cfgRuleWarehouse.getIsEnableVirtual());
        List<CfgRuleWarehouseDetailEntity> cfgRuleWarehouseDetailList = cfgRuleWarehouseDetailService.listByMainIdList(Collections.singletonList(cfgRuleWarehouse.getId()));
        //开启了虚拟仓
        if (Boolean.TRUE.equals(cfgRuleWarehouse.getIsEnableVirtual())) {
            List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> localWarehouse = cfgRuleWarehouseDetailList.stream()
                    .filter(v -> CfgRuleWarehouseTypeEnum.LOCAL.getCode().equals(v.getWarehouseType()))
                    .filter(v -> !ObjectUtils.isEmpty(v.getVirtualWarehouseId()))
                    .filter(v -> (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(v.getChannelType()) && v.getChannelIdJson().contains(strategyDTO.getPlatform()))
                            || v.getChannelIdJson().contains(strategyDTO.getShopId()))
                    .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::buildStrategyDetailResultDTO)
                    .collect(Collectors.toList());
            strategyResultDTO.setLocalWarehouseList(localWarehouse);
        } else {
            List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> localWarehouse = cfgRuleWarehouseDetailList.stream()
                    .filter(v -> CfgRuleWarehouseTypeEnum.LOCAL.getCode().equals(v.getWarehouseType()))
                    .filter(v -> ObjectUtils.isEmpty(v.getVirtualWarehouseId()))
                    .filter(v -> (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(v.getChannelType()) && v.getChannelIdJson().contains(strategyDTO.getPlatform()))
                            || v.getChannelIdJson().contains(strategyDTO.getShopId()))
                    .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::buildStrategyDetailResultDTO)
                    .collect(Collectors.toList());
            strategyResultDTO.setLocalWarehouseList(localWarehouse);
        }
        //开启了海外仓
        if (Boolean.TRUE.equals(cfgRuleWarehouse.getIsEnableOverseas()) || CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(strategyDTO.getPlatform())) {
            List<CfgRuleWarehouseDTO.StrategyDetailResultDTO> overseasWarehouse = cfgRuleWarehouseDetailList.stream()
                    .filter(v -> CfgRuleWarehouseTypeEnum.OVERSEAS.getCode().equals(v.getWarehouseType()))
                    .filter(v -> (VitualWarehouseChannelTypeEnum.PLATFORM.getCode().equals(v.getChannelType()) && v.getChannelIdJson().contains(strategyDTO.getPlatform()))
                            || v.getChannelIdJson().contains(strategyDTO.getShopId()))
                    .map(CfgRuleWarehouseDTO.StrategyDetailResultDTO::buildStrategyDetailResultDTO)
                    .collect(Collectors.toList());
            strategyResultDTO.setOverseasWarehouseList(overseasWarehouse);
        }
        return strategyResultDTO;
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_WAREHOUSE;
    }
}
