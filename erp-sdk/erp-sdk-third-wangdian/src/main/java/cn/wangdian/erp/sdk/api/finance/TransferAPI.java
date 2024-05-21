package cn.wangdian.erp.sdk.api.finance;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.finance.dto.TransferSettleQueryRequest;
import cn.wangdian.erp.sdk.api.finance.dto.TransferSettleQueryResponse;

import cn.wangdian.erp.sdk.impl.Api;

public interface TransferAPI
{
	@Api(value = "finance.settle.Transfer.search", paged = true)
	TransferSettleQueryResponse search(TransferSettleQueryRequest request, Pager pager) throws WdtErpException;
}
