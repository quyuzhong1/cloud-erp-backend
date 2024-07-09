package com.sdk.wangdian.sdk.api.wms.external.in;

import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.impl.Api;

public interface StockExternalInAPI {

    @Api(value = "wms.outer.OuterIn.createOrder")
    CreateStockExternalInResponse createOrder(Object order, Object orderDetail, Object isCheck) throws WdtErpException;
}
