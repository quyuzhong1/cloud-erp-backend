package com.erp.server.mrp.calculation.strategy.platform;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.SalesQtyTypeEnum;
import com.erp.server.mrp.calculation.service.SalesService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class AmazonCalculationStrategy implements PlatformCalculationStrategy {

    @Resource
    private SalesService salesService;

    @Override
    public List<ReplenishmentResultDTO.SalesInfoAllDTO> calculationHistorySale(String calcDate, CfgRuleSalesQtyEntity cfgRuleSalesQty) {
        // 以销售订单订单创建时间计算销量
        if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(cfgRuleSalesQty.getSalesQtyType())) {
            return salesService.listAllAmzSalesBySob2c(calcDate, cfgRuleSalesQty.getOrderType());
        } else {
            // 以销售出库单出库时间计算销量
            return salesService.listAllAmzSalesBySoOutStock(calcDate, cfgRuleSalesQty.getOrderType());
        }
    }







    @Override
    public CfgRulePlatformTypeEnum getPlatform() {
        return CfgRulePlatformTypeEnum.AMAZON;
    }
}
