package com.erp.server.dmp.push.service.wdt;

import com.sdk.wangdian.sdk.api.wms.external.in.StockExternalInResponse;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.OtherStockinResponse;

/**
 * 
 * @date 2024-05-24
 * @author tanmujin
 */
public interface WdtOtherInStockService {

    /**
     * 请求旺店通创建其他入库单接口
     * @param stockinRequest 其他入库单请求参数
     * @return void
     * @date: 2024-05-26
     * @author: tanmujin
     */
    void executeConsumer(CreateOtherStockinRequest stockinRequest);

    /**
     * 自留转仓调用外部出库单创建
     */
    void executeSelfConsumer(CreateOtherStockinRequest request);

    OtherStockinResponse.DataInfoDto queryWithDetail(CreateOtherStockinRequest request);
    /**
     * 自留转仓调用外部入库单查询
     */
    StockExternalInResponse querySelfIn(CreateOtherStockinRequest request);
}
