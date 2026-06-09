package com.sdk.wangdian.sdk.api.wms.stockspec.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.WdtStockSpecInventoryService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockspec.StockSpecAPI;
import com.sdk.wangdian.sdk.api.wms.stockspec.dto.AvailableStockQueryRequest;
import com.sdk.wangdian.sdk.api.wms.stockspec.dto.AvailableStockQueryResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class WdtStockSpecInventoryHandler implements WdtStockSpecInventoryService {

    private static final String SEARCH_AVAILABLE_STOCK_URL = "wms.StockSpec.queryAvailableStock";
    private static final int PAGE_SIZE = 200;

    @Resource
    private WangDianClientService wangDianClientService;

    @Override
    public Integer queryAvailableStock(String warehouseNo, String specNo) {
        if (CharSequenceUtil.isBlank(warehouseNo) || CharSequenceUtil.isBlank(specNo)) {
            return MathUtil.ZERO;
        }
        AvailableStockQueryRequest request = new AvailableStockQueryRequest();
        request.setWarehouseNo(warehouseNo);
        request.setSpecNo(specNo);
        Pager pager = new Pager();
        pager.setPageNo(0);
        pager.setPageSize(PAGE_SIZE);
        pager.setCalcTotal(true);
        try {
            StockSpecAPI stockSpecAPI = wangDianClientService.get(StockSpecAPI.class);
            log.warn("旺店通实体仓可用库存查询：warehouseNo={}，specNo={}", warehouseNo, specNo);
            int totalQty = 0;
            boolean hasNext = true;
            while (hasNext) {
                AvailableStockQueryResponse response = stockSpecAPI.search(request, pager);
                if (response == null) {
                    break;
                }
                List<AvailableStockQueryResponse.StockDto> stockDtoList = response.getStockDtoList();
                if (stockDtoList == null) {
                    stockDtoList = Collections.emptyList();
                }
                if (stockDtoList.isEmpty()) {
                    break;
                }
                totalQty += sumAvailableQty(stockDtoList, warehouseNo, specNo);
                Integer totalCount = response.getTotal();
                if (totalCount == null || totalCount <= (pager.getPageNo() + 1) * PAGE_SIZE) {
                    hasNext = false;
                } else {
                    pager.setPageNo(pager.getPageNo() + 1);
                }
            }
            log.warn("旺店通实体仓可用库存查询结果：warehouseNo={}，specNo={}，qty={}", warehouseNo, specNo, totalQty);
            return totalQty;
        } catch (WdtErpException e) {
            log.error("调用旺店通{}接口报错，warehouseNo={}，specNo={}", SEARCH_AVAILABLE_STOCK_URL, warehouseNo, specNo, e);
            throw new ServiceException("调用旺店通" + SEARCH_AVAILABLE_STOCK_URL + "接口报错，错误原因：" + CharSequenceUtil.blankToDefault(e.getMessage(), "未知错误"));
        }
    }

    private int sumAvailableQty(List<AvailableStockQueryResponse.StockDto> stockDtoList, String warehouseNo, String specNo) {
        int totalQty = 0;
        for (AvailableStockQueryResponse.StockDto stockDto : stockDtoList) {
            if (stockDto == null || stockDto.isDefect()) {
                continue;
            }
            if (!CharSequenceUtil.equals(warehouseNo, stockDto.getWarehouseNo())
                    || !CharSequenceUtil.equals(specNo, stockDto.getSpecNo())) {
                continue;
            }
            BigDecimal num = stockDto.getNum();
            totalQty += num == null ? 0 : num.intValue();
        }
        return totalQty;
    }
}
