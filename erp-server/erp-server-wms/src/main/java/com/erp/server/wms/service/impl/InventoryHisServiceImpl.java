package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.server.wms.mapper.InventoryHisMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.InventoryHisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Classname: InventoryHisServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-27  17:04
 * @Author: zhangchunlin
 */
@Service
public class InventoryHisServiceImpl extends SuperServiceImpl<InventoryHisMapper, InventoryHisEntity> implements InventoryHisService {

    @Autowired
    private InventoryHisMapper inventoryHisMapper;

    @Autowired
    private CommonService commonService;

    @Override
    public InventoryHisEntity findByInfoIdAndBillDate(String infoId, LocalDate billDate) {
        LambdaQueryWrapper<InventoryHisEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(InventoryHisEntity::getInfoId, infoId)
                .eq(InventoryHisEntity::getBillDate, billDate);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public int updateQtyById(String id, Integer qty, Integer version) {
        LoginUser loginUser =  commonService.getUserInfo();
        return inventoryHisMapper.updateQtyById(id, qty, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }


}