package com.erp.server.mrp.es.service.impl;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.es.entity.HistoryInventoryEsEntity;
import com.erp.server.mrp.es.repository.HistoryInventoryEsRepository;
import com.erp.server.mrp.es.service.HistoryInventoryEsService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HistoryInventoryEsServiceImpl implements HistoryInventoryEsService {

    @Resource
    private HistoryInventoryEsRepository historyInventoryEsRepository;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public Page<HistoryInventoryEsEntity> findByReplenishmentIdInAndDateBetween(List<String> replenishmentIds, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return historyInventoryEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds, startDate, endDate, pageable);
    }

    @Override
    public void saveAll(List<HistoryInventoryEsEntity> historyInventoryList) {
        if (CollectionUtils.isEmpty(historyInventoryList)) {
            return;
        }
        List<List<HistoryInventoryEsEntity>> partition = Lists.partition(historyInventoryList, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(suggestionList -> CompletableFuture.runAsync(() -> historyInventoryEsRepository.saveAll(suggestionList), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public List<HistoryInventoryEsEntity> findByReplenishmentIdAndDateBetween(String replenishmentId, LocalDate startDate, LocalDate endDate) {
        return historyInventoryEsRepository.findByReplenishmentIdAndDateBetween(replenishmentId, startDate, endDate);
    }

    @Override
    public Map<LocalDate, Integer> findByReplenishmentIdAndDateBetweenMap(String replenishmentId, LocalDate startDate, LocalDate endDate) {
        List<HistoryInventoryEsEntity> historyInventoryEsEntities = findByReplenishmentIdAndDateBetween(replenishmentId, startDate, endDate);
        return historyInventoryEsEntities.stream()
                .collect(Collectors.toMap(HistoryInventoryEsEntity::getDate, HistoryInventoryEsEntity::getOriginalInventQty, Integer::sum));
    }


    /**
     * 查询历史数据
     * @param replenishmentIds 建议id
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    private List<HistoryInventoryEsEntity> getHistoryInventory(List<String> replenishmentIds, LocalDate startDate, LocalDate endDate) {
        List<HistoryInventoryEsEntity> historyInventoryList = new ArrayList<>();
        Page<HistoryInventoryEsEntity> historyInventoryListPage;
        int page = 0;
        do {
            historyInventoryListPage = findByReplenishmentIdInAndDateBetween(replenishmentIds,startDate, endDate , PageRequest.of(page, 10000));
            historyInventoryList.addAll(historyInventoryListPage.toList());
            page++;
        } while (!historyInventoryListPage.isLast());
        return historyInventoryList;
    }

    @Override
    public List<ReplenishmentResultDTO.InventoryHistoryDTO> listByReplenishmentIdsAndDate(List<String> suggestionIds, LocalDate startDate, LocalDate endDate) {
        List<List<String>> partition = Lists.partition(suggestionIds, 1000);
        return partition.stream()
                .map(suggestionIdList -> CompletableFuture.supplyAsync(() -> {
                    List<HistoryInventoryEsEntity> inventory = getHistoryInventory(suggestionIdList, startDate, endDate);
                    return inventory.stream()
                            .map(v -> ReplenishmentResultDTO.InventoryHistoryDTO.buildInventoryHistory(v.getReplenishmentId(), v.getDate(), v.getOriginalInventQty()))
                            .collect(Collectors.toList());
                }, threadPoolTaskExecutor))
                .collect(Collectors.toList()).stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

    }

    @Override
    public void deleteBySuggestionIdsAndDate(List<String> suggestionIds, LocalDate startDate, LocalDate endDate) {
        List<List<String>> partition = Lists.partition(suggestionIds, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(suggestionList -> CompletableFuture.runAsync(() -> historyInventoryEsRepository
                        .deleteByReplenishmentIdInAndDateBetween(suggestionList, startDate, endDate), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }
}
