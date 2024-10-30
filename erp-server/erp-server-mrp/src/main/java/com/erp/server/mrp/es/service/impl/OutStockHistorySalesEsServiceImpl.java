package com.erp.server.mrp.es.service.impl;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.FbaOrderTypeEnum;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;
import com.erp.server.mrp.es.repository.OutStockHistorySalesEsRepository;
import com.erp.server.mrp.es.service.OutStockHistorySalesEsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OutStockHistorySalesEsServiceImpl implements OutStockHistorySalesEsService {

    @Resource
    private OutStockHistorySalesEsRepository outStockHistorySalesEsRepository;

    @Override
    public Page<OutStockHistorySalesEsEntity> findByReplenishmentIdInAndDateBetween(List<String> replenishmentIds, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return outStockHistorySalesEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds, startDate, endDate, pageable);
    }

    @Override
    public void saveAll(List<OutStockHistorySalesEsEntity> historyInventoryList) {
        if (ObjectUtils.isEmpty(historyInventoryList)) {
            return;
        }
        outStockHistorySalesEsRepository.saveAll(historyInventoryList);
    }

    @Override
    public Map<String, Integer> countQtyByReplenishmentIdsAndDate(List<String> replenishmentIds, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OutStockHistorySalesEsEntity> outStockHistorySalesList = getOutStockHistorySales(replenishmentIds, orderType, startDate, endDate);
        return outStockHistorySalesList.stream()
                .collect(Collectors.toMap(OutStockHistorySalesEsEntity::getReplenishmentId, OutStockHistorySalesEsEntity::getOriginalSalesQty, Integer::sum));
    }

    /**
     * 查询历史数据
     * @param replenishmentIds 建议id
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    private List<OutStockHistorySalesEsEntity> getOutStockHistorySales(List<String> replenishmentIds, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OutStockHistorySalesEsEntity> outStockHistorySalesList = new ArrayList<>();
        Page<OutStockHistorySalesEsEntity> outStockHistorySalesPage;
        int page = 0;
        do {
            if (ObjectUtils.isEmpty(orderType) || FbaOrderTypeEnum.ALL.getCode().equals(orderType)) {
                outStockHistorySalesPage = outStockHistorySalesEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds, startDate, endDate, PageRequest.of(page, 10000));
            } else {
                outStockHistorySalesPage = outStockHistorySalesEsRepository.findByReplenishmentIdInAndOrderTypeAndDateBetween(replenishmentIds, orderType, startDate, endDate, PageRequest.of(page, 10000));
            }
            outStockHistorySalesList.addAll(outStockHistorySalesPage.toList());
            page++;
        } while (!outStockHistorySalesPage.isLast());
        return outStockHistorySalesList;
    }

    @Override
    public void deleteByIdIn(List<String> deleteIds) {
        if (ObjectUtils.isEmpty(deleteIds)) {
            return;
        }
        outStockHistorySalesEsRepository.deleteByIdIn(deleteIds);
    }

    @Override
    public List<ReplenishmentResultDTO.SalesHistoryDTO> listByReplenishmentIdsAndDate(List<String> suggestionIdList, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OutStockHistorySalesEsEntity> historySales = getOutStockHistorySales(suggestionIdList, orderType, startDate, endDate);
        return historySales.stream()
                .map(v -> ReplenishmentResultDTO.SalesHistoryDTO.buildSalesHistory(v.getReplenishmentId(), v.getDate(), v.getOriginalSalesQty()))
                .collect(Collectors.toList());
    }
}
