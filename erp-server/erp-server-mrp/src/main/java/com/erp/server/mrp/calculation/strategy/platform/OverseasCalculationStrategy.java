package com.erp.server.mrp.calculation.strategy.platform;

import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.mrp.dto.FbaHistoryInventoryGroupDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.OverseasOrderTypeEnum;
import com.erp.model.mrp.enums.PlatformMappingTypeEnum;
import com.erp.model.mrp.enums.SalesQtyTypeEnum;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.server.mrp.calculation.service.SalesService;
import com.erp.server.mrp.es.entity.HistoryInventoryEsEntity;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;
import com.erp.server.mrp.es.service.HistoryInventoryEsService;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.erp.server.mrp.es.service.OutStockHistorySalesEsService;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import com.erp.server.mrp.service.OverseasHistoryInventoryService;
import com.google.common.collect.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class OverseasCalculationStrategy extends AbstractCalculationStrategy {

    @Resource
    private SalesService salesService;
    @Resource
    private CfgPlatformMappingService cfgPlatformMappingService;
    @Resource
    private OverseasHistoryInventoryService overseasHistoryInventoryService;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private LogisticsAuthFeign logisticsAuthFeign;


    @Override
    public CfgRulePlatformTypeEnum getPlatform() {
        return CfgRulePlatformTypeEnum.OVERSEAS;
    }


    @Override
    protected List<HistoryInventoryEsEntity> getHistoryInventory(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay) {
        return null;
    }


    @Override
    protected List<ReplenishmentResultDTO.SalesInfoAllDTO> getSalesInfoByOrderData(LocalDate calculationDate, Integer cleanDay) {
        List<String> channelIdList = logisticsAuthFeign.listAllChannelByOverseas();
        List<String> platforms = cfgPlatformMappingService.listEffectiveByPlatform(PlatformMappingTypeEnum.OVERSEAS_PLATFORM.getCode());
        return salesService.listAllOverseasSalesBySob2c(calculationDate, cleanDay, channelIdList, platforms);
    }


    @Override
    protected List<ReplenishmentResultDTO.SalesInfoAllDTO> getSalesInfoByOutStockData(LocalDate calculationDate, Integer cleanDay) {
        List<String> channelIdList = logisticsAuthFeign.listAllChannelByOverseas();
        List<String> platforms = cfgPlatformMappingService.listEffectiveByPlatform(PlatformMappingTypeEnum.OVERSEAS_PLATFORM.getCode());
        return salesService.listAllOverseasSalesBySoOutStock(calculationDate, cleanDay, channelIdList, platforms);
    }
}
