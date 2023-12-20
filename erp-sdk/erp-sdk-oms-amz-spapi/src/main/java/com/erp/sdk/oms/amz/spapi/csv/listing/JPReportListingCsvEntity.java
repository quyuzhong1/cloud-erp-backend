package com.erp.sdk.oms.amz.spapi.csv.listing;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 库存报告实体
 * 日本
 */
@Data
@NoArgsConstructor
public class JPReportListingCsvEntity implements Serializable {


    @CsvBindByName(column = "商品名")
    private String itemName;

    @CsvBindByName(column = "コンディション説明")
    private String itemDescription;

    @CsvBindByName(column = "出品ID")
    private String listingId;

    @CsvBindByName(column = "出品者SKU")
    private String sellerSku;

    @CsvBindByName(column = "価格")
    private String price;

    @CsvBindByName(column = "数量")
    private String quantity;

    @CsvBindByName(column = "出品日")
    private String openDate;

    @CsvBindByName(column = "image-url")
    private String imageUrl;

    @CsvBindByName(column = "item-is-marketplace")
    private String itemIsMarketplace;

    @CsvBindByName(column = "商品IDタイプ")
    private String productIdType;

    @CsvBindByName(column = "zshop-shipping-fee")
    private String zshopShippingFee;

    @CsvBindByName(column = "item-note")
    private String itemNote;

    @CsvBindByName(column = "コンディション")
    private String itemCondition;

    @CsvBindByName(column = "zshop-category1")
    private String zshopCategory1;

    @CsvBindByName(column = "zshop-browse-path")
    private String zshopBrowsePath;

    @CsvBindByName(column = "zshop-storefront-feature")
    private String zshopStorefrontFeature;

    @CsvBindByName(column = "asin1")
    private String asin1;

    @CsvBindByName(column = "asin2")
    private String asin2;

    @CsvBindByName(column = "asin3")
    private String asin3;

    @CsvBindByName(column = "will-ship-internationally")
    private String willShipInternationally;

    @CsvBindByName(column = "国外へ配送可")
    private String expeditedShipping;

    @CsvBindByName(column = "zshop-boldface")
    private String zshopBoldface;

    @CsvBindByName(column = "product-id")
    private String productId;

    @CsvBindByName(column = "bid-for-featured-placement")
    private String bidForFeaturedPlacement;

    @CsvBindByName(column = "add-delete")
    private String addDelete;

    @CsvBindByName(column = "pending-quantity")
    private String pendingQuantity;

    @CsvBindByName(column = "fulfillment-channel")
    private String fulfillmentChannel;

    @CsvBindByName(column = "merchant-shipping-group")
    private String merchantShippingGroup;

}
