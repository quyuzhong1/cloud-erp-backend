package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.CfgRuleOutEntity;
import com.erp.model.wms.enums.CfgRuleOutEnum;
import com.erp.server.wms.mapper.CfgRuleOutMapper;
import com.erp.server.wms.service.CfgRuleOutService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.CfgRuleOutDTO;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

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
        List<CfgRuleOutDTO.B2cAllowableDeviationsCondition> conditionDTOS = b2cAllowableDeviations.getConditionDTO();
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
        Set<String> values = new HashSet<>();
        List<String> duplicates = conditionDTOS.stream()
                .map(CfgRuleOutDTO.EquipmentSortingPortConditionDTO::getValue)
                .filter(value -> !values.add(value))
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
}
