package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.LocalInventoryDTO;
import com.erp.model.mrp.dto.OverseasProviderWarehouseDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleWarehouseTypeEnum;
import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;
import com.erp.server.mrp.calculation.service.InventoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OverseasUsableHandler extends AbstractSkuCalculationHandler {
    @Resource
    private OverseasInTransitHandler overseasInTransitHandler;
    @Resource
    private InventoryService inventoryService;


    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return overseasInTransitHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO dto) {
        return CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(dto.getReplenishment().getPlatformType())
                || Boolean.TRUE.equals(dto.getCfgRuleStrategy().getWarehouseResult().getIsEnableOverseas());
    }


    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {
        Map<String, String> codeMap = replenishmentResultDTO.getOverseasProviderWarehouseList()
                .stream().filter(v -> Boolean.FALSE.equals(v.getDisabled()))
                .collect(Collectors.toMap(OverseasProviderWarehouseDTO::getPlatformWarehouseCode,
                        OverseasProviderWarehouseDTO::getWarehouseId, (o1, o2) -> o1));
        List<ReplenishmentResultDTO.ReplenishmentInventoryDetailDTO> overseasUsableDetail = new ArrayList<>();
        List<LocalInventoryDTO> invetoryList = replenishmentResultDTO.getInventoryDTO().getOverseasUsableList()
                .stream().filter(v -> v.getSkuId().equals(replenishmentResultDTO.getReplenishment().getSkuId()))
                .filter(v -> codeMap.containsKey(v.getWarehouseCode()))
                .map(v -> new LocalInventoryDTO(codeMap.get(v.getWarehouseCode()), v.getQty(), null))
                .collect(Collectors.toList());
        Map<String, Integer> shopDemandQty = r.stream()
                .collect(Collectors.toMap(v -> v.getReplenishment().getShopId(), ReplenishmentResultDTO::getShopDemandQty));
        int qty = inventoryService.getAllocateQty(replenishmentResultDTO, replenishmentResultDTO.getCfgRuleStrategy().getWarehouseResult().getOverseasWarehouseList(), invetoryList, overseasUsableDetail,
                shopDemandQty,
                ReplenishmentInventoryTypeEnum.OVERSEAS_USABLE, CfgRuleWarehouseTypeEnum.OVERSEAS);
        replenishmentResultDTO.setOverseasUsableDetail(overseasUsableDetail);
        replenishmentResultDTO.getReplenishmentDetail().setOverseasUsableQty(qty);
    }
}
