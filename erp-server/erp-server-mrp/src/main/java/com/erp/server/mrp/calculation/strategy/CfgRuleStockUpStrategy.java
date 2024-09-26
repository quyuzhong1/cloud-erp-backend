package com.erp.server.mrp.calculation.strategy;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleLogisticsDetailEntity;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSettingEnum;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import com.erp.server.mrp.service.CfgRuleStockingRatioService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> cfgRuleStockingRatios = new ArrayList<>();
        List<CfgRuleLogisticsEntity> ruleLogisticsList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(cfgRuleStockUp)) {
            cfgRuleStockingRatios = cfgRuleStockingRatioService.listByStockUpIdAndType(cfgRuleStockUp.getId(), dto.getSkuType());
            ruleLogisticsList = cfgRuleLogisticsService.listByStockUpIdList(Collections.singletonList(cfgRuleStockUp.getId()));
        }
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsResult = ruleLogisticsList.stream()
                .min(Comparator.comparing(CfgRuleLogisticsEntity::getIndex))
                .map(CfgRuleLogisticsDTO.LogisticsResultDTO::buildLogisticsResult).orElse(getLogisticsMaxPriority(dto));
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMinResult = ruleLogisticsList.stream()
                .min(Comparator.comparing(v -> v.getLogisticsDays() + v.getLogisticsCycleDays()))
                .map(CfgRuleLogisticsDTO.LogisticsResultDTO::buildLogisticsResult).orElse(getMinLogistics(dto));
        CfgRuleLogisticsDTO.LogisticsResultDTO logisticsMaxResult = ruleLogisticsList.stream()
                .max(Comparator.comparing(v -> v.getLogisticsDays() + v.getLogisticsCycleDays()))
                .map(CfgRuleLogisticsDTO.LogisticsResultDTO::buildLogisticsResult).orElse(getMaxLogistics(dto));
        CfgRuleStockUpDTO.StrategyResultDTO result = new CfgRuleStockUpDTO.StrategyResultDTO();
        List<CfgRuleStockingRatioDTO.StockingRatioResultDTO> defaultRatio = dto.getDefaultStockingRatio().stream()
                .map(CfgRuleStockingRatioDTO.StockingRatioResultDTO::buildStockingRatioResult)
                .collect(Collectors.toList());
        result.buildStrategyResultDTO(cfgRuleStockUp, dto.getDefaultStockUp(), cfgRuleStockingRatios, logisticsResult, logisticsMaxResult, logisticsMinResult, defaultRatio);
        return result;
    }

    private CfgRuleLogisticsDTO.LogisticsResultDTO getMaxLogistics(CfgRuleStockUpDTO.StrategyDTO dto) {
        List<CfgRuleLogisticsDTO.LogisticsResultDTO> allLogistics = getAllLogistics(dto);
        return allLogistics.stream()
                .max(Comparator.comparing(v -> v.getLogisticsCycleDays() + v.getLogisticsDays()))
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_LOGISTICS_NOT_EXIST));
    }

    private static List<CfgRuleLogisticsDTO.LogisticsResultDTO> getAllLogistics(CfgRuleStockUpDTO.StrategyDTO dto) {
        List<CfgRuleLogisticsDTO.LogisticsResultDTO> dtos = new ArrayList<>();
        for (CfgRuleLogisticsEntity logistic : dto.getDefaultLogistics()) {
            dtos.add(CfgRuleLogisticsDTO.LogisticsResultDTO.buildLogisticsResult(logistic));
            //获取明细数据
            List<CfgRuleLogisticsDetailEntity> ruleLogisticsDetails = getCfgRuleLogisticsDetails(dto, logistic.getId(), dto.getLogisticsDetails());
            if (!CollectionUtils.isEmpty(ruleLogisticsDetails)) {
                for (CfgRuleLogisticsDetailEntity detail : ruleLogisticsDetails) {
                    dtos.add(CfgRuleLogisticsDTO.LogisticsResultDTO.buildLogisticsResult(detail, logistic));
                }
            }
        }
        return dtos;
    }

    private CfgRuleLogisticsDTO.LogisticsResultDTO getMinLogistics(CfgRuleStockUpDTO.StrategyDTO dto) {
        List<CfgRuleLogisticsDTO.LogisticsResultDTO> allLogistics = getAllLogistics(dto);
        return allLogistics.stream()
                .min(Comparator.comparing(v -> v.getLogisticsCycleDays() + v.getLogisticsDays()))
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_LOGISTICS_NOT_EXIST));
    }

    private CfgRuleLogisticsDTO.LogisticsResultDTO getLogisticsMaxPriority(CfgRuleStockUpDTO.StrategyDTO dto) {
        // 获取外层最高优先级数据
        CfgRuleLogisticsEntity entity = dto.getDefaultLogistics().stream()
                .min(Comparator.comparing(CfgRuleLogisticsEntity::getIndex))
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_CFG_RULE_STOCK_UP_NOT_EXIST, CfgRulePlatformTypeEnum.getName(dto.getPlatformType())));
        //获取对应明细数据
        List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetails = dto.getLogisticsDetails().stream().filter(v -> v.getMainId().equals(entity.getId())).collect(Collectors.toList());
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
                    .filter(v -> v.getWarehouseId().equals(dto.getWarehouseId()))
                    .findFirst().orElse(null);
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
    private static List<CfgRuleLogisticsDetailEntity> getCfgRuleLogisticsDetails(CfgRuleStockUpDTO.StrategyDTO dto, String logisticId, List<CfgRuleLogisticsDetailEntity> cfgRuleLogisticsDetails) {
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
                    .filter(v -> v.getWarehouseId().equals(dto.getWarehouseId()))
                    .collect(Collectors.toList());
        }
        return ruleLogisticsDetails;
    }

    @Override
    public CfgRuleSettingEnum getCfgRuleSetting() {
        return GET_STOCK_UP;
    }
}
