package com.sdk.wangdian.sdk.api.wms.stockin;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinResponse;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateTransferStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateTransferStockinResponse;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.ProcessStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.ProcessStockinResponse;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.OtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.OtherStockinResponse;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.RefundStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.RefundStockinResponse;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.StockinSearchRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.StockinSearchResponse;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.List;
import java.util.Map;

public interface StockinAPI {
    @Api(value = "wms.stockin.Refund.queryWithDetail", paged = true)
    RefundStockinResponse searchRefund(RefundStockinRequest request, Pager pager);

    @Api(value = "wms.stockin.Other.createOtherOrder")
    CreateOtherStockinResponse createOtherOrder(Map<String, Object> request) throws WdtErpException;
    
    @Api(value = "wms.stockin.Other.queryWithDetail", paged = true)
    OtherStockinResponse queryWithDetail(OtherStockinRequest request, Pager pager);

    @Api(value = "wms.stockin.Process.queryWithDetail", paged = true)
    ProcessStockinResponse searchProcess(ProcessStockinRequest request, Pager pager);
    
    @Api(value = "wms.stockin.Transfer.createOrder")
    CreateTransferStockinResponse createTransferOrder(CreateTransferStockinRequest.orderInfoDto orderInfo, List<CreateTransferStockinRequest.detailDto> detailList,boolean isCheck);

    @Api(value = "wms.stockin.Base.search", paged = true)
    StockinSearchResponse search(StockinSearchRequest request, Pager pager) throws WdtErpException;
}
