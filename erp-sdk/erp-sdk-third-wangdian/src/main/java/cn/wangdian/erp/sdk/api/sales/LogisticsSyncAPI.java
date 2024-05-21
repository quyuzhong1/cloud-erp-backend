package cn.wangdian.erp.sdk.api.sales;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.api.sales.dto.*;
import cn.wangdian.erp.sdk.impl.Api;

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
