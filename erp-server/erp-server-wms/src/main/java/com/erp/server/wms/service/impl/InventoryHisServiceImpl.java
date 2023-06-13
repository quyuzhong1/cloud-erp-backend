package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.server.wms.mapper.InventoryHisMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.InventoryHisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Classname: InventoryHisServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-27  17:04
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryHisServiceImpl extends SuperServiceImpl<InventoryHisMapper, InventoryHisEntity> implements InventoryHisService {

    @Autowired
    private InventoryHisMapper inventoryHisMapper;

    @Autowired
    private CommonService commonService;

    @Override
    public InventoryHisEntity findInventory(String infoId, LocalDate billDate) {
        LambdaQueryWrapper<InventoryHisEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(InventoryHisEntity::getInfoId, infoId)
                .eq(InventoryHisEntity::getBillDate, billDate).last("limit 1");;
        return baseMapper.selectOne(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateQtyById(String id, Integer qty, Integer version) {
        LoginUser loginUser =  commonService.getUserInfo();
        return inventoryHisMapper.updateQtyById(id, qty, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addOrUpdate(String inventoryInfoId, LocalDate billDate, Integer qty) {
        InventoryHisEntity inventoryHis = this.findInventory(inventoryInfoId, billDate);
        if (Objects.isNull(inventoryHis)) {
            inventoryHis = new InventoryHisEntity();
            inventoryHis.setInfoId(inventoryInfoId);
            inventoryHis.setBillDate(billDate);
            inventoryHis.setQty(qty);
            inventoryHis.setVersion(1);
            boolean save = super.save(inventoryHis);
            ValidatorUtil.isTrue(save, ()->new ServiceException("库存数据保存失败"));
        } else {
            int updateCnt = this.updateQtyById(inventoryHis.getId(), qty, inventoryHis.getVersion());
            if(updateCnt != 1) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
        }
    }


}