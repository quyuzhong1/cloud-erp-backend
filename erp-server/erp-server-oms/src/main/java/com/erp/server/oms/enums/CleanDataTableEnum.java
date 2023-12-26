package com.erp.server.oms.enums;

import com.erp.oms.aliexpress.dto.PlatformAliExpressListingDTO;
import com.erp.oms.aliexpress.dto.PlatformAliExpressOrderDTO;
import com.sdk.oms.shopee.dto.PlatformShopeeListingDTO;
import com.sdk.oms.shopee.dto.PlatformShopeeOrderDTO;
import com.sdk.oms.shopify.dto.PlatformShopifyListingDTO;
import com.sdk.oms.shopify.dto.PlatformShopifyOrderDTO;
import com.sdk.oms.walmart.dto.PlatformWalmartListingDTO;
import com.sdk.oms.walmart.dto.PlatformWalmartOrderDTO;

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
    //shopify
    SHOPIFY_ORDER("Shopify", "third_system_Shopify_order","third_system", "order","Shopify订单数据", PlatformShopifyOrderDTO.class),
    SHOPIFY_PRODUCT("Shopify", "third_system_Shopify_product","third_system", "product","Shopify产品数据", PlatformShopifyListingDTO.class),
    //沃尔玛
    WALMART_ORDER("Walmart", "third_system_Walmart_order","third_system", "order","沃尔玛订单数据", PlatformWalmartOrderDTO.class),
    WALMART_PRODUCT("Walmart", "third_system_Walmart_product","third_system", "product","沃尔玛产品数据", PlatformWalmartListingDTO.class),
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
