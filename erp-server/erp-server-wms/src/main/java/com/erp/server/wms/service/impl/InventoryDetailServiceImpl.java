package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    public InventoryDetailEntity findOneDetail(String inventoryInfoId, LocalDate instockBatchDate) {
        LambdaQueryWrapper<InventoryDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(InventoryDetailEntity::getInfoId, inventoryInfoId)
        .eq(InventoryDetailEntity::getInstockBatchDate, instockBatchDate).last("limit 1");
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<InventoryDetailEntity> findListQtyGreatZero(String inventoryInfoId) {
        List<InventoryDetailEntity> inventoryDetails = lambdaQuery().eq(InventoryDetailEntity::getInfoId, inventoryInfoId).gt(InventoryDetailEntity::getQty, 0).list();
        if(CollUtil.isNotEmpty(inventoryDetails)) {
            // TODO 明确是否能存储到微秒
            inventoryDetails = inventoryDetails.stream().sorted(Comparator.comparing(InventoryDetailEntity::getCreateTime)).collect(Collectors.toList());
        }
        return inventoryDetails;
    }

    @Override
    public int updateQtyById(String id, Integer qty, Integer version) {
        LoginUser loginUser = commonService.getUserInfo();
        return inventoryDetailMapper.updateQtyById(id, qty, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

}