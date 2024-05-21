package cn.wangdian.erp.sdk.api.wms.stockother.in;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockother.in.dto.StockOtherInQueryRequest;
import cn.wangdian.erp.sdk.api.wms.stockother.in.dto.StockOtherInQueryResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface StockOtherInAPI
{
	@Api(value = "wms.stockother.InQuery.queryWithDetail", paged = true)
	StockOtherInQueryResponse queryWithDetail(StockOtherInQueryRequest request, Pager pager) throws WdtErpException;
}
