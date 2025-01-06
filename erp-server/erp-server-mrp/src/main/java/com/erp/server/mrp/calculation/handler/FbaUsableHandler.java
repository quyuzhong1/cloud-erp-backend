package com.erp.server.mrp.calculation.handler;

import com.erp.model.mrp.dto.ReplenishmentInventoryDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class FbaUsableHandler extends AbstractSkuCalculationHandler {
    @Resource
    private FbaInTransitHandler fbaInTransitHandler;
    @Override
    public SkuCalculationHandler getNextHandler(List<ReplenishmentResultDTO> r) {
        return fbaInTransitHandler;
    }

    @Override
    public boolean shouldHandle(ReplenishmentResultDTO dto) {
        return CfgRulePlatformTypeEnum.AMAZON.getCode().equals(dto.getReplenishment().getPlatformType());
    }

    @Override
    public void doHandle(ReplenishmentResultDTO replenishmentResultDTO, List<ReplenishmentResultDTO> r) {

        Integer qty = replenishmentResultDTO.getInventoryDTO().getFbaUsableList()
                .stream().filter(v -> v.getSkuNo().equals(replenishmentResultDTO.getReplenishment().getSkuNo()))
                .filter(v -> v.getWarehouseId().equals(replenishmentResultDTO.getReplenishment().getFbaWarehouseId()))
                .map(ReplenishmentInventoryDTO.FbaUsableDTO::getQty)
                .findFirst()
                .orElse(0);
        replenishmentResultDTO.getReplenishmentDetail().setFbaUsableQty(qty);
    }

}
