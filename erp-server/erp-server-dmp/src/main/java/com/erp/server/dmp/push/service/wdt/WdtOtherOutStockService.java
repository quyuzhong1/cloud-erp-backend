package com.erp.server.dmp.push.service.wdt;

import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;

/**
 * 
 * @date 2024-05-24
 * @author tanmujin
 */
public interface WdtOtherOutStockService {

    /**
     *
     * @param stockoutRequest 请求旺店通创建其他出库单接口
     * @return void
     * @date: 2024-05-26
     * @author: tanmujin
     */
    void executeConsumer(CreateOtherStockoutRequest stockoutRequest);
}
