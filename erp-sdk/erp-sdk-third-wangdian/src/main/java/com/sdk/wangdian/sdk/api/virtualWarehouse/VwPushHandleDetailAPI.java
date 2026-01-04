package com.sdk.wangdian.sdk.api.virtualWarehouse;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailResponse;
import com.sdk.wangdian.sdk.impl.Api;

import java.util.Map;

public interface VwPushHandleDetailAPI
{
	@Api(value = "setting.strategy.VirtualWarehouse.create")
    VwPushHandelDetailResponse push(Map<String, Object> request, Object detailList) throws WdtErpException;
    /**
     * 查询虚拟仓库存信息
     */
    @Api(value = "setting.strategy.VirtualWarehouse.stockSearch", paged = true)
    String search(String request, Pager pager) throws WdtErpException;
}
