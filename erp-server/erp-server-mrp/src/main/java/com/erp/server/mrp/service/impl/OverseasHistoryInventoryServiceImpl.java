package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.mrp.entity.OverseasHistoryInventoryEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.server.mrp.mapper.OverseasHistoryInventoryMapper;
import com.erp.server.mrp.service.OverseasHistoryInventoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外仓库存 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
@Service
public class OverseasHistoryInventoryServiceImpl extends SuperServiceImpl<OverseasHistoryInventoryMapper, OverseasHistoryInventoryEntity> implements OverseasHistoryInventoryService {

    @Override
    public void saveTodayInventory(List<OverseasInventoryEntity> overseasHistoryInventory, LocalDate calculationDate) {
        List<OverseasHistoryInventoryEntity> entityList = list(Wrappers.<OverseasHistoryInventoryEntity>lambdaQuery().eq(OverseasHistoryInventoryEntity::getBillDate, calculationDate));
        List<OverseasHistoryInventoryEntity> entities = overseasHistoryInventory.parallelStream()
                .map(v -> {
                    OverseasHistoryInventoryEntity inventory = entityList.stream()
                            .filter(e -> v.getWarehouseCode().equals(e.getWarehouseCode()))
                            .filter(e -> v.getDictPlatform().equals(e.getDictPlatform()))
                            .filter(e -> v.getSkuId().equals(e.getSkuId()))
                            .findFirst()
                            .orElse(new OverseasHistoryInventoryEntity());
                    v.setId(null);
                    BeanUtils.copyProperties(v, inventory);
                    inventory.setBillDate(calculationDate);
                    inventory.setId(inventory.getId());
                    return inventory;
                }).collect(Collectors.toList());
        ApplicationContextUtils.getBean(OverseasHistoryInventoryServiceImpl.class).saveOrUpdateBatch(entities);
    }
}
