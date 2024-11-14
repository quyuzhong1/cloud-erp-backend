package com.erp.server.mrp.calculation.strategy.platform;


import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.server.mrp.es.entity.HistoryInventoryEsEntity;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;
import com.erp.server.mrp.es.service.HistoryInventoryEsService;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.erp.server.mrp.es.service.OutStockHistorySalesEsService;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractCalculationStrategy implements PlatformCalculationStrategy {

    @Resource
    private HistoryInventoryEsService historyInventoryEsService;
    @Resource
    private OrderHistorySalesEsService orderHistorySalesEsService;
    @Resource
    private OutStockHistorySalesEsService outStockHistorySalesEsService;

    @Override
    public void cleanHistoryInventory(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay) {
        LocalDate startDate = calculationDate.minusDays(cleanDay);
        LocalDate endDate = calculationDate.minusDays(1);
        List<String> suggestionIds = suggestions.stream().map(ReplenishmentSuggestionEntity::getId).collect(Collectors.toList());
        //删除原数据
        historyInventoryEsService.deleteBySuggestionIdsAndDate(suggestionIds, startDate, endDate);
        List<HistoryInventoryEsEntity> caleHistoryInventory = getHistoryInventory(suggestions, startDate, endDate);
        historyInventoryEsService.saveAll(caleHistoryInventory);
    }

    /***
     * 获取需要保存的数据
     * @param suggestions 建议
     * @param startDate   开始时间
     * @param endDate     结束时间
     */
    protected abstract List<HistoryInventoryEsEntity> getHistoryInventory(List<ReplenishmentSuggestionEntity> suggestions, LocalDate startDate, LocalDate endDate);

    @Override
    public void cleanHistorySalesByOrder(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay) {
        List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllList = getSalesInfoByOrderData(calculationDate, cleanDay);
        LocalDate startDate = calculationDate.minusDays(cleanDay);
        LocalDate endDate = calculationDate.minusDays(1);
        Map<String, String> suggestionMap = suggestions.stream()
                .collect(Collectors.toMap(k -> k.getSkuId() + "-" + k.getShopId(), ReplenishmentSuggestionEntity::getId, (o1, o2) -> o1));
        List<String> suggestionIds = suggestions.stream().map(ReplenishmentSuggestionEntity::getId).collect(Collectors.toList());
        //删除原数据
        orderHistorySalesEsService.deleteBySuggestionIdsAndDate(suggestionIds, startDate, endDate);
        //保存新数据
        List<OrderHistorySalesEsEntity> orderHistorySalesEsList = getOrderHistorySales(salesInfoAllList, suggestionMap);
        orderHistorySalesEsService.saveAll(orderHistorySalesEsList);
    }

    /**
     * 组装历史销量
     * @param salesInfoAllList  销量数据
     * @param suggestionMap     建议
     */
    private List<OrderHistorySalesEsEntity> getOrderHistorySales(List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllList, Map<String, String> suggestionMap) {
        List<OrderHistorySalesEsEntity> result = new ArrayList<>();
        for (ReplenishmentResultDTO.SalesInfoAllDTO dto : salesInfoAllList) {
            String suggestId = suggestionMap.get(dto.getSkuId() + "-" + dto.getShopId());
            if (ObjectUtils.isEmpty(suggestId)) {
                continue;
            }
            result.add(OrderHistorySalesEsEntity.createOrderHistorySales(suggestId, dto.getDate(), dto.getOriginalSalesQty(), dto.getSkuId(), dto.getShopId()));
        }
        return result;
    }

    /**
     * 根据平台获取销售订单销量数据
     * @param calculationDate 计算日
     * @param cleanDay        天数
     */
    protected abstract List<ReplenishmentResultDTO.SalesInfoAllDTO> getSalesInfoByOrderData(LocalDate calculationDate, Integer cleanDay);

    @Override
    public void cleanHistorySalesByOutStock(LocalDate calculationDate, List<ReplenishmentSuggestionEntity> suggestions, Integer cleanDay) {
        List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllList = getSalesInfoByOutStockData(calculationDate, cleanDay);
        LocalDate startDate = calculationDate.minusDays(cleanDay);
        LocalDate endDate = calculationDate.minusDays(1);
        Map<String, String> suggestionMap = suggestions.stream()
                .collect(Collectors.toMap(k -> k.getSkuId() + "-" + k.getShopId(), ReplenishmentSuggestionEntity::getId, (o1, o2) -> o1));
        List<String> suggestionIds = suggestions.stream().map(ReplenishmentSuggestionEntity::getId).collect(Collectors.toList());
        //删除原数据
        outStockHistorySalesEsService.deleteBySuggestionIdsAndDate(suggestionIds, startDate, endDate);
        //保存新数据
        List<OutStockHistorySalesEsEntity> outStockHistorySales = getOutStockHistorySales(salesInfoAllList, suggestionMap);
        outStockHistorySalesEsService.saveAll(outStockHistorySales);
    }

    /**
     * 组装历史销量
     * @param salesInfoAllList  销量数据
     * @param suggestionMap     建议
     */
    private List<OutStockHistorySalesEsEntity> getOutStockHistorySales(List<ReplenishmentResultDTO.SalesInfoAllDTO> salesInfoAllList, Map<String, String> suggestionMap) {
        List<OutStockHistorySalesEsEntity> result = new ArrayList<>();
        for (ReplenishmentResultDTO.SalesInfoAllDTO dto : salesInfoAllList) {
            String suggestId = suggestionMap.get(dto.getSkuId() + "-" + dto.getShopId());
            if (ObjectUtils.isEmpty(suggestId)) {
                continue;
            }
            result.add(OutStockHistorySalesEsEntity.createOutStockHistorySales(suggestId, dto.getDate(), dto.getOriginalSalesQty(), dto.getSkuId(), dto.getShopId()));
        }
        return result;
    }

    /**
     * 根据平台获取销售出库单销量数据
     * @param calculationDate 计算日
     * @param cleanDay        天数
     */
    protected abstract List<ReplenishmentResultDTO.SalesInfoAllDTO> getSalesInfoByOutStockData(LocalDate calculationDate, Integer cleanDay);
}
