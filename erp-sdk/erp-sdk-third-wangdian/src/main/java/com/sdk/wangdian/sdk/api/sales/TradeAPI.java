package com.sdk.wangdian.sdk.api.sales;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.sales.dto.TradeQueryRequest;
import com.sdk.wangdian.sdk.api.sales.dto.TradeQueryResponse;
import com.sdk.wangdian.sdk.api.sales.dto.TradeUploadRequest;
import com.sdk.wangdian.sdk.api.sales.dto.TradeUploadResponse;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.List;
import java.util.Map;

public interface TradeAPI
{
	@Api(value = "sales.TradeQuery.queryWithDetail", paged = true)
	TradeQueryResponse query(TradeQueryRequest request, Pager pager);


	@Api(value = "sales.TradeImport.upload")
	TradeUploadResponse upload(List<TradeUploadRequest> request);
}