package com.erp.sdk.oms.amz.spapi.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 库存报告实体
 */
@Data
@NoArgsConstructor
public class ReportListingCsvEntity implements Serializable {

    private String itemName;

    private String itemDescription;

    private String listingId;

    private String sellerSku;

    private String price;

    private String quantity;

    private String openDate;

    private String imageUrl;

    private String itemIsMarketplace;

    private String productIdType;

    private String zshopShippingFee;

    private String itemNote;

    private String itemCondition;

    private String zshopCategory1;

    private String zshopBrowsePath;

    private String zshopStorefrontFeature;

    private String asin1;

    private String asin2;

    private String asin3;

    private String willShipInternationally;

    private String expeditedShipping;

    private String zshopBoldface;

    private String productId;

    private String bidForFeaturedPlacement;

    private String addDelete;

    private String pendingQuantity;

    private String fulfillmentChannel;

    private String merchantShippingGroup;

    private String status;
}
