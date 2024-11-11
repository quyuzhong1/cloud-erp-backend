package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.VirtualInventoryHistoryEntity;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.server.mrp.mapper.VirtualInventoryHistoryMapper;
import com.erp.server.mrp.service.VirtualInventoryHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 虚拟库存表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-27
 */
@Service
public class VirtualInventoryHistoryServiceImpl extends SuperServiceImpl<VirtualInventoryHistoryMapper, VirtualInventoryHistoryEntity> implements VirtualInventoryHistoryService {

    @Override
    public void saveTodayInventory(List<VirtualInventoryEntity> virtualInventory, LocalDate calculationDate) {
        List<VirtualInventoryHistoryEntity> entityList = list(Wrappers.<VirtualInventoryHistoryEntity>lambdaQuery().eq(VirtualInventoryHistoryEntity::getBillDate, calculationDate));
        List<VirtualInventoryHistoryEntity> entities = virtualInventory.parallelStream()
                .map(v -> {
                    VirtualInventoryHistoryEntity inventory = entityList.stream()
                            .filter(e -> v.getSkuId().equals(e.getSkuId()))
                            .filter(e -> v.getWarehouseId().equals(e.getWarehouseId()))
                            .filter(e -> v.getDictInventoryStatus().equals(e.getDictInventoryStatus()))
                            .filter(e -> v.getVirtualWarehouseId().equals(e.getVirtualWarehouseId()))
                            .findFirst()
                            .orElse(new VirtualInventoryHistoryEntity());
                    v.setId(null);
                    BeanUtils.copyProperties(v, inventory);
                    inventory.setBillDate(calculationDate);
                    inventory.setId(inventory.getId());
                    return inventory;
                }).collect(Collectors.toList());
        saveOrUpdateBatch(entities);
    }
}
