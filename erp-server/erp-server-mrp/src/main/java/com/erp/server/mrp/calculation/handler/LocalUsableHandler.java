package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.CfgRuleWarehouseDTO;
import com.erp.model.mrp.dto.LocalInventoryDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRuleWarehouseTypeEnum;
import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import com.erp.server.mrp.service.CfgRuleCommonService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocalUsableHandler extends AbstractSkuCalculationHandler {
    @Resource
    private LocalInTransitHandler localInTransitHandler;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private CfgRuleCommonService cfgRuleCommonService;
    @Override
    public SkuCalculationHandler getNextHandler(CfgRuleStrategyDTO cfgRuleStrategy, ReplenishmentResultDTO replenishmentResult) {
        return localInTransitHandler;
    }

    @Override
    public boolean shouldHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        return true;
    }

    @Override
    public void doHandle(CfgRuleStrategyDTO cfgRuleStrategyDTO, ReplenishmentResultDTO replenishmentResultDTO) {
        CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult = cfgRuleStrategyDTO.getWarehouseResult();
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> localUsableDetail = new ArrayList<>();
        CfgRuleWarehouseTypeEnum warehouseType = Boolean.TRUE.equals(warehouseResult.getIsEnableVirtual()) ? CfgRuleWarehouseTypeEnum.VIRTUAL : CfgRuleWarehouseTypeEnum.LOCAL;
        List<LocalInventoryDTO> inventoryList;
        if (Boolean.TRUE.equals(warehouseResult.getIsEnableVirtual())) {
            inventoryList = replenishmentResultDTO.getInventoryDTO().getVirtualUsableList()
                    .stream().filter(v -> v.getSkuId().equals(replenishmentResultDTO.getReplenishment().getSkuId()))
                    .map(v -> new LocalInventoryDTO(v.getVirtualWarehouseId(), v.getQty()))
                    .collect(Collectors.toList());

        } else {
            inventoryList = replenishmentResultDTO.getInventoryDTO().getLocalUsableList()
                    .stream().filter(v -> v.getSkuId().equals(replenishmentResultDTO.getReplenishment().getSkuId()))
                    .map(v -> new LocalInventoryDTO(v.getWarehouseId(), v.getQty()))
                    .collect(Collectors.toList());
        }
        int qty = inventoryService.getAllocateQty(replenishmentResultDTO, cfgRuleStrategyDTO.getWarehouseResult().getLocalWarehouseList(),
                inventoryList, localUsableDetail, ReplenishmentInventoryTypeEnum.LOCAL_USABLE, warehouseType);
        replenishmentResultDTO.setLocalUsableDetail(localUsableDetail);
        replenishmentResultDTO.getReplenishmentDetail().setLocalUsableQty(qty);
    }
}
