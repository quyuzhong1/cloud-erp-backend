package cn.wangdian.erp.sdk.api.wms.stockother.out;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockother.out.dto.StockOtherOutQueryRequest;
import cn.wangdian.erp.sdk.api.wms.stockother.out.dto.StockOtherOutQueryResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface StockOtherOutAPI
{
	@Api(value = "wms.stockother.OutQuery.queryWithDetail", paged = true)
	StockOtherOutQueryResponse queryWithDetail(StockOtherOutQueryRequest request, Pager pager) throws WdtErpException;
}
