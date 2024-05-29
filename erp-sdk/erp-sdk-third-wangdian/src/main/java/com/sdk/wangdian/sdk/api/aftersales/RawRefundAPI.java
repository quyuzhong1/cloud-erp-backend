package com.sdk.wangdian.sdk.api.aftersales;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.aftersales.dto.RawRefundSearchRequest;
import com.sdk.wangdian.sdk.api.aftersales.dto.RawRefundSearchResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface RawRefundAPI
{
	@Api(value = "aftersales.refund.RawRefund.search", paged = true)
	RawRefundSearchResponse search(RawRefundSearchRequest request, Pager pager) throws WdtErpException;
}
