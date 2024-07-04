package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.dto.CfgRuleOutDTO;
import com.erp.model.wms.entity.CfgRuleOutEntity;
import com.erp.model.wms.enums.AbnormalCauseEnum;
import com.erp.model.wms.enums.CfgRuleOutEnum;
import com.erp.server.wms.mapper.CfgRuleOutMapper;
import com.erp.server.wms.service.CfgRuleOutService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.erp.model.wms.enums.CfgRuleOutEnum.AllowableDeviationsConditionEnum.*;

/**
 * <p>
 * 出库配置规则 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-06-28
 */
@Slf4j
@Service
public class CfgRuleOutServiceImpl extends SuperServiceImpl<CfgRuleOutMapper, CfgRuleOutEntity> implements CfgRuleOutService {

    @Resource
    private CfgRuleOutService service;

    @Resource
    private SpElServer spElServer;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(CfgRuleOutDTO.CommonDTO commonDTO) {
        //处理规则详情
        CfgRuleOutEntity equipmentSortingPortEntity = new CfgRuleOutEntity();
        equipmentSortingPortEntity.setType(CfgRuleOutEnum.CfgRuleOutTypeEnum.EQUIPMENT_SORTING_PORT.getCode());
        Map<String, Object> equipmentSortingPortMap = BeanUtil.beanToMap(commonDTO.getEquipmentSortingPortDTO());
        this.checkEquipmentSortingPort(commonDTO.getEquipmentSortingPortDTO());
        equipmentSortingPortEntity.setRuleContent(equipmentSortingPortMap);
        CfgRuleOutEntity b2cAllowableDeviationsEntity = new CfgRuleOutEntity();
        b2cAllowableDeviationsEntity.setType(CfgRuleOutEnum.CfgRuleOutTypeEnum.B2C_ALLOWABLE_DEVIATIONS.getCode());
        Map<String, Object> b2cAllowableDeviationsMap = BeanUtil.beanToMap(commonDTO.getB2cAllowableDeviations());
        this.checkB2cAllowableDeviations(commonDTO.getB2cAllowableDeviations());
        b2cAllowableDeviationsEntity.setRuleContent(b2cAllowableDeviationsMap);
        //删除数据后再保存
        service.remove(new QueryWrapper<>());
        service.saveBatch(Arrays.asList(equipmentSortingPortEntity, b2cAllowableDeviationsEntity));
        return new BaseResultDTO.AddDTO();
    }

    private void checkB2cAllowableDeviations(CfgRuleOutDTO.B2cAllowableDeviations b2cAllowableDeviations) {
        if(Objects.isNull(b2cAllowableDeviations)){
            return;
        }
        List<CfgRuleOutDTO.B2cAllowableDeviationsCondition> conditionDTOS = b2cAllowableDeviations.getConditionDTOList();
        if(CollectionUtil.isEmpty(conditionDTOS)){
            return;
        }
        List<String> valueList = conditionDTOS.stream().flatMap(v->v.getValList().stream()).collect(Collectors.toList());
        Set<String> values = new HashSet<>();
        List<String> duplicates = valueList.stream()
                .filter(v -> !values.add(v))
                .collect(Collectors.toList());

        if (!duplicates.isEmpty()) {
            throw new ServiceException("B2C称重量方允许偏差条件存在物流商获渠道");
        }

        for (CfgRuleOutDTO.B2cAllowableDeviationsCondition b2cAllowableDeviationsConditionDetail : conditionDTOS) {
            if(CollectionUtil.isEmpty(b2cAllowableDeviationsConditionDetail.getConditionDetailList())){
                continue;
            }
            Set<String> detailSet = new HashSet<>();
            List<String> detailDuplicates = b2cAllowableDeviationsConditionDetail.getConditionDetailList().stream()
                    .map(CfgRuleOutDTO.B2cAllowableDeviationsConditionDetail::getField)
                    .filter(v -> !detailSet.add(v))
                    .collect(Collectors.toList());

            if (!detailDuplicates.isEmpty()) {
                throw new ServiceException("B2C称重量方允许偏差条件存在相同配置");
            }
            List<ConditionElement> conditionElementList = BeanUtil.copyToList(b2cAllowableDeviationsConditionDetail.getConditionDetailList(),ConditionElement.class);
            SpElExpressionDTO splElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
            String expression = splElDTO.getExpression();
            Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
            if (!checkResult) {
                throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
            }
        }

    }

    private void checkEquipmentSortingPort(CfgRuleOutDTO.EquipmentSortingPortDTO equipmentSortingPortDTO) {
        if(Objects.isNull(equipmentSortingPortDTO)){
            return;
        }
        List<CfgRuleOutDTO.EquipmentSortingPortConditionDTO> conditionDTOS = equipmentSortingPortDTO.getSortingConditionDTOList();
        if(CollectionUtil.isEmpty(conditionDTOS)){
            return;
        }
        List<String> valueList = conditionDTOS.stream().flatMap(v->v.getValueList().stream()).collect(Collectors.toList());
        Set<String> values = new HashSet<>();
        List<String> duplicates = valueList.stream()
                .filter(v -> !values.add(v))
                .collect(Collectors.toList());

        if (!duplicates.isEmpty()) {
            throw new ServiceException("设备分拣口存在相同的物流商或渠道配置");
        }
    }

    @Override
    public CfgRuleOutDTO.CommonDTO view() {
        List<CfgRuleOutEntity> cfgRuleOutEntities = this.list();
        CfgRuleOutEntity equipmentSortingPortEntity = cfgRuleOutEntities.stream().filter(entity -> entity.getType().equals(CfgRuleOutEnum.CfgRuleOutTypeEnum.EQUIPMENT_SORTING_PORT.getCode())).findFirst().orElse(new CfgRuleOutEntity());
        CfgRuleOutEntity b2cAllowableDeviationsEntity = cfgRuleOutEntities.stream().filter(entity -> entity.getType().equals(CfgRuleOutEnum.CfgRuleOutTypeEnum.B2C_ALLOWABLE_DEVIATIONS.getCode())).findFirst().orElse(new CfgRuleOutEntity()  );
        CfgRuleOutDTO.CommonDTO commonDTO = new CfgRuleOutDTO.CommonDTO();
        commonDTO.setEquipmentSortingPortDTO(BeanUtil.mapToBean(equipmentSortingPortEntity.getRuleContent(), CfgRuleOutDTO.EquipmentSortingPortDTO.class,true));;
        commonDTO.setB2cAllowableDeviations(BeanUtil.mapToBean(b2cAllowableDeviationsEntity.getRuleContent(), CfgRuleOutDTO.B2cAllowableDeviations.class,true));
        return commonDTO;
    }

    @Override
    public String getSortingPort(CfgRuleOutDTO.SortingPortRuleDTO dto) {
        ValidatorUtil.validateEntity(dto);
        CfgRuleOutDTO.CommonDTO commonDTO = this.view();
        String sortingPort = this.getSortingPort(commonDTO,dto);
        if(sortingPort.equals(CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode())){
            //更新异常
            soB2cDeliveryService.updateAbnormal(Arrays.asList(dto.getDeliveryOrderId()), AbnormalCauseEnum.EQUIPMENT_SORTING);
        }
        return sortingPort;
    }

    public String getSortingPort(CfgRuleOutDTO.CommonDTO commonDTO,CfgRuleOutDTO.SortingPortRuleDTO dto) {
        //校验称重量方规则，通过走设备分拣口规则
        CfgRuleOutDTO.B2cAllowableDeviations b2cAllowableDeviations = commonDTO.getB2cAllowableDeviations();
        Boolean b2cAllowableDeviationsResult = this.handleB2cAllowableDeviations(b2cAllowableDeviations,dto);
        //不通过返回异常口
        if(!b2cAllowableDeviationsResult){
            return CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode();
        }
        List<CfgRuleOutDTO.EquipmentSortingPortConditionDTO> equipmentSortingPortDTO = commonDTO.getEquipmentSortingPortDTO().getSortingConditionDTOList();
        for (CfgRuleOutDTO.EquipmentSortingPortConditionDTO equipmentSortingPortConditionDTO : equipmentSortingPortDTO) {
            if(equipmentSortingPortConditionDTO.getCompare().equals(CfgRuleOutEnum.EquipmentSortingPortCompareEnum.IN_LIST.getCode()) || equipmentSortingPortConditionDTO.getCompare().equals(CfgRuleOutEnum.EquipmentSortingPortCompareEnum.EQ.getCode())){
                if(equipmentSortingPortConditionDTO.getValueList().contains(dto.getLogisticsSupplierId()) || equipmentSortingPortConditionDTO.getValueList().contains(dto.getChannelId())){
                    return equipmentSortingPortConditionDTO.getPort();
                }
            }
            if(equipmentSortingPortConditionDTO.getCompare().equals(CfgRuleOutEnum.EquipmentSortingPortCompareEnum.NOT_IN_LIST.getCode()) || equipmentSortingPortConditionDTO.getCompare().equals(CfgRuleOutEnum.EquipmentSortingPortCompareEnum.NQ.getCode())){
                if(!equipmentSortingPortConditionDTO.getValueList().contains(dto.getLogisticsSupplierId()) && !equipmentSortingPortConditionDTO.getValueList().contains(dto.getChannelId())){
                    return equipmentSortingPortConditionDTO.getPort();
                }
            }
        }
        return CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode();
    }

    @Override
    public Boolean handleB2cAllowableDeviations(CfgRuleOutDTO.B2cAllowableDeviations b2cAllowableDeviations, CfgRuleOutDTO.SortingPortRuleDTO dto) {
        List<CfgRuleOutDTO.B2cAllowableDeviationsCondition> conditionList = b2cAllowableDeviations.getConditionDTOList();
        dto.handleNullToZero();
        for (CfgRuleOutDTO.B2cAllowableDeviationsCondition condition : conditionList) {
            if(!condition.getValList().contains(dto.getChannelId()) && !condition.getValList().contains(dto.getLogisticsSupplierId())){
                continue;
            }
            List<ConditionElement> conditionElementList = BeanMapper.copyList(condition.getConditionDetailList(), ConditionElement.class);
            Map<String,Object> map = this.getConditionMap(dto,b2cAllowableDeviations.isWhenZeroNormalOutSwitch());
            Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map);
            return matchResult;
        }
        return true;
    }

    private Map<String, Object> getConditionMap(CfgRuleOutDTO.SortingPortRuleDTO dto,boolean whenZeroNormalOutSwitch) {
        Map<String,Object> map = new HashMap<>();
        if(!whenZeroNormalOutSwitch || dto.getOrderWeight().compareTo(BigDecimal.ZERO) != 0){
            map.put(WEIGHING_VARIANCE_RATE.getCode(),dto.getOrderWeight().compareTo(BigDecimal.ZERO) == 0?100:dto.getOrderWeight().subtract(dto.getScanWeight()).abs().divide(dto.getOrderWeight(),4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            map.put(WEIGHING_VARIANCE_VALUE.getCode(),dto.getOrderWeight().subtract(dto.getScanWeight()).abs());
        }
        if(!whenZeroNormalOutSwitch || dto.getOrderLength().compareTo(BigDecimal.ZERO) != 0){
            map.put(VOLUME_DIFFERENCE_RATE_LONG.getCode(),dto.getOrderLength().compareTo(BigDecimal.ZERO) == 0?100:dto.getOrderLength().subtract(dto.getScanLength()).abs().divide(dto.getOrderLength(),4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            map.put(VOLUME_DIFFERENCE_VALUE_LONG.getCode(),dto.getOrderLength().subtract(dto.getScanLength()).abs());
        }
        if(!whenZeroNormalOutSwitch || dto.getOrderWidth().compareTo(BigDecimal.ZERO) != 0){
            map.put(VOLUME_DIFFERENCE_RATE_WIDTH.getCode(),dto.getOrderWidth().compareTo(BigDecimal.ZERO) == 0?100:dto.getOrderWidth().subtract(dto.getScanWidth()).abs().divide(dto.getOrderWidth(),4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            map.put(VOLUME_DIFFERENCE_VALUE_WIDTH.getCode(),dto.getOrderWidth().subtract(dto.getScanWidth()).abs());
        }
        if(!whenZeroNormalOutSwitch || dto.getOrderHeight().compareTo(BigDecimal.ZERO) != 0){
            map.put(VOLUME_DIFFERENCE_RATE_HEIGHT.getCode(),dto.getOrderHeight().compareTo(BigDecimal.ZERO) == 0?100:dto.getOrderHeight().subtract(dto.getScanHeight()).abs().divide(dto.getOrderHeight(),4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            map.put(VOLUME_DIFFERENCE_VALUE_HEIGHT.getCode(),dto.getOrderHeight().subtract(dto.getScanHeight()).abs());
        }
        return map;
    }
}
