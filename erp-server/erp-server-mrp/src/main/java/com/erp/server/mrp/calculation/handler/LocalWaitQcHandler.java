package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.LocalInventoryDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleWarehouseTypeEnum;
import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocalWaitQcHandler extends AbstractSkuCalculationHandler {
    @Resource
    private LocalInTransitHandler localInTransitHandler;
    @Resource
    private InventoryService inventoryService;


    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return localInTransitHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO dto) {
        return true;
    }

    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localWaitQcDetail = new ArrayList<>();
        List<LocalInventoryDTO> inventoryList = replenishmentResultDTO.getInventoryDTO().getLocalWaitQcList()
                    .stream().filter(v -> v.getSkuId().equals(replenishmentResultDTO.getReplenishment().getSkuId()))
                    .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                    .collect(Collectors.toList());
        int qty = inventoryService.getAllocateQty(replenishmentResultDTO, replenishmentResultDTO.getCfgRuleStrategy().getWarehouseResult().getLocalWarehouseList(),
                inventoryList, localWaitQcDetail, ReplenishmentInventoryTypeEnum.LOCAL_WAIT_QC, CfgRuleWarehouseTypeEnum.LOCAL);
        replenishmentResultDTO.setLocalWaitQcDetail(localWaitQcDetail);
        replenishmentResultDTO.getReplenishmentDetail().setLocalWaitQcQty(qty);
    }
}
