package cn.wangdian.erp.sdk.api.setting;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.setting.dto.LogisticsQueryRequest;
import cn.wangdian.erp.sdk.api.setting.dto.LogisticsQueryResponse;
import cn.wangdian.erp.sdk.api.setting.dto.PurchaseProviderQueryRequest;
import cn.wangdian.erp.sdk.api.setting.dto.PurchaseProviderQueryResponse;
import cn.wangdian.erp.sdk.api.setting.dto.ShopQueryRequest;
import cn.wangdian.erp.sdk.api.setting.dto.ShopQueryResponse;
import cn.wangdian.erp.sdk.api.setting.dto.WarehouseQueryRequest;
import cn.wangdian.erp.sdk.api.setting.dto.WarehouseQueryResponse;
import cn.wangdian.erp.sdk.impl.Api;

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
