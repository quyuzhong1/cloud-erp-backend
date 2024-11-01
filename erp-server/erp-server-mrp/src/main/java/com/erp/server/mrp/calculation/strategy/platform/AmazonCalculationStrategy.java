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
public class AmazonCalculationStrategy implements PlatformCalculationStrategy {

    @Resource
    private SalesService salesService;
    @Resource
    private FbaHistoryInventoryService fbaHistoryInventoryService;

    @Resource
    private HistoryInventoryEsService historyInventoryEsService;
    @Resource
    private OrderHistorySalesEsService orderHistorySalesEsService;
    @Resource
    private OutStockHistorySalesEsService outStockHistorySalesEsService;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public CfgRulePlatformTypeEnum getPlatform() {
        return CfgRulePlatformTypeEnum.AMAZON;
    }

    @Override
    public void cleanHistoryInventory(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay) {

        LocalDate startDate = calculationDate.minusDays(cleanDay);
        LocalDate endDate = calculationDate.minusDays(1);
        List<FbaHistoryInventoryEntity> list = fbaHistoryInventoryService.listByStartDateAndEndDate(startDate, endDate);
        Map<FbaHistoryInventoryGroupDTO, Set<String>> suggestionMap = suggestions.stream()
                .collect(Collectors.groupingBy(FbaHistoryInventoryGroupDTO::buildFbaHistoryInventoryGroup, Collectors.mapping(ReplenishmentSuggestionEntity::getId, Collectors.toSet())));

        Map<FbaHistoryInventoryGroupDTO, Map<LocalDate, Integer>> fbaInventoryMap = getFbaInventoryMap(list);
        List<String> suggestionIds = suggestions.stream().map(ReplenishmentSuggestionEntity::getId).collect(Collectors.toList());
        //删除原数据
        historyInventoryEsService.deleteBySuggestionIdsAndDate(suggestionIds, startDate, endDate);
        //保存新数据
        List<HistoryInventoryEsEntity> caleHistoryInventory = getCaleHistoryInventory(fbaInventoryMap, suggestionMap);
        historyInventoryEsService.saveAll(caleHistoryInventory);
    }

    /**
     * 以sku和仓库id为维度，合并对应建议与fba历史库存
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
    public void cleanHistorySalesByOrder(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay) {
        List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllList = salesService.listAllAmzSalesBySob2c(calculationDate, cleanDay);
        LocalDate startDate = calculationDate.minusDays(cleanDay);
        LocalDate endDate = calculationDate.minusDays(1);
        Map<String, String> suggestionMap = suggestions.stream()
                .collect(Collectors.toMap(k -> k.getSkuId() + "-" + k.getShopId(), ReplenishmentSuggestionEntity::getId, (o1, o2) -> o1));
        List<List<ReplenishmentResultDTO.SalesInfoAllDTO>> partition = Lists.partition(salesInfoAllList, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(salesInfoList -> CompletableFuture.runAsync(() -> {
                    Map<ReplenishmentResultDTO.SalesInfoAllDTO, String> salesInfoMap = salesInfoList.stream()
                            .filter(v -> suggestionMap.containsKey(v.getSkuId() + "-" + v.getShopId()))
                            .collect(Collectors.toMap(k -> k, v -> suggestionMap.get(v.getSkuId() + "-" + v.getShopId())));
                    List<OrderHistorySalesEsEntity> orderHistorySalesList = new ArrayList<>();
                    List<OrderHistorySalesEsEntity> updateList = new ArrayList<>();
                    List<String> deleteIds = new ArrayList<>();
                    int page = 0;
                    Page<OrderHistorySalesEsEntity> orderHistorySalesPage;
                    do {
                        orderHistorySalesPage = orderHistorySalesEsService.findByReplenishmentIdInAndDateBetween(new ArrayList<>(salesInfoMap.values()), startDate, endDate, PageRequest.of(page, 10000));
                        orderHistorySalesList.addAll(orderHistorySalesPage.toList());
                        page++;
                    } while (!orderHistorySalesPage.isLast());
                    processOrderContent(orderHistorySalesList, salesInfoMap, updateList, deleteIds);
                    orderHistorySalesEsService.saveAll(updateList);
                    orderHistorySalesEsService.deleteByIdIn(deleteIds);
                }, threadPoolTaskExecutor)).toArray(CompletableFuture[]::new)).join();
    }

    /**
     * 增量更新历史销量
     *
     * @param orderHistorySalesList 历史销量
     * @param salesInfoMap          销量
     * @param updateList            需要编辑的数据
     * @param deleteIds             需要删除的数据
     */
    private void processOrderContent(List<OrderHistorySalesEsEntity> orderHistorySalesList, Map<ReplenishmentResultDTO.SalesInfoAllDTO, String> salesInfoMap, List<OrderHistorySalesEsEntity> updateList, List<String> deleteIds) {
        List<String> deleteIdList = orderHistorySalesList.stream()
                .filter(v -> salesInfoMap.entrySet().stream()
                        .noneMatch(e -> e.getKey().getDate().equals(v.getDate()) && e.getKey().getOrderType().equals(v.getOrderType()) && e.getValue().equals(v.getReplenishmentId())))
                .map(OrderHistorySalesEsEntity::getId)
                .collect(Collectors.toList());
        deleteIds.addAll(deleteIdList);
        Map<String, OrderHistorySalesEsEntity> orderSalesMap = orderHistorySalesList.stream()
                .collect(Collectors.toMap(k -> k.getReplenishmentId() + "-" + k.getDate() + "-" + k.getOrderType(), v -> v, (o1, o2) -> o1));
        for (Map.Entry<ReplenishmentResultDTO.SalesInfoAllDTO, String> entry : salesInfoMap.entrySet()) {
            OrderHistorySalesEsEntity orderHistorySalesEsEntity = orderSalesMap.get(entry.getValue() + "-" + entry.getKey().getDate() + "-" + entry.getKey().getOrderType());
            if (ObjectUtils.isEmpty(orderHistorySalesEsEntity)) {
                updateList.add(OrderHistorySalesEsEntity.createOrderHistorySales(entry.getValue(), entry.getKey().getDate(), entry.getKey().getOriginalSalesQty()));
            } else if (!Objects.equals(orderHistorySalesEsEntity.getOriginalSalesQty(), entry.getKey().getOriginalSalesQty())) {
                updateList.add(OrderHistorySalesEsEntity.updateOrderHistorySales(orderHistorySalesEsEntity.getId(), entry.getKey().getOriginalSalesQty()));
            }
        }
    }

    @Override
    public void cleanHistorySalesByOutStock(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay) {
        LocalDate startDate = calculationDate.minusDays(cleanDay);
        LocalDate endDate = calculationDate.minusDays(1);
        Map<String, String> suggestionMap = suggestions.stream()
                .collect(Collectors.toMap(k -> k.getSkuId() + "-" + k.getShopId(), ReplenishmentSuggestionEntity::getId, (o1, o2) -> o1));
        List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllList = salesService.listAllAmzSalesBySoOutStock(calculationDate, cleanDay);
        List<List<ReplenishmentResultDTO.SalesInfoAllDTO>> partition = Lists.partition(salesInfoAllList, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(salesInfoList -> CompletableFuture.runAsync(() -> {
                    Map<ReplenishmentResultDTO.SalesInfoAllDTO, String> salesInfoMap = salesInfoList.stream()
                            .filter(v -> suggestionMap.containsKey(v.getSkuId() + "-" + v.getShopId()))
                            .collect(Collectors.toMap(k -> k, v -> suggestionMap.get(v.getSkuId() + "-" + v.getShopId())));
                    List<OutStockHistorySalesEsEntity> outStockHistorySalesList = new ArrayList<>();
                    List<OutStockHistorySalesEsEntity> updateList = new ArrayList<>();
                    List<String> deleteIds = new ArrayList<>();
                    int page = 0;
                    Page<OutStockHistorySalesEsEntity> outStockHistorySalesPage;
                    do {
                        outStockHistorySalesPage = outStockHistorySalesEsService.findByReplenishmentIdInAndDateBetween(new ArrayList<>(salesInfoMap.values()), startDate, endDate, PageRequest.of(page, 10000));
                        outStockHistorySalesList.addAll(outStockHistorySalesPage.toList());
                        page++;
                    } while (!outStockHistorySalesPage.isLast());
                    processOutStockContent(outStockHistorySalesList, salesInfoMap, updateList, deleteIds);
                    outStockHistorySalesEsService.saveAll(updateList);
                    outStockHistorySalesEsService.deleteByIdIn(deleteIds);
                }, threadPoolTaskExecutor)).toArray(CompletableFuture[]::new)).join();
    }


    /**
     * 增量更新历史销量
     *
     * @param outStockHistorySalesList 历史销量
     * @param salesInfoMap             销量
     * @param updateList               需要编辑的数据
     * @param deleteIds                需要删除的数据
     */
    private void processOutStockContent(List<OutStockHistorySalesEsEntity> outStockHistorySalesList, Map<ReplenishmentResultDTO.SalesInfoAllDTO, String> salesInfoMap, List<OutStockHistorySalesEsEntity> updateList, List<String> deleteIds) {
        List<String> deleteIdList = outStockHistorySalesList.stream()
                .filter(v -> salesInfoMap.entrySet().stream()
                        .noneMatch(e -> e.getKey().getDate().equals(v.getDate()) && e.getKey().getOrderType().equals(v.getOrderType()) && e.getValue().equals(v.getReplenishmentId())))
                .map(OutStockHistorySalesEsEntity::getId)
                .collect(Collectors.toList());
        deleteIds.addAll(deleteIdList);
        Map<String, OutStockHistorySalesEsEntity> orderSalesMap = outStockHistorySalesList.stream()
                .collect(Collectors.toMap(k -> k.getReplenishmentId() + "-" + k.getDate() + "-" + k.getOrderType(), v -> v, (o1, o2) -> o1));
        for (Map.Entry<ReplenishmentResultDTO.SalesInfoAllDTO, String> entry : salesInfoMap.entrySet()) {
            OutStockHistorySalesEsEntity outStockHistorySalesEsEntity = orderSalesMap.get(entry.getValue() + "-" + entry.getKey().getDate() + "-" + entry.getKey().getOrderType());
            if (ObjectUtils.isEmpty(outStockHistorySalesEsEntity)) {
                updateList.add(OutStockHistorySalesEsEntity.createOutStockHistorySales(entry.getValue(), entry.getKey().getDate(), entry.getKey().getOriginalSalesQty()));
            } else if (!Objects.equals(outStockHistorySalesEsEntity.getOriginalSalesQty(), entry.getKey().getOriginalSalesQty())) {
                updateList.add(OutStockHistorySalesEsEntity.updateHistoryInventory(outStockHistorySalesEsEntity.getId(), entry.getKey().getOriginalSalesQty()));
            }
        }

    }
}
