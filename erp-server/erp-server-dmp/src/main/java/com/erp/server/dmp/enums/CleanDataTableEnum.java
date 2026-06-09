package com.erp.server.dmp.enums;

import com.erp.oms.aliexpress.dto.PlatformAliExpressListingDTO;
import com.erp.oms.aliexpress.dto.PlatformAliExpressOrderDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonListingDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.sdk.oms.mercadolocal.dto.MercadoListingDTO;
import com.sdk.oms.mercadolocal.dto.MercadoOrderDTO;
import com.sdk.oms.shopee.dto.PlatformShopeeListingDTO;
import com.sdk.oms.shopee.dto.PlatformShopeeOrderDTO;
import com.sdk.oms.shopify.dto.PlatformShopifyListingDTO;
import com.sdk.oms.shopify.dto.PlatformShopifyOrderDTO;
import com.sdk.oms.tiktok.dto.TikTokListingDTO;
import com.sdk.oms.tiktok.dto.TikTokOrderDTO;
import com.sdk.oms.walmart.dto.PlatformWalmartListingDTO;
import com.sdk.oms.walmart.dto.PlatformWalmartOrderDTO;
import com.sdk.tms.track123.dto.PlatformTrack123TrackDTO;
import com.sdk.wms.goodcang.dto.response.*;
import com.sdk.wms.iml.dto.response.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/11 18:41
 */
public enum CleanDataTableEnum {
    //虾皮
    SHOPEE_ORDER("Shopee", "third_system_Shopee_order","third_system", "order","虾皮订单数据", PlatformShopeeOrderDTO.class),
    SHOPEE_PRODUCT("Shopee", "third_system_Shopee_product","third_system", "product","虾皮产品数据", PlatformShopeeListingDTO.class),
    //无忧
    ALI_EXPRESS_ORDER("AliExpress", "third_system_AliExpress_order","third_system", "order","无忧订单数据", PlatformAliExpressOrderDTO.class),
    ALI_EXPRESS_PRODUCT("AliExpress", "third_system_AliExpress_product","third_system", "product","无忧产品数据", PlatformAliExpressListingDTO.class),
    ALI_EXPRESS_OVERSEAS_MANAGED_PRODUCT("AliExpressOverseasManaged", "third_system_AliExpressOverseasManaged_product","third_system", "product","速卖通海外托管产品数据", PlatformAliExpressListingDTO.class),
    //shopify
    SHOPIFY_ORDER("Shopify", "third_system_Shopify_order","third_system", "order","Shopify订单数据", PlatformShopifyOrderDTO.class),
    SHOPIFY_PRODUCT("Shopify", "third_system_Shopify_product","third_system", "product","Shopify产品数据", PlatformShopifyListingDTO.class),
    //沃尔玛
    WALMART_ORDER("Walmart", "third_system_Walmart_order","third_system", "order","沃尔玛订单数据", PlatformWalmartOrderDTO.class),
    WALMART_PRODUCT("Walmart", "third_system_Walmart_product","third_system", "product","沃尔玛产品数据", PlatformWalmartListingDTO.class),
    // 亚马逊
    AMAZON_ORDER("Amazon", "third_system_Amazon_order","third_system", "order","Amazon订单数据", PlatformAmazonOrderDTO.class),
    AMAZON_PRODUCT("Amazon", "third_system_Amazon_product","third_system", "product","Amazon产品数据", PlatformAmazonListingDTO.class),
    AMAZON_FBA_SHIPMENT("Amazon", "third_system_Amazon_fba_shipment","third_system", "fba_shipment","AmazonFBA货件", PlatformAmazonFbaShipmentDTO.class),
    AMAZON_SO_OUT_STOCK("Amazon", "third_system_Amazon_so_out_stock","third_system", "so_out_stock","Amazon物流销售出库", PlatformAmazonFulfilledShipmentsDTO.class),

    //美客多
    MERCADO_ORDER("mercadolibre", "third_system_mercadolibre_order","third_system", "order","美客多订单数据", MercadoOrderDTO.class),
    MERCADO_PRODUCT("mercadolibre", "third_system_mercadolibre_product","third_system", "product","美客多产品数据", MercadoListingDTO.class),

    //TikTok
    TikTok_ORDER("TikTok", "third_system_TikTok_order","third_system", "order","TikTok订单数据", TikTokOrderDTO.class),
    TikTok_PRODUCT("TikTok", "third_system_TikTok_product","third_system", "product","TikTok产品数据", TikTokListingDTO.class),

    //track123
    TRACK123_GET_TRACK("TRACK123", "third_system_TRACK123_getTrack","third_system", "getTrack","TRACK123物流轨迹数据", PlatformTrack123TrackDTO.class),
    //IML
    IML_CITY_DICT("iml", "third_system_iml_city_dict","third_system", "city_dict","iml区域数据", ImlRegionResp.class),
    IML_INBOUND("iml", "third_system_iml_inbound","third_system", "inbound","iml入库数据", ImlReceiptResp.class),
    IML_INVENTORY("iml", "third_system_iml_inventory","third_system", "inventory","iml库存数据", ImlInventoryResp.class),
    IML_OUTBOUND("iml", "third_system_iml_outbound","third_system", "outbound","iml出库数据", ImlOutboundResp.class),
    IML_PRODUCT("iml", "third_system_iml_product","third_system", "product","iml产品数据", ImlProductResp.class),
    IML_WAREHOUSE("iml", "third_system_iml_warehouse","third_system", "warehouse","iml仓库数据", ImlWarehouseResp.class),

    //goodcang
    GOODCANG_CITY_DICT("goodcang", "third_system_goodcang_transfer","third_system", "transfer","goodcang中转仓数据", GoodCangTransferWarehouseResp.class),
    GOODCANG_INBOUND("goodcang", "third_system_goodcang_inbound","third_system", "inbound","goodcang入库数据", GoodCangReceiptBatchResp.class),
    GOODCANG_INVENTORY("goodcang", "third_system_goodcang_inventory","third_system", "inventory","goodcang库存数据", GoodCangInventoryResp.class),
    GOODCANG_OUTBOUND("goodcang", "third_system_goodcang_outbound","third_system", "outbound","goodcang出库数据", GoodCangOutboundResp.class),
    GOODCANG_PRODUCT("goodcang", "third_system_goodcang_product","third_system", "product","goodcang产品数据", GoodCangSkuResp.class),
    GOODCANG_WAREHOUSE("goodcang", "third_system_goodcang_warehouse","third_system", "warehouse","goodcang仓库数据", GoodCangWarehouseResp.class),
    ;

    private String platform;

    private String tableName;
    private String category;
    private String business;
    private String desc;
    private Class tClass;

    public String getPlatform() {
        return platform;
    }

    public String getTableName() {
        return tableName;
    }
    public String getCategory() {
        return category;
    }
    public String getBusiness() {
        return business;
    }

    public String getDesc() {
        return desc;
    }
    public Class getTClass() {
        return tClass;
    }
    CleanDataTableEnum(String platform, String tableName,String category,String business, String desc, Class tClass) {
        this.platform = platform;
        this.tableName = tableName;
        this.category = category;
        this.business = business;
        this.desc = desc;
        this.tClass = tClass;
    }

    public static CleanDataTableEnum getByName(String name) {
        CleanDataTableEnum[] values = values();
        for (CleanDataTableEnum value : values) {
            if (value.tableName.equals(name)) {
                return value;
            }
        }
        return null;
    }

    public static List<CleanDataTableEnum> getByPlatform(String platform) {
        CleanDataTableEnum[] values = values();
        List<CleanDataTableEnum> enums = new ArrayList<>();
        for (CleanDataTableEnum value : values) {
            if (value.platform.equals(platform)) {
                enums.add(value);
            }
        }
        return enums;
    }
}
