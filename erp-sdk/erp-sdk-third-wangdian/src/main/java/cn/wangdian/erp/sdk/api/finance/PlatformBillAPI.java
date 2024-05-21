package cn.wangdian.erp.sdk.api.finance;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.finance.dto.AlipayAccountCheckSearchRequest;
import cn.wangdian.erp.sdk.api.finance.dto.AlipayAccountCheckSearchResponse;
import cn.wangdian.erp.sdk.api.finance.dto.RawPaymentSearchRequest;
import cn.wangdian.erp.sdk.api.finance.dto.RawPaymentSearchResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface PlatformBillAPI
{
	@Api(value = "finance.AlipayAccountCheck.search", paged = true)
	AlipayAccountCheckSearchResponse searchAlipayAccountCheck(AlipayAccountCheckSearchRequest request, Pager pager) throws WdtErpException;

	@Api(value = "finance.RawPayment.search", paged = true)
	RawPaymentSearchResponse searchRawPayment(RawPaymentSearchRequest request, Pager pager) throws WdtErpException;
}
