package com.sdk.wangdian.sdk.api.finance;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.finance.dto.TransferSettleQueryRequest;
import com.sdk.wangdian.sdk.api.finance.dto.TransferSettleQueryResponse;

import com.sdk.wangdian.sdk.impl.Api;

public interface TransferAPI
{
	@Api(value = "finance.settle.Transfer.search", paged = true)
	TransferSettleQueryResponse search(TransferSettleQueryRequest request, Pager pager) throws WdtErpException;
}
