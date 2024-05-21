package cn.wangdian.erp.sdk.api.aftersales;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.aftersales.dto.RawRefundSearchRequest;
import cn.wangdian.erp.sdk.api.aftersales.dto.RawRefundSearchResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface RawRefundAPI
{
	@Api(value = "aftersales.refund.RawRefund.search", paged = true)
	RawRefundSearchResponse search(RawRefundSearchRequest request, Pager pager) throws WdtErpException;
}
