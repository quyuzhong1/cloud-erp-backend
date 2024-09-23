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
        List<WarehouseLocationSafetyInventoryEntity> list = safetyInventoryService.lambdaQuery().list();

        //即时库存数据
        List<String> warehouseIds = list.stream().map(item -> item.getWarehouseId()).distinct().collect(Collectors.toList());
        List<String> skuIds = list.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
//        List<String> warehouseLocations = list.stream().map(item -> item.getWarehouseLocation()).distinct().collect(Collectors.toList());
//        InventoryDTO.SearchParamDTO searchParamDTO = new InventoryDTO.SearchParamDTO();
//        searchParamDTO.setWarehouseIdList(warehouseIds);
//        searchParamDTO.setSkuIdList(skuIds);
        List<InventoryDTO.PagingViewDTO> inventoryList = inventoryMapper.exportByLocation(new InventoryDTO.SearchParamDTO(), null, null);

        Map<String, InventoryDTO.PagingViewDTO> inventoryMap = inventoryList.stream()
                .collect(Collectors.toMap(item1 -> item1.getWarehouseId() + "#" + item1.getWarehouseLocation() + "#" + item1.getSkuId(), item2 -> item2, (o1, o2) -> o2));
        List<WarehouseLocationDTO.MappingDTO> locationMappingList = new ArrayList<>();
        for (String warehouseId : warehouseIds) {
            List<WarehouseLocationDTO.MappingDTO> mappingDTOS = warehouseLocationService.listArea2LocationMapping(warehouseId);
            locationMappingList.addAll(mappingDTOS);
        }
        //遍历仓位安全库存
        for (WarehouseLocationSafetyInventoryEntity entity : list) {
            String warehouseId = entity.getWarehouseId();
            String warehouseLocation = entity.getWarehouseLocation();
            String skuId = entity.getSkuId();
            String skuNo = entity.getSkuNo();
            InventoryDTO.PagingViewDTO inventory = inventoryMap.get(warehouseId + "#" + warehouseLocation + "#" + skuId);
            if(inventory == null){
                continue;
            }

            if(inventory.getUsableQty() < entity.getSafetyQty()){
                WarehouseLocationReplenishDTO.AddDTO dto = new WarehouseLocationReplenishDTO.AddDTO();
                dto.setWarehouseId(warehouseId);
                dto.setWarehouseLocation(warehouseLocation);
                dto.setSkuId(skuId);
                WarehouseLocationDTO.MappingDTO mappingDTO = locationMappingList.stream()
                        .filter(item -> item.getWarehouseId().equalsIgnoreCase(warehouseId)
                                && item.getLocationCode().equalsIgnoreCase(warehouseLocation)).findFirst().get();
                dto.setWarehouseArea(mappingDTO.getAreaCode());
                dto.setSourceType(ReplenishTypeEnum.SAFETY_INVENTORY);
                dto.setSkuNo(skuNo);
                int qty = 0;
                if(entity.getSafetyQty() == null || entity.getSafetyQty() == 0){
                    //没有设置补货触发量时，不参与补货计算
                    continue;
                }
                if(entity.getMaxQty() != null && entity.getMaxQty() != 0){
                    //有设置补货上限量时：等于补货上限量-当前可用库存
                    qty = entity.getMaxQty() - inventory.getUsableQty();
                }
                if(entity.getMaxQty() != null && entity.getMaxQty() == 0){
                    //没有设置补货上限量时：等于安全库存-当前可用库存
                    qty = entity.getSafetyQty() - inventory.getUsableQty();
                }
                if(inventory.getUsableQty() >= entity.getSafetyQty()){
                    continue;
                }
                dto.setQty(qty);
                replenishService.add(dto);
            }
        }

        return ReturnT.SUCCESS;
    }
}
