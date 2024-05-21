package cn.wangdian.erp.sdk.api.wms.stockspec;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockspec.dto.AvailableStockQueryRequest;
import cn.wangdian.erp.sdk.api.wms.stockspec.dto.AvailableStockQueryResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface StockSpecAPI
{
	@Api(value = "wms.StockSpec.queryAvailableStock", paged = true)
	AvailableStockQueryResponse search(AvailableStockQueryRequest request, Pager pager) throws WdtErpException;
}
