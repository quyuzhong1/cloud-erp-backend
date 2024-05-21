package cn.wangdian.erp.sdk.api.goods;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSuiteSearchRequest;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSuiteSearchResponse;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSuiteUploadRequest;
import cn.wangdian.erp.sdk.impl.Api;

import java.util.List;

public interface GoodsSuiteAPI
{
	@Api(value = "goods.Suite.upload")
	Integer upload(GoodsSuiteUploadRequest.Suite suite, List<GoodsSuiteUploadRequest.Detail> detailList) throws WdtErpException;

	@Api(value = "goods.Suite.search", paged = true)
	GoodsSuiteSearchResponse search(GoodsSuiteSearchRequest request, Pager pager) throws WdtErpException;
}
