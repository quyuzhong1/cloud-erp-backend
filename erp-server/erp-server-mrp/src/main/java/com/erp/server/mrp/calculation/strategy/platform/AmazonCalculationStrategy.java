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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    protected List<HistoryInventoryEsEntity> getHistoryInventory(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay) {
        LocalDate startDate = calculationDate.minusDays(cleanDay);
        LocalDate endDate = calculationDate.minusDays(1);
        List<FbaHistoryInventoryEntity> list = fbaHistoryInventoryService.listByStartDateAndEndDate(startDate, endDate);
        Map<FbaHistoryInventoryGroupDTO, Set<String>> suggestionMap = suggestions.stream()
                .collect(Collectors.groupingBy(FbaHistoryInventoryGroupDTO::buildFbaHistoryInventoryGroup, Collectors.mapping(ReplenishmentSuggestionEntity::getId, Collectors.toSet())));

        Map<FbaHistoryInventoryGroupDTO, List<FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO>> fbaInventoryMap = getFbaInventoryMap(list);
        return getCaleHistoryInventory(fbaInventoryMap, suggestionMap);
    }

    @Override
    protected List<ReplenishmentResultDTO.SalesInfoAllDTO> getSalesInfoByOrderData(LocalDate calculationDate, Integer cleanDay) {
        return salesService.listAllAmzSalesBySob2c(calculationDate, cleanDay);
    }

    /**
     * 以sku和仓库id为维度，合并对应建议与fba历史库存
     * @param fbaInventoryMap fba历史库存
     * @param suggestionMap   建议
     */
    private List<HistoryInventoryEsEntity> getCaleHistoryInventory(Map<FbaHistoryInventoryGroupDTO, List<FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO>> fbaInventoryMap, Map<FbaHistoryInventoryGroupDTO, Set<String>> suggestionMap) {
        List<List<Map.Entry<FbaHistoryInventoryGroupDTO, List<FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO>>>> inventoryPartition = Lists.partition(new ArrayList<>(fbaInventoryMap.entrySet()), 1000);
        return inventoryPartition.stream()
                .map(inventoryMap -> CompletableFuture.supplyAsync(() -> {
                    List<HistoryInventoryEsEntity> inventoryList = new ArrayList<>();
                    for (Map.Entry<FbaHistoryInventoryGroupDTO, List<FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO>> entry : inventoryMap) {
                        Set<String> suggestionIds = suggestionMap.get(entry.getKey());
                        if (CollectionUtils.isEmpty(suggestionIds)) {
                            continue;
                        }
                        for (String id : suggestionIds) {
                            for (FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO dto : entry.getValue()) {
                                inventoryList.add(HistoryInventoryEsEntity.createHistoryInventory(id, dto.getBillDate(), dto.getQty()));
                            }
                        }
                    }
                    return  inventoryList;
                }, threadPoolTaskExecutor)).collect(Collectors.toList()).stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }


    /**
     * 分组合并Fba历史库存
     * @param list fba历史库存
     */
    private static Map<FbaHistoryInventoryGroupDTO, List<FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO>> getFbaInventoryMap(List<FbaHistoryInventoryEntity> list) {
        return list.stream()
                //分组
                .collect(Collectors.groupingBy(FbaHistoryInventoryGroupDTO::buildFbaHistoryInventoryGroup,
                        //映射结果
                        Collectors.mapping(FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO::buildFbaInventoryResult,
                                //合并相同billDate 的qty
                                Collectors.collectingAndThen(Collectors.toList(), e -> e.stream()
                                        .collect(Collectors.toMap(
                                                FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO::getBillDate,
                                                FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO::getQty,
                                                Integer::sum
                                        ))
                                        .entrySet().stream()
                                        .map(entry -> new FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO(entry.getKey(), entry.getValue()))
                                        .collect(Collectors.toList())))
                ));
    }

    @Override
    protected List<ReplenishmentResultDTO.SalesInfoAllDTO> getSalesInfoByOutStockData(LocalDate calculationDate, Integer cleanDay) {
        return salesService.listAllAmzSalesBySoOutStock(calculationDate, cleanDay);
    }


}
