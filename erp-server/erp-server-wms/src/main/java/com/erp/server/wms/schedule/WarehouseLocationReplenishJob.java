package com.erp.server.wms.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.entity.WarehouseLocationReplenishEntity;
import com.erp.model.wms.entity.WarehouseLocationSafetyInventoryEntity;
import com.erp.model.wms.enums.ReplenishBillStatusEnum;
import com.erp.model.wms.enums.ReplenishTypeEnum;
import com.erp.server.wms.mapper.InventoryMapper;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.WarehouseLocationReplenishService;
import com.erp.server.wms.service.WarehouseLocationSafetyInventoryService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @date 2024-07-10
 * @author tanmujin
 */
@Slf4j
@Component
public class WarehouseLocationReplenishJob {

    @Resource
    private WarehouseLocationReplenishService replenishService;
    @Resource
    private WarehouseLocationSafetyInventoryService safetyInventoryService;
    @Resource
    private InventoryMapper inventoryMapper;
    @Resource
    private WarehouseLocationService warehouseLocationService;

    @XxlJob("deleteReplenishBill")
    public ReturnT<String> deleteReplenishBill(){
        List<WarehouseLocationReplenishEntity> deleteList = replenishService.list(new QueryWrapper<WarehouseLocationReplenishEntity>()
                .eq("status", ReplenishBillStatusEnum.WAIT_HANDLE.getCode()).eq("source_type", ReplenishTypeEnum.SAFETY_INVENTORY.getCode()));
        if(! deleteList.isEmpty()){
            List<String> deleteIds = deleteList.stream().map(item -> item.getId()).collect(Collectors.toList());
            replenishService.getBaseMapper().deleteBatchIds(deleteIds);
        }
        return ReturnT.SUCCESS;
    }

    @XxlJob("generateReplenishBill")
    public ReturnT<String> generateReplenishBill(){
        replenishService.generateReplenishBill();
        return ReturnT.SUCCESS;
    }
}
