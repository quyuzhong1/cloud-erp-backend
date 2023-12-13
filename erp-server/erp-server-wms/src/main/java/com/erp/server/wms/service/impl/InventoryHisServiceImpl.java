package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.server.wms.mapper.InventoryHisMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.InventoryHisService;
import com.erp.server.wms.service.TransactionFlowService;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Classname: InventoryHisServiceImpl

 * @CreateTime: 2023-04-27  17:04
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryHisServiceImpl extends SuperServiceImpl<InventoryHisMapper, InventoryHisEntity> implements InventoryHisService {


    @Autowired
    private CommonService commonService;
    @Resource
    private TransactionFlowService transactionFlowService;

    @Override
    public InventoryHisEntity findInventory(String infoId, LocalDate billDate) {
        LambdaQueryWrapper<InventoryHisEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(InventoryHisEntity::getInfoId, infoId)
                .eq(InventoryHisEntity::getBillDate, billDate)
                .last("limit 1");;
        return baseMapper.selectOne(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateQtyById(String id, Integer qty) {
        LoginUser loginUser =  commonService.getUserInfo();
        return baseMapper.updateQtyById(id, qty, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
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
            int updateCnt = this.updateQtyById(inventoryHis.getId(), qty);
            if(updateCnt != 1) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
        }
    }

    @Override
    public InventoryHisEntity findLastInventory(String inventoryId, LocalDate localDate) {
        LambdaQueryWrapper<InventoryHisEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(InventoryHisEntity::getInfoId, inventoryId)
                .le(InventoryHisEntity::getBillDate, localDate)
                .orderByDesc(InventoryHisEntity::getBillDate)
                .last("limit 1");;
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void overrideInventoryHis(LocalDate startTime, LocalDate endTime, String status, String inventoryId) {
        // 查询历史库存
        List<InventoryHisEntity> lockInventoryHisList = lambdaQuery()
                .le(ObjUtil.isNotEmpty(endTime), InventoryHisEntity::getBillDate, endTime)
                .eq(ObjUtil.isNotEmpty(inventoryId), InventoryHisEntity::getInfoId, inventoryId)
                .last("for update")
                .list();
        if(CollectionUtil.isEmpty(lockInventoryHisList)){
            XxlJobHelper.log("需要修复历史库存列表为空！");
            return;
        }
        Map<String, List<InventoryHisEntity>> inventoryHisMap = lockInventoryHisList
                .stream()
                .sorted(Comparator.comparing(InventoryHisEntity::getInfoId))
                .collect(Collectors.groupingBy(item -> StrUtil.format("{}", item.getInfoId())));
        // 查询所有流水
        List<TransactionFlowEntity> lockFlowList = transactionFlowService.lambdaQuery()
                .lt(ObjUtil.isNotEmpty(endTime), TransactionFlowEntity::getTradeTime, endTime.plusDays(1))
                .eq(ObjUtil.isNotEmpty(inventoryId), TransactionFlowEntity::getInventoryId, inventoryId)
                .last("for update")
                .list();
        if(CollectionUtil.isEmpty(lockFlowList)){
            XxlJobHelper.log("相关库存流水列表为空！");
            return;
        }
        Map<String, Integer> transactionFlowMap = lockFlowList
                .stream()
                .collect(Collectors.groupingBy(item -> StrUtil.format("{}_{}", item.getInventoryId(), item.getTradeTime().toLocalDate()),
                        Collectors.summingInt(TransactionFlowEntity::getQty)));
        Map<String, Integer> transactionFlowBillDataMap = lockFlowList
                .stream()
                .collect(Collectors.groupingBy(item -> StrUtil.format("{}_{}", item.getInventoryId(), item.getBillDate()),
                        Collectors.summingInt(TransactionFlowEntity::getQty)));
        for (String key : inventoryHisMap.keySet()) {
            List<InventoryHisEntity> hisEntityList = inventoryHisMap.get(key);
            AtomicReference<Integer> curQty = new AtomicReference<>(0);
            hisEntityList.stream().sorted(Comparator.comparing(InventoryHisEntity::getBillDate))
                .forEachOrdered(item -> {
                    Integer tradeQty = transactionFlowMap.get(StrUtil.format("{}_{}", key, item.getBillDate()));
                    if(null == tradeQty){
                        tradeQty = transactionFlowBillDataMap.get(StrUtil.format("{}_{}", key, item.getBillDate()));
                    }
                    Integer finalTradeQty = null !=  tradeQty ? tradeQty : 0;
                    curQty.updateAndGet(v -> v + finalTradeQty);
                    updateById(new InventoryHisEntity(item.getId(), curQty));
                });
        }
    }


}