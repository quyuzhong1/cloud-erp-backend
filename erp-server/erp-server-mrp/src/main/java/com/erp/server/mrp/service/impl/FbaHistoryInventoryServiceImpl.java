package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.server.mrp.mapper.FbaHistoryInventoryMapper;
import com.erp.server.mrp.service.FbaHistoryInventoryService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * fba历史库存 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@Service
public class FbaHistoryInventoryServiceImpl extends SuperServiceImpl<FbaHistoryInventoryMapper, FbaHistoryInventoryEntity> implements FbaHistoryInventoryService {

    @Override
    public void saveTodayInventory(List<FbaInventoryEntity> inventoryEntities, LocalDate calculationDate) {
        List<FbaHistoryInventoryEntity> entityList = list(Wrappers.<FbaHistoryInventoryEntity>lambdaQuery().eq(FbaHistoryInventoryEntity::getBillDate, calculationDate));
        List<FbaHistoryInventoryEntity> entities = inventoryEntities.parallelStream()
                .map(v -> {
                    FbaHistoryInventoryEntity inventory = entityList.stream()
                            .filter(e -> v.getWarehouseId().equals(e.getWarehouseId()))
                            .filter(e -> v.getAsin().equals(e.getAsin()))
                            .filter(e -> v.getMsku().equals(e.getMsku()))
                            .filter(e -> v.getFnSku().equals(e.getFnSku()))
                            .findFirst()
                            .orElse(new FbaHistoryInventoryEntity());
                    v.setId(null);
                    BeanUtils.copyProperties(v, inventory);
                    inventory.setBillDate(calculationDate);
                    inventory.setId(inventory.getId());
                    return inventory;
                }).collect(Collectors.toList());
        saveOrUpdateBatch(entities);
    }

    @Override
    public List<FbaHistoryInventoryEntity> listBySkuNo(String skuNo, String fbaWarehouseId) {
        return list(Wrappers.<FbaHistoryInventoryEntity>lambdaQuery()
                .eq(FbaHistoryInventoryEntity::getSkuNo, skuNo)
                .eq(FbaHistoryInventoryEntity::getWarehouseId, fbaWarehouseId)
        );
    }
}
