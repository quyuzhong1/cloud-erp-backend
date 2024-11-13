package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.LocalHistoryInventoryEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.server.mrp.mapper.LocalHistoryInventoryMapper;
import com.erp.server.mrp.service.LocalHistoryInventoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 库存表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
@Service
public class LocalHistoryInventoryServiceImpl extends SuperServiceImpl<LocalHistoryInventoryMapper, LocalHistoryInventoryEntity> implements LocalHistoryInventoryService {

    @Override
    public void saveTodayInventory(List<InventoryEntity> localHistoryInventory, LocalDate calculationDate) {
        List<LocalHistoryInventoryEntity> entityList = list(Wrappers.<LocalHistoryInventoryEntity>lambdaQuery().eq(LocalHistoryInventoryEntity::getBillDate, calculationDate));
        List<LocalHistoryInventoryEntity> entities = localHistoryInventory.parallelStream()
                .map(v -> {
                    LocalHistoryInventoryEntity inventory = entityList.stream()
                            .filter(e -> v.getSkuId().equals(e.getSkuId()))
                            .filter(e -> v.getWarehouseId().equals(e.getWarehouseId()))
                            .filter(e -> v.getDictInventoryStatus().equals(e.getDictInventoryStatus()))
                            .filter(e -> v.getWarehouseLocation().equals(e.getWarehouseLocation()))
                            .filter(e -> v.getOrgId().equals(e.getOrgId()))
                            .findFirst()
                            .orElse(new LocalHistoryInventoryEntity());
                    v.setId(null);
                    BeanUtils.copyProperties(v, inventory);
                    inventory.setBillDate(calculationDate);
                    inventory.setId(inventory.getId());
                    return inventory;
                }).collect(Collectors.toList());
        saveOrUpdateBatch(entities);
    }
}
