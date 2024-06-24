package com.sdk.wangdian.sdk.api.goods;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsSuiteSearchRequest;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsSuiteSearchResponse;
import com.sdk.wangdian.sdk.api.goods.dto.GoodsSuiteUploadRequest;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.List;

public interface GoodsSuiteAPI
{
	@Api(value = "goods.Suite.upload")
	Integer upload(GoodsSuiteUploadRequest.Suite suite, List<GoodsSuiteUploadRequest.Detail> detailList) throws WdtErpException;

	@Api(value = "goods.Suite.search", paged = true)
    GoodsSuiteSearchResponse search(GoodsSuiteSearchRequest request, Pager pager) throws WdtErpException;
}
