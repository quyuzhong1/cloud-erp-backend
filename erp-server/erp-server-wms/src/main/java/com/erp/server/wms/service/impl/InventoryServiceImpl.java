package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.DistributedLockEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.dto.InventoryDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.enums.InventoryDirectEnum;
import com.erp.model.wms.enums.InventoryTransTypeEnum;
import com.erp.server.wms.mapper.InventoryMapper;
import com.erp.server.wms.service.InventoryDetailService;
import com.erp.server.wms.service.InventoryService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    private RedissonClient redisson;

    @Autowired
    private InventoryDetailService inventoryDetailService;

    /**
     * 库存交易业务处理，由于供应链SCM业务会发起feign调用，采用分布式事务
     * @param inventoryDTO
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void trade(InventoryDTO.AddDTO inventoryDTO) {
        // TODO 关账、盘点验证，不允许操作；库存不足验证
        // 交易单据类型
        InventoryTransTypeEnum inventoryTransTypeEnum = inventoryDTO.getTransTypeEnum();
        // 仓库组织
        String orgId = inventoryDTO.getOrgId();
        // 仓库
        String warehouseId = inventoryDTO.getWarehouseId();
        // 操作的sku数据
        List<InventoryDTO.CommonDTO> members = inventoryDTO.getMembers();

        for(InventoryDTO.CommonDTO member : members) {
            // 按照仓库+SKU进行锁定，暂不考虑库位，防止数据冲突
            String lockKey = StrUtil.format( "{}:{}:{}",DistributedLockEnum.WMS_INVENTORY_SKU.getCode(), warehouseId, member.getSkuId());
            RLock lock = redisson.getLock(lockKey);
            boolean isLock;
            try {
                isLock = lock.tryLock(5, TimeUnit.SECONDS);
                log.info("仓库：{}，组织：{}，SKU：{}，交易单据：{}，是否获取到锁: {}", warehouseId, orgId, member.getSkuId(), inventoryTransTypeEnum.getName(), isLock);
                if (!isLock) {
                    throw new ServiceException(ApiError.ERROR_1026);
                }
                // 内部业务逻辑
                dealSkuInventory(inventoryTransTypeEnum, orgId, warehouseId, member);
            } catch (Exception e) {
                log.error("仓库：{}，SKU：{}库存操作时获取锁异常",e);
                throw new ServiceException(ApiError.ERROR_1026);
            } finally {
                //释放锁
                if(lock.isLocked() && lock.isHeldByCurrentThread()){ // 锁是否存在，是当前执行线程的锁
                    lock.unlock(); // 释放锁
                }
            }
        }
    }


    public void checkInventory() {
        // 1.检查是否关账
        // 2.检查是否盘点
        // 3.检查库存不足
    }

    /**
     * 处理库存业务
     * @param inventoryTransTypeEnum
     * @param orgId
     * @param member
     */
    public void dealSkuInventory(InventoryTransTypeEnum inventoryTransTypeEnum, String orgId, String warehouseId, InventoryDTO.CommonDTO member) {
        // 1.记录到实时库存表；按照仓库+组织+库位+SKU查询是否存在，不存在则新增，存在则更新数据
        String inventoryId = addOrUpdateInventory(inventoryTransTypeEnum, orgId, warehouseId, member);
        // 2.新增或修改库存明细
        addOrUpdateInventoryMember(inventoryId, member);
        // 3.记录交易流水
        recordFlowTransaction();
    }

    /**
     * 新增或修改实时库存
     */
    public String addOrUpdateInventory(InventoryTransTypeEnum inventoryTransTypeEnum, String orgId, String warehouseId, InventoryDTO.CommonDTO member) {
        lambdaQuery().eq(InventoryEntity::getWarehouseId,warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, member.getSkuId());

        // 组织+仓库+库位+SKU+状态 确定唯一一条记录
        LambdaQueryWrapper<InventoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryEntity::getWarehouseId,warehouseId).eq(InventoryEntity::getOrgId, orgId)
                .eq(InventoryEntity::getSkuId, member.getSkuId())
                .eq(InventoryEntity::getWarehouseLocation, StrUtils.null2EmptyWithTrim(member.getWarehouseLocation()))
                .eq(InventoryEntity::getStatusCode, member.getInventoryStatusEnum().getCode());
        InventoryEntity inventory = baseMapper.selectOne(queryWrapper);
        if(Objects.isNull(inventory)) {
            log.info("仓库【{}】，组织：【{}】，库位：【{}】，SKU：【{}】，操作单据：【{}】，状态【{}】在库存实时表中不存在数据，新增数据", warehouseId, orgId, member.getWarehouseLocation(),member.getSkuId(), inventoryTransTypeEnum.getName(), member.getInventoryStatusEnum().getName());
            inventory = new InventoryEntity();
            inventory.setWarehouseId(warehouseId);
            inventory.setOrgId(orgId);
            inventory.setWarehouseLocation(member.getWarehouseLocation());
            inventory.setSkuId(member.getSkuId());
            inventory.setSkuNo(member.getSkuNo());
            inventory.setStatusCode(member.getInventoryStatusEnum().getCode());
            inventory.setQty(0);
            super.save(inventory);
        }
        Integer originQty = inventory.getQty();
        Integer opQty = member.getQty();
        Integer afterQty = Objects.equals(InventoryDirectEnum.INVENTORY_IN.getCode(), member.getInventoryDirectEnum().getCode()) ?
                (originQty + opQty) : (originQty - opQty);
        if(afterQty < 0) {
            afterQty = 0;
        }
        log.info("仓库【{}】，组织：【{}】，库位：【{}】, SKU：【{}】，操作单据：【{}】，状态【{}】原数量【{}】，操作方向：【{}】，本次操作数量【{}】，操作后数量【{}】", warehouseId, orgId, member.getWarehouseLocation(), member.getSkuId(), inventoryTransTypeEnum.getName(), member.getInventoryStatusEnum().getName(),
                originQty, member.getInventoryDirectEnum().getName(), opQty, afterQty);

        inventory.setQty(afterQty);
        super.updateById(inventory);
        return inventory.getId();
    }

    /**
     * 新增或修改库存明细
     */
    public void addOrUpdateInventoryMember(String inventoryId, InventoryDTO.CommonDTO member) {

    }

    /**
     * 记录库存交易流水
     */
    public void recordFlowTransaction() {

    }



}