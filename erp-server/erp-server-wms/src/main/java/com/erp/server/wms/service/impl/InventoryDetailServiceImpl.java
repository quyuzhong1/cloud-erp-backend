package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.erp.model.wms.entity.InventoryDetailEntity;
import com.erp.server.wms.mapper.InventoryDetailMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.InventoryDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname: InventoryDetailServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:00
 * @Author: zhangchunlin
 */
@Service
public class InventoryDetailServiceImpl extends SuperServiceImpl<InventoryDetailMapper, InventoryDetailEntity> implements InventoryDetailService {

    @Autowired
    private InventoryDetailMapper inventoryDetailMapper;

    @Autowired
    private CommonService commonService;

    @Override
    public InventoryDetailEntity findByInfoIdAndInstockBatchDate(String inventoryInfoId, LocalDate instockBatchDate) {
        LambdaQueryWrapper<InventoryDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(InventoryDetailEntity::getInfoId, inventoryInfoId)
        .eq(InventoryDetailEntity::getInstockBatchDate, instockBatchDate);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<InventoryDetailEntity> findByInventoryIdAndQtyGreatZero(String inventoryInfoId) {
        return lambdaQuery().eq(InventoryDetailEntity::getInfoId, inventoryInfoId).gt(InventoryDetailEntity::getQty, 0).list();
    }

    @Override
    public int updateQtyById(String id, Integer qty, Integer version) {
        LoginUser loginUser = commonService.getUserInfo();
        return inventoryDetailMapper.updateQtyById(id, qty, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

}