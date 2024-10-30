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

        Map<FbaHistoryInventoryGroupDTO, List<FbaHistoryInventoryGroupDTO.FbaInventoryResultDTO>> fbaInventoryMap = getFbaInventoryMap(list);
        List<HistoryInventoryEsEntity> caleHistoryInventory = getCaleHistoryInventory(fbaInventoryMap, suggestionMap);
        List<String> suggestionIds = suggestions.stream().map(ReplenishmentSuggestionEntity::getId).collect(Collectors.toList());
        List<List<String>> partition = Lists.partition(suggestionIds, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(suggestionList -> CompletableFuture.runAsync(() -> {
                    List<HistoryInventoryEsEntity> updateList = new ArrayList<>();
                    List<HistoryInventoryEsEntity> historyInventoryList = new ArrayList<>();
                    List<String> deleteIds = new ArrayList<>();
                    int page = 0;
                    Page<HistoryInventoryEsEntity> historyInventoryEsEntities;
                    do {
                        historyInventoryEsEntities = historyInventoryEsService.findByReplenishmentIdInAndDateBetween(suggestionList, startDate, endDate, PageRequest.of(page, 10000));
                        historyInventoryList.addAll(historyInventoryEsEntities.toList());
                        page++;
                    } while (!historyInventoryEsEntities.isLast());
                    processContent(historyInventoryList, caleHistoryInventory, updateList, deleteIds);
                    historyInventoryEsService.saveAll(updateList);
                    historyInventoryEsService.deleteByIdIn(deleteIds);
                }, threadPoolTaskExecutor)).toArray(CompletableFuture[]::new)).join();
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

    /**
     * 增量更新历史库存
     *
     * @param historyInventoryList 历史库存
     * @param newInventoryList     新的fba历史库存
     * @param updateList           编辑的数据
     * @param deleteIds            删除的id
     */
    private void processContent(List<HistoryInventoryEsEntity> historyInventoryList, List<HistoryInventoryEsEntity> newInventoryList, List<HistoryInventoryEsEntity> updateList, List<String> deleteIds) {
        //需要删除的数据
        List<String> deleteIdList = historyInventoryList.stream()
                .filter(v -> newInventoryList.stream().noneMatch(e -> e.getReplenishmentId().equals(v.getReplenishmentId()) && e.getDate().equals(v.getDate())))
                .map(HistoryInventoryEsEntity::getId)
                .collect(Collectors.toList());
        deleteIds.addAll(deleteIdList);
        //需要更新的数据
        for (HistoryInventoryEsEntity entity : newInventoryList) {
            HistoryInventoryEsEntity historyInventory = historyInventoryList.stream()
                    .filter(v -> v.getReplenishmentId().equals(entity.getReplenishmentId()))
                    .filter(v -> v.getDate().equals(entity.getDate()))
                    .findFirst()
                    .orElse(null);
            if (ObjectUtils.isEmpty(historyInventory)) {
                updateList.add(HistoryInventoryEsEntity.createHistoryInventory(entity.getReplenishmentId(), entity.getDate(), entity.getOriginalInventQty()));
                continue;
            }
            if (!entity.getOriginalInventQty().equals(historyInventory.getOriginalInventQty())) {
                historyInventory.setOriginalInventQty(historyInventory.getOriginalInventQty());
                updateList.add(historyInventory);
            }
        }
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
