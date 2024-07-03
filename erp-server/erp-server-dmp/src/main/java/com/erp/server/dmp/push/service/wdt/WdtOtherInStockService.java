package com.erp.server.dmp.push.service.wdt;

import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;

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
}
