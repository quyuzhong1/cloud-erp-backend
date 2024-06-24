package com.sdk.wangdian.sdk.api.sales;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.impl.Api;
import com.sdk.wangdian.sdk.api.sales.dto.*;

import java.util.List;

public interface LogisticsSyncAPI
{
	@Api(value = "sales.LogisticsSync.getSyncListExt", paged = true)
    LogisticsSyncGetResponse get(LogisticsSyncGetRequest request, Pager pager);

	@Api(value = "sales.LogisticsSync.getSpecialOids")
    LogisticsSyncSpecialOidsGetResponse getSpecialOids(int tradeId, byte platformId, String tid, String oids);

	@Api(value = "sales.LogisticsSync.update")
    LogisticsSyncUpdateResponse update(List<LogisticsSyncUpdateDto> request);
}
