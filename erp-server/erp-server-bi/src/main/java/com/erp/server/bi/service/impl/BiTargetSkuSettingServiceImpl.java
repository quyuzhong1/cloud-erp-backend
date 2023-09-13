package com.erp.server.bi.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.entity.BiTargetSkuSettingEntity;
import com.common.core.exception.ServiceException;
import com.erp.server.bi.mapper.BiTargetSkuSettingMapper;
import com.erp.server.bi.service.BiTargetSkuSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetSkuSettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * sku 目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetSkuSettingServiceImpl extends SuperServiceImpl<BiTargetSkuSettingMapper, BiTargetSkuSettingEntity> implements BiTargetSkuSettingService {


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetSkuSettingDTO.AddDTO addDTO) {
        BiTargetSkuSettingEntity biTargetSkuSettingEntity = new BiTargetSkuSettingEntity();
        BeanMapperUtils.copy(addDTO, biTargetSkuSettingEntity);

        // 数据处理
        handleData(biTargetSkuSettingEntity);

        log.info("开始新增sku 目标设置单");
        boolean save = super.save(biTargetSkuSettingEntity);
        if(!save) {
            throw new ServiceException("sku 目标设置单保存失败");
        }

        return biTargetSkuSettingEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetSkuSettingDTO.UpdateDTO updateDTO) {
        BiTargetSkuSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "sku 目标设置单"));
        BiTargetSkuSettingEntity biTargetSkuSettingEntity =  BeanMapperUtils.map(BiTargetSkuSettingEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetSkuSettingEntity);
        log.info("编辑 开始修改sku 目标设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetSkuSettingEntity);
        if(!save) {
            throw new ServiceException("sku 目标设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(BiTargetSkuSettingEntity biTargetSkuSettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
