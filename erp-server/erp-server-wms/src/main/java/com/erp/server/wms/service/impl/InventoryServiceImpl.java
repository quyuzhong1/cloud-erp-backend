package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.*;
import com.erp.server.wms.mapper.InventoryMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Classname: InventoryServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-25  12:17
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class InventoryServiceImpl extends SuperServiceImpl<InventoryMapper, InventoryEntity> implements InventoryService {

    @Autowired
    private CommonService commonService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private RedissonClient redisson;

    @Override
    public InventoryEntity findInventory(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        // 组织+仓库+库位+SKU+状态 确定唯一一条记录
        InventoryStatusEnum inventoryStatus = InventoryStatusEnum.of(status);
        String qWarehouseLocationId = StrUtils.null2EmptyWithTrim(warehouseLocationId);
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId,warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, status);
        /**
         * 不控制库位把库位条件置位空字符串（从空库位查询）；
         * 其他控制库位的如果传了则从指定库位出，没传则从空库位出
         */
        if(Objects.equals(Boolean.FALSE, inventoryStatus.getControlLocation())) {
            qWarehouseLocationId = "";
        }
        queryWrapper.eq(InventoryEntity::getWarehouseLocation, qWarehouseLocationId).last("limit 1");
        InventoryEntity inventory = baseMapper.selectOne(queryWrapper);
        return inventory;
    }

    @Override
    public InventoryEntity findInventoryLock(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        // 组织+仓库+库位+SKU+状态 确定唯一一条记录
        // 此处使用读写锁，避免并发情况下读取的数据不一致，读跟读之间不冲突，读写或写写冲突，暂不考虑库位
        InventoryStatusEnum inventoryStatus = InventoryStatusEnum.of(status);
        String lockKey = StrUtil.format( "{}:{}:{}", DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), warehouseId, skuId);
        RReadWriteLock rwLock = redisson.getReadWriteLock(lockKey);
        RLock rlock = rwLock.readLock();
        boolean isLock;
        try {
            isLock = rlock.tryLock(8, TimeUnit.SECONDS);// 防止一直等待，加最大等待时间
            log.info("仓库：【{}】，SKU ID：【{}】，是否获取到锁: {}", warehouseId, skuId, isLock);
            if (!isLock) {
                throw new ServiceException(ApiError.ERROR_1026);
            }
            String qWarehouseLocationId = StrUtils.null2EmptyWithTrim(warehouseLocationId);
            LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(InventoryEntity::getWarehouseId,warehouseId).eq(InventoryEntity::getOrgId, orgId)
                    .eq(InventoryEntity::getSkuId, skuId)
                    .eq(InventoryEntity::getDictInventoryStatus, status);
            /**
             * 不控制库位把库位条件置位空字符串（从空库位查询）；
             * 其他控制库位的如果传了则从指定库位出，没传则从空库位出
             */
            if(Objects.equals(Boolean.FALSE, inventoryStatus.getControlLocation())) {
                qWarehouseLocationId = "";
            }
            queryWrapper.eq(InventoryEntity::getWarehouseLocation, qWarehouseLocationId).last("limit 1");
            InventoryEntity inventory = baseMapper.selectOne(queryWrapper);
            return inventory;
        } catch (InterruptedException e) {
            log.error("仓库id：【{}】，SKU编号：【{}】，获取锁异常", warehouseId, skuId, e );
            throw new ServiceException(ApiError.ERROR_1026);
        } finally {
            //释放锁
            if(rlock.isLocked() && rlock.isHeldByCurrentThread()){ // 锁是否存在，是当前执行线程的锁
                rlock.unlock(); // 释放锁
            }
        }
    }

    @Override
    public List<InventoryEntity> findInventoryCheckLocation(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId,warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, skuId)
                .eq(InventoryEntity::getDictInventoryStatus, status);
        if(StrUtils.isNotEmpty(warehouseLocationId)) {
            queryWrapper.eq(InventoryEntity::getWarehouseLocation, StrUtils.null2EmptyWithTrim(warehouseLocationId));
        } else {
            log.info("组织id：【{}】，仓库id：【{}】,SKU：【{}】，状态：【{}】，库位为空，不作为查询条件", orgId, warehouseId, skuId, status, warehouseLocationId);
        }
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Integer getUsableInventoryTotal(String orgId, String warehouseId, String skuId, String warehouseLocationId) {
        return this.getInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId, InventoryStatusEnum.USABLE.getCode());
    }

    @Override
    public Integer getInventoryTotal(String orgId, String warehouseId, String skuId, String warehouseLocationId, String status) {
        List<InventoryEntity> inventories = this.findInventoryCheckLocation(orgId, warehouseId, skuId, warehouseLocationId, status);
        return CollUtil.isEmpty(inventories) ? 0 : inventories.stream().collect(Collectors.summingInt(InventoryEntity::getQty));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateQtyById(String id, Integer qty, Integer version) {
        LoginUser loginUser = commonService.getUserInfo();
        return inventoryMapper.updateQtyById(id, qty, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

}