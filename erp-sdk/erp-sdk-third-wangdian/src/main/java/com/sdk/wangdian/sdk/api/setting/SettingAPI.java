package com.sdk.wangdian.sdk.api.setting;

import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.setting.dto.LogisticsQueryRequest;
import com.sdk.wangdian.sdk.api.setting.dto.LogisticsQueryResponse;
import com.sdk.wangdian.sdk.api.setting.dto.PurchaseProviderQueryRequest;
import com.sdk.wangdian.sdk.api.setting.dto.PurchaseProviderQueryResponse;
import com.sdk.wangdian.sdk.api.setting.dto.ShopQueryRequest;
import com.sdk.wangdian.sdk.api.setting.dto.ShopQueryResponse;
import com.sdk.wangdian.sdk.api.setting.dto.WarehouseQueryRequest;
import com.sdk.wangdian.sdk.api.setting.dto.WarehouseQueryResponse;
import com.sdk.wangdian.sdk.impl.Api;

public interface SettingAPI
{

	@Api(value = "setting.Warehouse.queryWarehouse", paged = true)
	WarehouseQueryResponse queryWarehouse(WarehouseQueryRequest request, Pager pager);

	@Api(value = "setting.PurchaseProvider.queryDetail" , paged = true)
	PurchaseProviderQueryResponse queryPurchaseProvider(PurchaseProviderQueryRequest request, Pager pager) throws WdtErpException;

	@Api(value = "setting.Shop.queryShop", paged = true)
	ShopQueryResponse search(ShopQueryRequest request, Pager pager) throws WdtErpException ;

	@Api(value = "setting.Logistics.queryLogistics", paged = true)
	LogisticsQueryResponse queryLogistics(LogisticsQueryRequest request, Pager pager) throws WdtErpException;
}
