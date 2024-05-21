package cn.wangdian.erp.sdk.api.wms.stockin;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.dto.TransferOrderCreateRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateOtherStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateTransferStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateTransferStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.ProcessStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.ProcessStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.OtherStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.OtherStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.RefundStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.RefundStockinResponse;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.StockinSearchRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.StockinSearchResponse;
import cn.wangdian.erp.sdk.impl.Api;

import java.util.List;
import java.util.Map;

public interface StockinAPI {
    @Api(value = "wms.stockin.Refund.queryWithDetail", paged = true)
    RefundStockinResponse searchRefund(RefundStockinRequest request, Pager pager);

    @Api(value = "wms.stockin.Other.createOtherOrder")
    CreateOtherStockinResponse createOtherOrder(CreateOtherStockinRequest request) throws WdtErpException;
    
    @Api(value = "wms.stockin.Other.queryWithDetail", paged = true)
    OtherStockinResponse queryWithDetail(OtherStockinRequest request, Pager pager);

    @Api(value = "wms.stockin.Process.queryWithDetail", paged = true)
    ProcessStockinResponse searchProcess(ProcessStockinRequest request, Pager pager);
    
    @Api(value = "wms.stockin.Transfer.createOrder")
    CreateTransferStockinResponse createTransferOrder(CreateTransferStockinRequest.orderInfoDto orderInfo, List<CreateTransferStockinRequest.detailDto> detailList,boolean isCheck);

    @Api(value = "wms.stockin.Base.search", paged = true)
    StockinSearchResponse search(StockinSearchRequest request, Pager pager) throws WdtErpException;
}
