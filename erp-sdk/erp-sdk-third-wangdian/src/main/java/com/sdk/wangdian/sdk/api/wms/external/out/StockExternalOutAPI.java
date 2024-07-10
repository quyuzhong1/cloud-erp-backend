package com.sdk.wangdian.sdk.api.wms.external.out;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.impl.Api;

public interface StockExternalOutAPI {

    @Api(value = "wms.outer.OuterOut.createOrder")
    CreateStockExternalOutResponse createOrder(Object order, Object orderDetail, Object isCheck) throws WdtErpException;

    @Api(value = "wms.outer.OuterOut.queryWithDetail", paged = true)
    StockExternalOutResponse queryWithDetail(StockExternalOutRequest order, Pager pager);
}
