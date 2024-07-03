package com.sdk.wangdian.sdk.api.aftersales;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.aftersales.dto.RefundSearchRequest;
import com.sdk.wangdian.sdk.api.aftersales.dto.RefundSearchResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface RefundAPI
{
	@Api(value = "aftersales.refund.Refund.search", paged = true)
	RefundSearchResponse search(RefundSearchRequest request, Pager pager);
}
