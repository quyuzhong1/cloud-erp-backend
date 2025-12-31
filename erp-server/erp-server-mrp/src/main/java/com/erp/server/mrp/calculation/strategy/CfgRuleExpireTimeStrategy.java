package com.erp.server.mrp.calculation.strategy;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.dto.CfgRuleExpireTimeDTO;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import com.erp.model.mrp.entity.CfgRuleOverseasInstockDaysEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.erp.model.mrp.enums.CfgRuleSettingEnum.GET_EXPIRE_TIME;

/**
 * 获取备货相关设置
 */
@Component
public class CfgRuleExpireTimeStrategy implements CfgRuleSettingStrategy<CfgRuleExpireTimeDTO.StrategyDTO, CfgRuleExpireTimeDTO.StrategyResultDTO> {
    @Override
    public CfgRuleExpireTimeDTO.StrategyResultDTO process(CfgRuleExpireTimeDTO.StrategyDTO dto) {
        CfgRuleExpireTimeEntity cfgRuleExpireTime = dto.getCfgRuleExpireTimeList().stream()
                .filter(v -> v.getRefId().equals(dto.getRefId()))
                .findFirst()
                .orElse(null);

        List<CfgRuleLogisticsEntity> ruleLogisticsList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(cfgRuleExpireTime)) {
            ruleLogisticsList = dto.getCfgRuleLogisticsList().stream()
                    .filter(v -> v.getExpireTimeId().equals(cfgRuleExpireTime.getId()))
                    .collect(Collectors.toList());
        }
        CfgRuleExpireTimeEntity defaultCfgRuleExpireTime = dto.getCfgRuleExpireTimeList().stream()
                .filter(v -> ObjectUtils.isEmpty(v.getRefId()))
                .findFirst()
                .orElse(new CfgRuleExpireTimeEntity());
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult = ruleLogisticsList.stream()
                .min(Comparator.comparing(CfgRuleLogisticsEntity::getIndex))
                .map(CfgRuleLogisticsDTO.LogisticsResultDTO::buildLogisticsResult).orElse(getLogisticsMaxPriority(dto, defaultCfgRuleExpireTime.getId()));
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMinResult = ruleLogisticsList.stream()
                .min(Comparator.comparing(v -> v.getLogisticsDays() + v.getLogisticsCycleDays()))
                .map(CfgRuleLogisticsDTO.LogisticsResultDTO::buildLogisticsResult).orElse(getMinLogistics(dto, defaultCfgRuleExpireTime.getId()));
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMaxResult = ruleLogisticsList.stream()
                .max(Comparator.comparing(v -> v.getLogisticsDays() + v.getLogisticsCycleDays()))
                .map(CfgRuleLogisticsDTO.LogisticsResultDTO::buildLogisticsResult).orElse(getMaxLogistics(dto, defaultCfgRuleExpireTime.getId()));
        int instockDays;
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(dto.getPlatformType())) {
            instockDays = CfgRuleExpireTimeDTO.getOrDefault(cfgRuleExpireTime, CfgRuleExpireTimeEntity::getPlatformInstockDays, defaultCfgRuleExpireTime.getPlatformInstockDays());
        } else {
            if (dto.getRefId().equals("1876099794825076745")) {
                System.out.println("1876099794825076745");
            }
            instockDays = CfgRuleExpireTimeDTO.getOrDefault(cfgRuleExpireTime, CfgRuleExpireTimeEntity::getOverseasInstockDays, getDefaultInStockDays(dto.getCfgRuleOverseasInStockDaysList(), dto.getWarehouseId(),defaultCfgRuleExpireTime.getOverseasInstockDays()));
        }
        CfgRuleExpireTimeDTO.StrategyResultDTO result = new CfgRuleExpireTimeDTO.StrategyResultDTO();
        result.buildStrategyResultDTO(cfgRuleExpireTime, defaultCfgRuleExpireTime,  logisticsResult, logisticsMaxResult, logisticsMinResult, instockDays);
        return result;
    }

    private Integer getDefaultInStockDays(List<CfgRuleOverseasInstockDaysEntity> cfgRuleOverseasInStockDaysList, List<String> warehouseIdList, Integer overseasInstockDays) {
        return cfgRuleOverseasInStockDaysList.stream()
                .filter(v -> warehouseIdList.contains(v.getWarehouseId()))
                .map(CfgRuleOverseasInstockDaysEntity::getInstockDays)
                .max(Integer::compareTo)
                .orElse(overseasInstockDays);
    }

    private CfgRuleLogisticsDTO.LogisticsResultDTO getMaxLogistics(CfgRuleExpireTimeDTO.StrategyDTO dto, String expireTimeId) {
        List<CfgRuleLogisticsDTO.LogisticsResultDTO> allLogistics = getAllLogistics(dto, expireTimeId);
        return allLogistics.stream()
                .max(Comparator.comparing(v -> v.getLogisticsCycleDays() + v.getLogisticsDays()))
                .orElseThrow(() -> new ServiceException(ApiError.LOGISTICS_CONFIG_NOT_FOUND));
    }

    private static List<CfgRuleLogisticsDTO.LogisticsResultDTO> getAllLogistics(CfgRuleExpireTimeDTO.StrategyDTO dto, String expireTimeId) {
        List<CfgRuleLogisticsEntity> cfgRuleLogisticsList = dto.getCfgRuleLogisticsList().stream()
                .filter(v -> v.getExpireTimeId().equals(expireTimeId))
                .filter(v -> v.getPlatformType().equals(dto.getPlatformType()))
                .collect(Collectors.toList());
        List<CfgRuleLogisticsDTO.LogisticsResultDTO> dtos = new ArrayList<>();

        for (CfgRuleLogisticsEntity logistic : cfgRuleLogisticsList) {
            dtos.add(CfgRuleLogisticsDTO.LogisticsResultDTO.buildLogisticsResult(logistic));
            //获取明细数据
            List<CfgRuleLogisticsDetailEntity> ruleLogisticsDetails = getCfgRuleLogisticsDetails(dto, logistic.getId(), dto.getCfgRuleLogisticsDetailList());
            if (!CollectionUtils.isEmpty(ruleLogisticsDetails)) {
                for (CfgRuleLogisticsDetailEntity detail : ruleLogisticsDetails) {
                    dtos.add(CfgRuleLogisticsDTO.LogisticsResultDTO.buildLogisticsResult(detail, logistic));
                }
            }
        }
        return dtos;
    }

    private CfgRuleLogisticsDTO.LogisticsResultDTO getMinLogistics(CfgRuleExpireTimeDTO.StrategyDTO dto, String expireTimeId) {
        List<CfgRuleLogisticsDTO.LogisticsResultDTO> allLogistics = getAllLogistics(dto, expireTimeId);
        return allLogistics.stream()
                .min(Comparator.comparing(v -> v.getLogisticsCycleDays() + v.getLogisticsDays()))
                .orElseThrow(() -> new ServiceException(ApiError.LOGISTICS_CONFIG_NOT_FOUND));
    }

    private CfgRuleLogisticsDTO.LogisticsResultDTO getLogisticsMaxPriority(CfgRuleExpireTimeDTO.StrategyDTO dto, String expireTimeId) {
        // 获取外层最高优先级数据
        CfgRuleLogisticsEntity entity = dto.getCfgRuleLogisticsList().stream()
                .filter(v -> v.getExpireTimeId().equals(expireTimeId))
                .filter(v -> v.getPlatformType().equals(dto.getPlatformType()))
                .min(Comparator.comparing(CfgRuleLogisticsEntity::getIndex))
                .orElseThrow(() -> new ServiceException(ApiError.REPLENISHMENT_STOCK_UP_RULE_CONFIG_NOT_EXIST));
        //获取对应明细数据
        List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetails = dto.getCfgRuleLogisticsDetailList().stream().filter(v -> v.getMainId().equals(entity.getId())).collect(Collectors.toList());
        CfgRuleLogisticsDetailEntity detail = null;
        //amazon 取值店铺
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(dto.getPlatformType())) {
            //先获取区域加店铺 获取不到则获取区域加全部店铺 再获取不到则取外层数据
            detail = cfgRuleLogisticsDetails.stream()
                    .filter(v -> v.getArea().equals(dto.getArea()))
                    .filter(v -> v.getShopIdJson().contains(dto.getShopId()))
                    .findFirst().orElseGet(() -> cfgRuleLogisticsDetails.stream().filter(v -> v.getArea().equals(dto.getArea()))
                            .filter(v -> v.getType().equals(ShopAuthTypeEnum.ENUM_ALL.getCode()))
                            .findFirst().orElse(null)
                    );
        } else if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(dto.getPlatformType())) {
            //先获取海外仓 再获取不到则取外层数据
            detail = cfgRuleLogisticsDetails.stream()
                    .filter(v -> dto.getWarehouseId().contains(v.getWarehouseId()))
                    .max(Comparator.comparing(CfgRuleLogisticsDetailEntity::getLogisticsDays))
                    .orElse(null);
        }
        if (ObjectUtils.isEmpty(detail)) {
            return CfgRuleLogisticsDTO.LogisticsResultDTO.buildLogisticsResult(entity);
        } else {
            return CfgRuleLogisticsDTO.LogisticsResultDTO.buildLogisticsResult(detail, entity);
        }
    }

    /**
     * 获取明细数据
     *
     * @param dto                     参数
     * @param logisticId              物流id
     * @param cfgRuleLogisticsDetails 配置
     */
    private static List<CfgRuleLogisticsDetailEntity> getCfgRuleLogisticsDetails(CfgRuleExpireTimeDTO.StrategyDTO dto, String logisticId, List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetails) {
        List<CfgRuleLogisticsDetailEntity> ruleLogisticsDetails = new ArrayList<>();
        //amazon 取值店铺
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(dto.getPlatformType())) {
            //先获取区域加店铺 和 区域加全部店铺
            ruleLogisticsDetails = cfgRuleLogisticsDetails.stream()
                    .filter(v -> v.getMainId().equals(logisticId))
                    .filter(v -> v.getArea().equals(dto.getArea()))
                    .filter(v -> v.getShopIdJson().contains(dto.getShopId()) || v.getType().equals(ShopAuthTypeEnum.ENUM_ALL.getCode()))
                    .collect(Collectors.toList());

        } else if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(dto.getPlatformType())) {
            //获取海外仓
            ruleLogisticsDetails = cfgRuleLogisticsDetails.stream()
                    .filter(v -> v.getMainId().equals(logisticId))
                    .filter(v -> dto.getWarehouseId().contains(v.getWarehouseId()))
                    .collect(Collectors.toList());
        }
        return ruleLogisticsDetails;
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_EXPIRE_TIME;
    }
}
