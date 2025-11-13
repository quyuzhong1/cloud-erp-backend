package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.DictKingdeeDTO;
import com.erp.model.sys.entity.DictKingdeeEntity;
import com.erp.server.sys.mapper.DictKingdeeMapper;
import com.erp.server.sys.service.DictKingdeeService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 金蝶字典表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-06-07
 */
@Slf4j
@Service
public class DictKingdeeServiceImpl extends SuperServiceImpl<DictKingdeeMapper, DictKingdeeEntity> implements DictKingdeeService {

    @Override
    public Boolean addOrUpdate(List<DictKingdeeDTO.CommonDTO> dto) {
        if(CollectionUtils.isEmpty(dto)){
            return true;
        }
        List<String> typeNameList = dto.stream().map(DictKingdeeDTO.CommonDTO::getTypeName).collect(Collectors.toList());
        List<DictKingdeeEntity> existDbList = this.listByTypeName(typeNameList);
        List<DictKingdeeEntity> hangdleList = BeanMapperUtils.copyList(DictKingdeeEntity.class, dto);
        List<DictKingdeeEntity> addList = new ArrayList<>();
        List<DictKingdeeEntity> updateList = new ArrayList<>();
        for (DictKingdeeEntity handleEntity : hangdleList) {
            DictKingdeeEntity existDb = existDbList.stream().filter(e -> e.getKingdeeId().equals(handleEntity.getKingdeeId())).findFirst().orElse(null);
            if(existDb == null){
                addList.add(handleEntity);
            }else{
                BeanUtil.copyProperties(handleEntity, existDb, CopyOptions.create().setIgnoreNullValue(true));
                updateList.add(existDb);
            }
        }
        List<String> nowExistIds = hangdleList.stream().map(DictKingdeeEntity::getKingdeeId).collect(Collectors.toList());
        List<String> disabledIdList = existDbList.stream().filter(v->!nowExistIds.contains(v.getKingdeeId())).map(BaseEntity::getId).collect(Collectors.toList());
        this.handleDbData(addList,updateList,disabledIdList);
        return true;
    }


    private void handleDbData(List<DictKingdeeEntity> addList,List<DictKingdeeEntity> updateList,List<String> disabledIdList ) {
        if(CollectionUtils.isNotEmpty(addList)){
            this.saveBatch(addList);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(addList);
        }
        if(CollectionUtils.isNotEmpty(disabledIdList)){
            this.lambdaUpdate().eq(DictKingdeeEntity::getId, disabledIdList).set(DictKingdeeEntity::getDisabled,true);
        }
    }

    private List<DictKingdeeEntity> listByTypeName(List<String> typeNameList){
        if(CollectionUtils.isEmpty(typeNameList)){
            return new ArrayList<>();
        }
        return this.lambdaQuery().in(DictKingdeeEntity::getTypeName,typeNameList).list();
    }
    /**
    * 修改
    */
    @Override
    public Boolean update(DictKingdeeDTO.UpdateDTO updateDTO) {
        DictKingdeeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶字典单"));
        DictKingdeeEntity dictKingdeeEntity =  BeanMapperUtils.map(DictKingdeeEntity.class, updateDTO);

        // 数据处理
        handleData(dictKingdeeEntity);
        log.info("编辑 开始修改金蝶字典单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dictKingdeeEntity);
        if(!save) {
            throw new ServiceException("金蝶字典单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<DictKingdeeDTO.ListDTO> listByParam(DictKingdeeDTO.ParamDTO param) {
        return baseMapper.listByParam(param);
    }

    @Override
    public List<DictKingdeeDTO.ListDTO> dropDown(String typeName) {
        List<DictKingdeeEntity> dictKingdeeEntityList = this.lambdaQuery().eq(StringUtils.isNotBlank(typeName),DictKingdeeEntity::getTypeName,typeName).orderByAsc(DictKingdeeEntity::getDisabled).list();
        return dictKingdeeEntityList.stream().map(v->new DictKingdeeDTO.ListDTO(v.getTypeName(),v.getValue(),v.getName(),v.getDisabled())).collect(Collectors.toList());
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DictKingdeeEntity dictKingdeeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
