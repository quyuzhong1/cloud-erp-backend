package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.InventoryDetailEntity;
import com.erp.server.wms.mapper.InventoryDetailMapper;
import com.erp.server.wms.service.InventoryDetailService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @Classname: InventoryDetailServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:00
 * @Author: zhangchunlin
 */
@Service
public class InventoryDetailServiceImpl extends SuperServiceImpl<InventoryDetailMapper, InventoryDetailEntity> implements InventoryDetailService {

    @Override
    public InventoryDetailEntity findByInfoIdAndInstockBatchDate(String inventoryInfoId, LocalDate instockBatchDate) {
        LambdaQueryWrapper<InventoryDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(InventoryDetailEntity::getInfoId, inventoryInfoId)
        .eq(InventoryDetailEntity::getInstockBatchDate, instockBatchDate);
        return baseMapper.selectOne(queryWrapper);
    }

}