package com.sdk.wangdian.sdk.api.wms;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.dto.*;
import com.sdk.wangdian.sdk.impl.Api;
import com.sdk.wangdian.sdk.api.wms.dto.*;

import java.util.List;
import java.util.Map;

public interface StockAPI {
    @Api(value = "wms.StockSpec.search", paged = true)
    StockSearchResponse search(StockSearchRequest request, Pager pager);

    @Api(value = "wms.StockSpec.search2", paged = true)
    StockSearch2Response search2(StockSearch2Request request, Pager pager);

    @Api("wms.StockPd.stockSyncByPd")
    Map<String, Object> createPdOrder(PdOrderCreateRequest.OrderDto order, List<PdOrderCreateRequest.DetailDto> detailDtoList);
    
    @Api("wms.stocktransfer.Edit.createOrder")
    TransferOrderCreateResponse createTransferOrder(TransferOrderCreateRequest.orderInfoDto orderInfo, List<TransferOrderCreateRequest.detailDto> detailList,boolean isCheck);

    @Api(value = "wms.stocktransfer.Manage.queryWithDetail", paged = true)
    TransferOrderSearchResponse searchStockTransfer(TransferOrderSearchRequest request, Pager pager) throws WdtErpException ;
}
