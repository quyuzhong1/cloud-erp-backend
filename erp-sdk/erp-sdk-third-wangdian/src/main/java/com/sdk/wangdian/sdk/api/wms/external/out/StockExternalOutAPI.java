package com.sdk.wangdian.sdk.api.wms.external.out;

import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.Map;

public interface StockExternalOutAPI {

    @Api(value = "wms.outer.OuterOut.createOrder")
    CreateStockExternalOutResponse createOrder(Map<String, Object> request) throws WdtErpException;
}
