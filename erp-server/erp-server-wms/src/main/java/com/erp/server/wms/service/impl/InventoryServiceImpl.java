package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.dto.InventoryDTO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.mapper.InventoryMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
                log.info("库存状态：【{}】不控制库位", inventoryStatus.getName());
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

        warehouseLocationId = StrUtils.null2EmptyWithTrim(warehouseLocationId);
        queryWrapper.eq(InventoryEntity::getWarehouseLocation, StrUtils.null2EmptyWithTrim(warehouseLocationId));
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Integer getUsableInventoryTotal(String orgId, String warehouseId, String skuId, String warehouseLocationId) {
        return this.getInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId, InventoryStatusEnum.USABLE.getCode());
    }

    /**
     * 根据skuIds 仓库 ，组织 仓位 获取到 sku即时库存
     * @author yl
     * @date 2023-05-16 17:06
     * @param skuIds
     * @param warehouseId
     * @param orgId
     * @param warehouseLocationId
     * @return java.util.List<com.erp.model.wms.dto.InventoryDTO.SkuInventoryTotalDTO>
     */
    @Override
    public List<InventoryDTO.SkuInventoryTotalDTO> listSkuInventory(List<String> skuIds, String warehouseId, String orgId, String warehouseLocationId) {


        return null;
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

    @Override
    public List<InventoryEntity> listPickingDetailInventory(PickingDetailDTO.InventoryParamDTO dto) {
        //根据组织、仓库、sku查询可用库存
        List<InventoryEntity> inventoryList = this.findInventoryCheckLocation(dto.getOrgId(), dto.getWarehouseId(), dto.getSkuId(), null, InventoryStatusEnum.USABLE.getCode());

        log.info("组织【{}】、仓库【{}】、SKU【{}】查询可用库存",dto.getOrgName(),dto.getWarehouseName(),dto.getSkuNo());

        if (CollectionUtils.isEmpty(inventoryList)) {
            throw new ServiceException(new ApiResult(1,String.format("组织【%s】、仓库【%s】、SKU【%s】可用库存不足",dto.getOrgName(),dto.getWarehouseName(),dto.getSkuNo())));
        }
        /**
         * 拣货规则：
         * 1、如果可用库存存在超过拣货数量则直接顺序取
         * 2、如果可用库存不存在超过拣货数量则倒序取（如果剩余拣货数量与库存数量匹配则直接取）
         */

        List<InventoryEntity> resultList  = new ArrayList<>();

        //拣货数量
        Integer qty = dto.getQty();

        //1、如果可用库存存在超过拣货数量则直接顺序取
        InventoryEntity inventoryEntity = inventoryList.stream().filter(obj -> obj.getQty().intValue() >= dto.getQty().intValue()).sorted(Comparator.comparing(InventoryEntity::getQty)).findFirst().orElse(null);
        if (ObjectUtils.isNotEmpty(inventoryEntity)) {
            inventoryEntity.setQty(qty);
            resultList.add(inventoryEntity);
            return resultList;
        }
        //2、如果可用库存不存在超过拣货数量则倒序取（如果剩余拣货数量与库存数量匹配则直接取）
        List<InventoryEntity> sortList = inventoryList.stream().filter(obj -> dto.getQty().intValue() > obj.getQty().intValue()).sorted(Comparator.comparing(InventoryEntity::getQty).reversed()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sortList)) {
            throw new ServiceException(new ApiResult(1,String.format("组织【%s】、仓库【%s】、SKU【%s】可用库存不足",dto.getOrgName(),dto.getWarehouseName(),dto.getSkuNo())));
        }
        for (InventoryEntity inventory : sortList) {
            //如果拣货数量为0则跳出循环
            if (MathUtil.compareTo(qty,MathUtil.ZERO) == MathUtil.ZERO) {
                break;
            }
            //剩余拣货数量
            qty = qty - inventory.getQty();

            //添加拣货明细
            resultList.add(inventory);

            //判断剩余数量是否存在相同库存数量，如果存在则直接匹配
            Integer finalQty = qty;
            List<String> inventoryIds = resultList.stream().map(InventoryEntity::getId).collect(Collectors.toList());
            InventoryEntity matches = inventoryList.stream().filter(obj -> obj.getQty().intValue() == finalQty.intValue() && !inventoryIds.contains(obj.getId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(matches)) {
                //添加拣货明细
                resultList.add(matches);
                break;
            }

        }
        return resultList;
    }

}