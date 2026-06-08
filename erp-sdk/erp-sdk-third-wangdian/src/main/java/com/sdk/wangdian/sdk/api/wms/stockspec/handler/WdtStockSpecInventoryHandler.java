package com.sdk.wangdian.sdk.api.wms.stockspec.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
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
        pager.setPageSize(200);
        pager.setCalcTotal(true);
        try {
            StockSpecAPI stockSpecAPI = wangDianClientService.get(StockSpecAPI.class);
            log.info("旺店通实体仓可用库存查询：warehouseNo={}，specNo={}", warehouseNo, specNo);
            AvailableStockQueryResponse response = stockSpecAPI.search(request, pager);
            if (response == null) {
                return MathUtil.ZERO;
            }
            List<AvailableStockQueryResponse.StockDto> stockDtoList = response.getStockDtoList();
            if (stockDtoList == null) {
                stockDtoList = Collections.emptyList();
            }
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
            log.info("旺店通实体仓可用库存查询结果：warehouseNo={}，specNo={}，qty={}", warehouseNo, specNo, totalQty);
            return totalQty;
        } catch (WdtErpException e) {
            throw new ServiceException("调用旺店通" + SEARCH_AVAILABLE_STOCK_URL + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
        }
    }
}
