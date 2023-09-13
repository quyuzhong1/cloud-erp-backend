package com.erp.server.bi.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.entity.BiTargetNewProductSettingEntity;

import com.erp.server.bi.mapper.BiTargetNewProductSettingMapper;
import com.erp.server.bi.service.BiTargetNewProductSettingService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 新品目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetNewProductSettingServiceImpl extends SuperServiceImpl<BiTargetNewProductSettingMapper, BiTargetNewProductSettingEntity> implements BiTargetNewProductSettingService {


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetNewProductSettingDTO.AddDTO addDTO) {
        BiTargetNewProductSettingEntity biTargetNewProductSettingEntity = new BiTargetNewProductSettingEntity();
        BeanMapperUtils.copy(addDTO, biTargetNewProductSettingEntity);

        // 数据处理
        handleData(biTargetNewProductSettingEntity);

        log.info("开始新增新品目标设置单");
        boolean save = super.save(biTargetNewProductSettingEntity);
        if(!save) {
            throw new ServiceException("新品目标设置单保存失败");
        }

        return biTargetNewProductSettingEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetNewProductSettingDTO.UpdateDTO updateDTO) {
        BiTargetNewProductSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "新品目标设置单"));
        BiTargetNewProductSettingEntity biTargetNewProductSettingEntity =  BeanMapperUtils.map(BiTargetNewProductSettingEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetNewProductSettingEntity);
        log.info("编辑 开始修改新品目标设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetNewProductSettingEntity);
        if(!save) {
            throw new ServiceException("新品目标设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(BiTargetNewProductSettingEntity biTargetNewProductSettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
