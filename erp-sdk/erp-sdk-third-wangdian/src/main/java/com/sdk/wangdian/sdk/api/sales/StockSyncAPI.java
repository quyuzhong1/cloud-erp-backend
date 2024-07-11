package com.sdk.wangdian.sdk.api.sales;

import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.sales.dto.StockSyncCalcResponse;
import com.sdk.wangdian.sdk.api.sales.dto.StockSyncGetResponse;
import com.sdk.wangdian.sdk.api.sales.dto.StockSyncSuccessRequest;
import com.sdk.wangdian.sdk.impl.Api;

public interface StockSyncAPI
{
	@Api(value = "sales.StockSync.getSelfWaitSyncIdListOpen")
	StockSyncGetResponse get(int count, int position) throws WdtErpException;

	@Api(value = "sales.StockSync.calcStockWithAuth")
	StockSyncCalcResponse calc(Long apiGoodsId, boolean forceSync) throws WdtErpException ;

	@Api(value = "sales.StockSync.syncSuccess")
	void setSuccess(Long apiGoodsId, StockSyncSuccessRequest.SyncInfo syncInfo) throws WdtErpException;

	@Api(value = "sales.StockSync.syncFail")
	void setFail(Long apiGoodsId, StockSyncSuccessRequest.SyncInfo syncInfo) throws WdtErpException;

	/**
	 *
	 * @param apiGoodsId
	 * @param stockChangeCount 库存变化次数
	 * @throws WdtErpException
	 */
	@Api(value = "sales.StockSync.cancelSync")
	void cancel(Long apiGoodsId, int stockChangeCount) throws WdtErpException;
}
