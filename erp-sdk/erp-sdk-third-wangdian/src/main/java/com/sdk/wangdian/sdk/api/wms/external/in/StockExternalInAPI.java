package com.sdk.wangdian.sdk.api.wms.external.in;

import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.Map;

public interface StockExternalInAPI {

    @Api(value = "wms.outer.OuterIn.createOrder")
    CreateStockExternalInResponse createOrder(Map<String, Object> request) throws WdtErpException;
}
