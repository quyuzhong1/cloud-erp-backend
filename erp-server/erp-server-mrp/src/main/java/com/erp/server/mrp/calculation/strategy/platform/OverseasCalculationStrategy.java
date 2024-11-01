package com.erp.server.mrp.calculation.strategy.platform;

import com.erp.model.mrp.dto.FbaHistoryInventoryGroupDTO;
import com.erp.model.mrp.dto.OverseasHistoryInventoryGroupDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.PlatformMappingTypeEnum;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.server.mrp.calculation.service.SalesService;
import com.erp.server.mrp.es.entity.HistoryInventoryEsEntity;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import com.erp.server.mrp.service.OverseasHistoryInventoryService;
import com.google.common.collect.Lists;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
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
    protected List<HistoryInventoryEsEntity> getHistoryInventory(List<ReplenishmentSuggestionEntity> suggestions, LocalDate startDate, LocalDate endDate) {
        List<OverseasHistoryInventoryEntity> list = overseasHistoryInventoryService.listByStartDateAndEndDate(startDate, endDate);
        Map<OverseasHistoryInventoryGroupDTO, Set<String>> suggestionMap = suggestions.stream()
                .collect(Collectors.groupingBy(OverseasHistoryInventoryGroupDTO::buildOverseasHistoryInventoryGroup, Collectors.mapping(ReplenishmentSuggestionEntity::getId, Collectors.toSet())));
        Map<OverseasHistoryInventoryGroupDTO, Map<LocalDate, Integer>> fbaInventoryMap = getOverseasInventoryMap(list);
        return getCaleHistoryInventory(fbaInventoryMap, suggestionMap);
    }

    /**
     * 以sku和仓库id为维度，合并对应建议与fba历史库存
     *
     * @param fbaInventoryMap fba历史库存
     * @param suggestionMap   建议
     */
    private List<HistoryInventoryEsEntity> getCaleHistoryInventory(Map<OverseasHistoryInventoryGroupDTO, Map<LocalDate, Integer>> fbaInventoryMap, Map<OverseasHistoryInventoryGroupDTO, Set<String>> suggestionMap) {
        List<List<Map.Entry<OverseasHistoryInventoryGroupDTO, Map<LocalDate, Integer>>>> inventoryPartition = Lists.partition(new ArrayList<>(fbaInventoryMap.entrySet()), 1000);
        return inventoryPartition.stream()
                .map(inventoryMap -> CompletableFuture.supplyAsync(() -> {
                    List<HistoryInventoryEsEntity> inventoryList = new ArrayList<>();
                    for (Map.Entry<OverseasHistoryInventoryGroupDTO, Map<LocalDate, Integer>> entry : inventoryMap) {
                        Set<String> suggestionIds = suggestionMap.get(entry.getKey());
                        if (CollectionUtils.isEmpty(suggestionIds)) {
                            continue;
                        }
                        for (String id : suggestionIds) {
                            for (Map.Entry<LocalDate, Integer> map : entry.getValue().entrySet()) {
                                inventoryList.add(HistoryInventoryEsEntity.createHistoryInventory(id, map.getKey(), map.getValue()));
                            }
                        }
                    }
                    return inventoryList;
                }, threadPoolTaskExecutor)).collect(Collectors.toList()).stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 分组合并海外仓历史库存
     *
     * @param list 海外仓历史库存
     */
    private Map<OverseasHistoryInventoryGroupDTO, Map<LocalDate, Integer>> getOverseasInventoryMap(List<OverseasHistoryInventoryEntity> list) {

        Map<OverseasHistoryInventoryGroupDTO, Map<LocalDate, Integer>> result = new HashMap<>();
        for (OverseasHistoryInventoryEntity entity : list) {
            OverseasHistoryInventoryGroupDTO group = OverseasHistoryInventoryGroupDTO.buildOverseasHistoryInventoryGroup(entity);
            Map<LocalDate, Integer> inventoryResultMap = result.get(group);
            if (CollectionUtils.isEmpty(inventoryResultMap)) {
                Map<LocalDate, Integer> data = new HashMap<>();
                data.put(entity.getBillDate(), entity.getSellableQty().intValue());
                result.put(group, data);
            } else {
                Integer qty = inventoryResultMap.get(entity.getBillDate());
                if (ObjectUtils.isEmpty(qty)) {
                    inventoryResultMap.put(entity.getBillDate(), entity.getSellableQty().intValue());
                } else {
                    inventoryResultMap.put(entity.getBillDate(), entity.getSellableQty().intValue() + qty);
                }
                result.put(group, inventoryResultMap);
            }
        }
        return result;
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
