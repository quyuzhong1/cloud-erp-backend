package com.erp.server.mrp.calculation.strategy.platform;

import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.OverseasOrderTypeEnum;
import com.erp.model.mrp.enums.PlatformMappingTypeEnum;
import com.erp.model.mrp.enums.SalesQtyTypeEnum;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.server.mrp.calculation.service.SalesService;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class OverseasCalculationStrategy implements PlatformCalculationStrategy {

    @Resource
    private SalesService salesService;
    @Resource
    private CfgPlatformMappingService cfgPlatformMappingService;

    @Resource
    private LogisticsAuthFeign logisticsAuthFeign;

    @Override
    public List<ReplenishmentResultDTO.SalesInfoAllDTO> calculationHistorySale(String calcDate, CfgRuleSalesQtyEntity cfgRuleSalesQty) {
        List<String> channelIdList = logisticsAuthFeign.listAllChannelByOverseas();

        List<String> platforms = cfgPlatformMappingService.listEffectiveByPlatform(PlatformMappingTypeEnum.OVERSEAS_PLATFORM.getCode());
        // 以销售订单订单创建时间计算销量
        if (SalesQtyTypeEnum.BY_CREATE_TIME.getCode().equals(cfgRuleSalesQty.getSalesQtyType())) {
            return salesService.listAllOverseasSalesBySob2c(calcDate, cfgRuleSalesQty.getOrderType(), channelIdList, platforms);
        } else {
            // 以销售出库单出库时间计算销量
            return salesService.listAllOverseasSalesBySoOutStock(calcDate, cfgRuleSalesQty.getOrderType(), channelIdList, platforms);
        }
    }


    @Override
    public CfgRulePlatformTypeEnum getPlatform() {
        return CfgRulePlatformTypeEnum.OVERSEAS;
    }
}
