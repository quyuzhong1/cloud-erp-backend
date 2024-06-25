package com.sdk.wangdian.sdk.api.finance;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.finance.dto.AlipayAccountCheckSearchRequest;
import com.sdk.wangdian.sdk.api.finance.dto.AlipayAccountCheckSearchResponse;
import com.sdk.wangdian.sdk.api.finance.dto.RawPaymentSearchRequest;
import com.sdk.wangdian.sdk.api.finance.dto.RawPaymentSearchResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface PlatformBillAPI
{
	@Api(value = "finance.AlipayAccountCheck.search", paged = true)
	AlipayAccountCheckSearchResponse searchAlipayAccountCheck(AlipayAccountCheckSearchRequest request, Pager pager) throws WdtErpException;

	@Api(value = "finance.RawPayment.search", paged = true)
	RawPaymentSearchResponse searchRawPayment(RawPaymentSearchRequest request, Pager pager) throws WdtErpException;
}
