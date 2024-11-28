package com.erp.server.mrp.calculation.strategy.platform;

import com.erp.model.mrp.dto.FbaHistoryInventoryGroupDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.FbaHistoryInventoryEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.server.mrp.calculation.service.SalesService;
import com.erp.server.mrp.es.entity.HistoryInventoryEsEntity;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;
import com.erp.server.mrp.es.service.HistoryInventoryEsService;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.erp.server.mrp.es.service.OutStockHistorySalesEsService;
import com.erp.server.mrp.service.FbaHistoryInventoryService;
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
public class AmazonCalculationStrategy extends AbstractCalculationStrategy {

    @Resource
    private SalesService salesService;
    @Resource
    private FbaHistoryInventoryService fbaHistoryInventoryService;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Override
    public CfgRulePlatformTypeEnum getPlatform() {
        return CfgRulePlatformTypeEnum.AMAZON;
    }

    @Override
    protected List<HistoryInventoryEsEntity> getHistoryInventory(List<ReplenishmentSuggestionEntity> suggestions, LocalDate startDate, LocalDate endDate) {
        List<FbaHistoryInventoryEntity> list = fbaHistoryInventoryService.listByStartDateAndEndDate(startDate, endDate);
        Map<FbaHistoryInventoryGroupDTO, Set<String>> suggestionMap = suggestions.stream()
                .collect(Collectors.groupingBy(FbaHistoryInventoryGroupDTO::buildFbaHistoryInventoryGroup, Collectors.mapping(ReplenishmentSuggestionEntity::getId, Collectors.toSet())));
        Map<FbaHistoryInventoryGroupDTO, Map<LocalDate, Integer>> fbaInventoryMap = getFbaInventoryMap(list);
        return getCaleHistoryInventory(fbaInventoryMap, suggestionMap);
    }

    @Override
    protected List<ReplenishmentResultDTO.SalesInfoAllDTO> getSalesInfoByOrderData(LocalDate calculationDate, Integer cleanDay) {
        return salesService.listAllAmzSalesBySob2c(calculationDate, cleanDay);
    }

    /**
     * 以sku和仓库id为维度，合并对应建议与fba历史库存
     *
     * @param fbaInventoryMap fba历史库存
     * @param suggestionMap   建议
     */
    private List<HistoryInventoryEsEntity> getCaleHistoryInventory(Map<FbaHistoryInventoryGroupDTO, Map<LocalDate, Integer>> fbaInventoryMap,
                                                                   Map<FbaHistoryInventoryGroupDTO, Set<String>> suggestionMap) {
        List<List<Map.Entry<FbaHistoryInventoryGroupDTO, Map<LocalDate, Integer>>>> inventoryPartition = Lists.partition(new ArrayList<>(fbaInventoryMap.entrySet()), 1000);
        return inventoryPartition.stream()
                .map(inventoryMap -> CompletableFuture.supplyAsync(() -> {
                    List<HistoryInventoryEsEntity> inventoryList = new ArrayList<>();
                    for (Map.Entry<FbaHistoryInventoryGroupDTO, Map<LocalDate, Integer>> entry : inventoryMap) {
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
     * 分组合并Fba历史库存
     *
     * @param list fba历史库存
     */
    private static Map<FbaHistoryInventoryGroupDTO, Map<LocalDate, Integer>> getFbaInventoryMap(List<FbaHistoryInventoryEntity> list) {

        Map<FbaHistoryInventoryGroupDTO, Map<LocalDate, Integer>> result = new HashMap<>();
        for (FbaHistoryInventoryEntity entity : list) {
            FbaHistoryInventoryGroupDTO group = FbaHistoryInventoryGroupDTO.buildFbaHistoryInventoryGroup(entity);
            Map<LocalDate, Integer> inventoryResultMap = result.get(group);
            if (CollectionUtils.isEmpty(inventoryResultMap)) {
                Map<LocalDate, Integer> data = new HashMap<>();
                data.put(entity.getBillDate(), entity.getFulfillableQty());
                result.put(group, data);
            } else {
                Integer qty = inventoryResultMap.get(entity.getBillDate());
                if (ObjectUtils.isEmpty(qty)) {
                    inventoryResultMap.put(entity.getBillDate(), entity.getFulfillableQty());
                } else {
                    inventoryResultMap.put(entity.getBillDate(), entity.getFulfillableQty() + qty);
                }
                result.put(group, inventoryResultMap);
            }
        }
        return result;
    }

    @Override
    protected List<ReplenishmentResultDTO.SalesInfoAllDTO> getSalesInfoByOutStockData(LocalDate calculationDate, Integer cleanDay) {
        return salesService.listAllAmzSalesBySoOutStock(calculationDate, cleanDay);
    }


}
