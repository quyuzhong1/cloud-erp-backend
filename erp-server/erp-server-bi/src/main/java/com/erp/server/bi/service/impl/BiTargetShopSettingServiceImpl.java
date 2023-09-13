package com.erp.server.bi.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.entity.BiTargetShopSettingEntity;
import com.common.core.exception.ServiceException;
import com.erp.server.bi.mapper.BiTargetShopSettingMapper;
import com.erp.server.bi.service.BiTargetShopSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetShopSettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 店铺目标设置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetShopSettingServiceImpl extends SuperServiceImpl<BiTargetShopSettingMapper, BiTargetShopSettingEntity> implements BiTargetShopSettingService {


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetShopSettingDTO.AddDTO addDTO) {
        BiTargetShopSettingEntity biTargetShopSettingEntity = new BiTargetShopSettingEntity();
        BeanMapperUtils.copy(addDTO, biTargetShopSettingEntity);

        // 数据处理
        handleData(biTargetShopSettingEntity);

        log.info("开始新增店铺目标设置单");
        boolean save = super.save(biTargetShopSettingEntity);
        if(!save) {
            throw new ServiceException("店铺目标设置单保存失败");
        }

        return biTargetShopSettingEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetShopSettingDTO.UpdateDTO updateDTO) {
        BiTargetShopSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "店铺目标设置单"));
        BiTargetShopSettingEntity biTargetShopSettingEntity =  BeanMapperUtils.map(BiTargetShopSettingEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetShopSettingEntity);
        log.info("编辑 开始修改店铺目标设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetShopSettingEntity);
        if(!save) {
            throw new ServiceException("店铺目标设置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(BiTargetShopSettingEntity biTargetShopSettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
