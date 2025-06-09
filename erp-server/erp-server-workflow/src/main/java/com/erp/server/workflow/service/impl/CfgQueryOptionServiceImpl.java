package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
import com.erp.server.workflow.mapper.CfgQueryOptionMapper;
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
 */
@Slf4j
@Service
public class CfgQueryOptionServiceImpl extends SuperServiceImpl<CfgQueryOptionMapper, CfgQueryOptionEntity> implements CfgQueryOptionService {


    @Override
    public List<CfgQueryOptionDTO.ListDTO> proDropDown(String bussinessKey) {
        return baseMapper.proDropDown(bussinessKey);
    }


    @Override
    public List<CfgQueryOptionDTO.cfgApproveSyncDropDownDTO> cfgApproveSyncDropDown(String bussinessKey,String fieldBelongsType) {
        LambdaQueryWrapper<CfgQueryOptionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgQueryOptionEntity::getFieldBelongsType, CfgQueryOptionFieldBelongsTypeEnum.COMMON.getCode());
        List<CfgQueryOptionEntity> common = baseMapper.selectList(queryWrapper);
        common.stream().forEach(item -> {
            item.setConditionFieldName(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getName()+"-"+item.getConditionFieldName());
        });

        queryWrapper.clear();
        if (StringUtils.isNotBlank(fieldBelongsType)) {
            queryWrapper.eq(CfgQueryOptionEntity::getFieldBelongsType, fieldBelongsType);
        }
        if (StringUtils.isNotBlank(bussinessKey)) {
            queryWrapper.eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey);
        }
        queryWrapper.eq(CfgQueryOptionEntity::getIsDeleted, false);
        queryWrapper.orderByDesc(CfgQueryOptionEntity::getFieldBelongsType);
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = baseMapper.selectList(queryWrapper);
        cfgQueryOptionEntities.stream().forEach(item -> {
            item.setConditionFieldName(CfgQueryOptionFieldBelongsTypeEnum.getName(item.getFieldBelongsType())+"-"+item.getConditionFieldName());
        });

        cfgQueryOptionEntities.addAll(common);
        cfgQueryOptionEntities = cfgQueryOptionEntities.stream()
                .filter(e -> !e.getConditionField().contains("id") && !e.getConditionField().contains("Id"))
                .collect(Collectors.toList());
        return BeanUtil.copyToList(cfgQueryOptionEntities, CfgQueryOptionDTO.cfgApproveSyncDropDownDTO.class);
    }

    @Override
    public List<CfgQueryOptionDTO.TreeDTO> tree(String bussinessKey) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = this.list(new LambdaQueryWrapper<CfgQueryOptionEntity>().eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey).eq(CfgQueryOptionEntity::getIsDeleted, false));
        List<CfgQueryOptionDTO.TreeDTO> resultList = new ArrayList<>(cfgQueryOptionEntities.size());
        Map<String, String> map = new HashMap<>();
        for (RuleCompareEnum item : RuleCompareEnum.values()) {
            map.put(item.getCode(), item.getName());
        }
        for (CfgQueryOptionEntity item : cfgQueryOptionEntities) {
            String conditionField = item.getConditionField();
            CfgQueryOptionDTO.TreeDTO tree = new CfgQueryOptionDTO.TreeDTO();
            tree.setConditionField(conditionField);
            String logicStr = item.getLogic();
            List<String> logicList = Arrays.asList(logicStr.split(","));
            List<CfgQueryOptionDTO.TreeDTO> childrenList = new ArrayList<>(logicList.size());
            for (String logic : logicList) {
                CfgQueryOptionDTO.TreeDTO children = new CfgQueryOptionDTO.TreeDTO();
                children.setConditionField(conditionField);
                children.setLogic(logic);
                children.setLogicName(map.getOrDefault(logic, ""));
                childrenList.add(children);
            }
            tree.setChildren(childrenList);
            resultList.add(tree);
        }
        return resultList;
    }

    @Override
    public List<CfgQueryOptionDTO.ViewDTO> getSystemfield(String bussinessKey) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = baseMapper.getSystemfield(bussinessKey);
        List<CfgQueryOptionDTO.ViewDTO> viewDTOS = BeanUtil.copyToList(cfgQueryOptionEntities, CfgQueryOptionDTO.ViewDTO.class);
        viewDTOS.forEach(item -> {
            if (StrUtil.isNotBlank(item.getFieldType())) {
                item.setFieldTypeName(CfgQueryOptionFieldTypeEnum.valueOf(item.getFieldType().toUpperCase()).getName());
            }
        });
        return viewDTOS;
    }

    @Override
    public List<CfgQueryOptionEntity> listBySysFieldList(String bussinessKey, List<String> sysFieldList) {
        return lambdaQuery().eq(CfgQueryOptionEntity::getBussinessKey,bussinessKey)
                .in(CfgQueryOptionEntity::getConditionField,sysFieldList)
                .list();
    }

    @Override
    public List<CfgQueryOptionEntity> listByMqParams(CfgQueryOptionDTO.MqParamsDTO mqParamsDTO) {
        return baseMapper.listByMqParams(mqParamsDTO);
    }

    @Override
    public Map<String, Object> getVariablesMapByBusinessKey(CfgQueryOptionDTO.VariablesParamsDTO dto) {
        if(Objects.isNull(dto) || StringUtils.isBlank(dto.getBusinessKey()) || CollUtil.isEmpty(dto.getVariablesMap())){
            return Collections.emptyMap();
        }
        //主表map
        Map<String, Object> variablesMap = dto.getVariablesMap();
        //获取配置明细
        List<CfgQueryOptionEntity> cfgQueryOptionList = lambdaQuery()
                .in(CfgQueryOptionEntity::getBussinessKey, dto.getBusinessKey())
                .list();
        if(CollUtil.isNotEmpty(cfgQueryOptionList)){
            //根据fieldBelongsType 进行分组
            Map<String, List<CfgQueryOptionEntity>> fieldBelongsTypeByMap = cfgQueryOptionList.stream().collect(Collectors.groupingBy(CfgQueryOptionEntity::getFieldBelongsType));
            //获取主表字段配置
            List<CfgQueryOptionEntity> mainCfgQueryOptionList = fieldBelongsTypeByMap.get(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode());

            //遍历
            for (Map.Entry<String, List<CfgQueryOptionEntity>> entry : fieldBelongsTypeByMap.entrySet()) {
                //common和主表不需要再查询
                if(entry.getKey().equals(CfgQueryOptionFieldBelongsTypeEnum.COMMON.getCode()) || entry.getKey().equals(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode())){
                    continue;
                }
                List<CfgQueryOptionEntity> value = entry.getValue();
                //如果存在，则需要找对明细表里的关联字段，并根据该字段来进行FeignQuery查询出对应的明细列表
                CfgQueryOptionEntity detailEntity = value.stream().filter(e -> StringUtils.isNotBlank(e.getParentId())).findFirst().orElse(null);
                if (Objects.isNull(detailEntity)) {
                    continue;
                }
                //获取关联记录
                String parentId = detailEntity.getParentId();
                CfgQueryOptionEntity refEntity = mainCfgQueryOptionList.stream().filter(e -> e.getId().equals(parentId)).findFirst().orElse(null);
                String refField = refEntity.getConditionField();
                String refValue = String.valueOf(variablesMap.get(refField));

                String classpath = detailEntity.getClasspath();
                classpath = classpath.replace("class ", "");
                Class<BaseEntity> clazz = null;
                try {
                    clazz = (Class<BaseEntity>) Class.forName(classpath);
                } catch (ClassNotFoundException e) {
                    throw new ServiceException(classpath + "实体不存在");
                }
                List<BaseEntity> detailList = FeignQuery.create(clazz)
                        .eq(detailEntity.getConditionField(), refValue)
                        .list();

                if (CollUtil.isEmpty(detailList)) {
                    throw new ServiceException(entry.getKey() + "明细列表数据不存在");
                }
                //明细数据
                variablesMap.put(entry.getKey() , BeanUtil.copyToList(detailList,Map.class));
            }
        }
        return variablesMap;
    }
}
