package com.erp.server.bi.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;

import com.common.core.exception.ServiceException;
import com.erp.server.bi.mapper.BiTargetCategorySettingMapper;
import com.erp.server.bi.service.BiTargetCategorySettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 分类 目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetCategorySettingServiceImpl extends SuperServiceImpl<BiTargetCategorySettingMapper, BiTargetCategorySettingEntity> implements BiTargetCategorySettingService {


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetCategorySettingDTO.AddDTO addDTO) {
        BiTargetCategorySettingEntity biTargetCategorySettingEntity = new BiTargetCategorySettingEntity();
        BeanMapperUtils.copy(addDTO, biTargetCategorySettingEntity);

        // 数据处理
        handleData(biTargetCategorySettingEntity);

        log.info("开始新增分类 目标设置单");
        boolean save = super.save(biTargetCategorySettingEntity);
        if(!save) {
            throw new ServiceException("分类 目标设置单保存失败");
        }


        return biTargetCategorySettingEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetCategorySettingDTO.UpdateDTO updateDTO) {
        BiTargetCategorySettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "分类 目标设置单"));
        BiTargetCategorySettingEntity biTargetCategorySettingEntity =  BeanMapperUtils.map(BiTargetCategorySettingEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetCategorySettingEntity);
        log.info("编辑 开始修改分类 目标设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetCategorySettingEntity);
        if(!save) {
            throw new ServiceException("分类 目标设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(BiTargetCategorySettingEntity biTargetCategorySettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
