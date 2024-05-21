package cn.wangdian.erp.sdk.api.aftersales;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.api.aftersales.dto.RefundSearchRequest;
import cn.wangdian.erp.sdk.api.aftersales.dto.RefundSearchResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface RefundAPI
{
	@Api(value = "aftersales.refund.Refund.search", paged = true)
	RefundSearchResponse search(RefundSearchRequest request, Pager pager);
}
