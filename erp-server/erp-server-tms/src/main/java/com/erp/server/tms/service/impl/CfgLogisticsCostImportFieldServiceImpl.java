package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.model.sys.entity.CfgQueryOptionEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.erp.model.tms.enums.CfgLogisticsCostImportFieldFieldTypeEnum;
import com.erp.server.tms.mapper.CfgLogisticsCostImportFieldMapper;
import com.erp.server.tms.service.CfgLogisticsCostImportFieldService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.CfgLogisticsCostImportFieldDTO;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;

import static jdk.nashorn.internal.objects.NativeArray.forEach;

/**
 * <p>
 * 费用项配置字段基础表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@Service
public class CfgLogisticsCostImportFieldServiceImpl extends SuperServiceImpl<CfgLogisticsCostImportFieldMapper, CfgLogisticsCostImportFieldEntity> implements CfgLogisticsCostImportFieldService {


    @Override
    public List<CfgLogisticsCostImportFieldDTO.ListDTO> listByBusinessType(String businessType) {
        List<CfgLogisticsCostImportFieldDTO.ListDTO> list = this.baseMapper.listByBusinessType(businessType);

        List<String> queryOptionIds = list.stream().map(CfgLogisticsCostImportFieldDTO.ListDTO::getQueryOptionId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(queryOptionIds)){
            List<CfgQueryOptionEntity> cfgQueryOptionEntities = FeignQuery.create(CfgQueryOptionEntity.class).in(CfgQueryOptionEntity::getId, queryOptionIds).list();
            if(CollUtil.isNotEmpty(cfgQueryOptionEntities)){
                for (CfgLogisticsCostImportFieldDTO.ListDTO listDTO : list) {
                    if(StringUtils.isNotBlank(listDTO.getQueryOptionId())){
                        listDTO.setQueryOptionItem(cfgQueryOptionEntities.stream().filter(item -> item.getId().equals(listDTO.getQueryOptionId())).findFirst().orElse(null));
                    }
                }
            }
        }
        list.forEach(e -> e.setFieldTypeName(CfgLogisticsCostImportFieldFieldTypeEnum.getName(e.getFieldType())));
        return list;
    }

    @Override
    public List<CfgLogisticsCostImportFieldDTO.TreeDTO> tree(String businessType) {
        List<CfgLogisticsCostImportFieldDTO.TreeDTO> flagList = this. baseMapper.findTree(businessType);

        List<CfgLogisticsCostImportFieldDTO.TreeDTO> feildList = BeanMapperUtils.copyList(CfgLogisticsCostImportFieldDTO.TreeDTO.class, this.baseMapper.listByBusinessType(businessType));
        feildList.forEach(e -> e.setFieldTypeName(CfgLogisticsCostImportFieldFieldTypeEnum.getName(e.getFieldType())));

        List<CfgLogisticsCostImportFieldDTO.TreeDTO> treeList = feildList.stream().
                filter(item -> "0".equals(item.getParentId()))
                .map(item -> {
                    item.setChildrenList(getChildren(item, feildList, flagList));
                    return item;
                }).collect(Collectors.toList());
        return treeList;
    }

    private List<CfgLogisticsCostImportFieldDTO.TreeDTO> getChildren(CfgLogisticsCostImportFieldDTO.TreeDTO item, List<CfgLogisticsCostImportFieldDTO.TreeDTO> fieldList, List<CfgLogisticsCostImportFieldDTO.TreeDTO> flagList) {
        List<CfgLogisticsCostImportFieldDTO.TreeDTO> collect = fieldList.stream().filter(dept -> item.getId().equals(dept.getParentId()))
                .map(d -> {
                    d.setChildrenList(getChildren(d, fieldList, flagList));
                    return d;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect) ? null : collect;
    }
}
