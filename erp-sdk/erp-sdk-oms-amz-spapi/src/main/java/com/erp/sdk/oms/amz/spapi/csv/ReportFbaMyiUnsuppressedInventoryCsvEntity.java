package com.erp.sdk.oms.amz.spapi.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;


/**
 * 亚马逊物流管理库存-未归档 实体
 */
@Data
@NoArgsConstructor
public class ReportFbaMyiUnsuppressedInventoryCsvEntity {
    @CsvBindByName(column = "sku")
    private String sku;

    @CsvBindByName(column = "fnsku")
    private String fnsku;

    @CsvBindByName(column = "asin")
    private String asin;

    @CsvBindByName(column = "product-name")
    private String productName;

    @CsvBindByName(column = "condition")
    private String condition;

    @CsvBindByName(column = "your-price")
    private String yourPrice;

    @CsvBindByName(column = "mfn-listing-exists")
    private String mfnListingExists;

    @CsvBindByName(column = "mfn-fulfillable-quantity")
    private String mfnFulfillableQuantity;

    @CsvBindByName(column = "afn-listing-exists")
    private String afnListingExists;

    @CsvBindByName(column = "afn-warehouse-quantity")
    private String afnWarehouseQuantity;

    @CsvBindByName(column = "afn-fulfillable-quantity")
    private String afnFulfillableQuantity;

    @CsvBindByName(column = "afn-unsellable-quantity")
    private String afnUnsellableQuantity;

    @CsvBindByName(column = "afn-reserved-quantity")
    private String afnReservedQuantity;

    @CsvBindByName(column = "afn-total-quantity")
    private String afnTotalQuantity;

    @CsvBindByName(column = "per-unit-volume")
    private String perUnitVolume;

    @CsvBindByName(column = "afn-inbound-working-quantity")
    private String afnInboundWorkingQuantity;

    @CsvBindByName(column = "afn-inbound-shipped-quantity")
    private String afnInboundShippedQuantity;

    @CsvBindByName(column = "afn-inbound-receiving-quantity")
    private String afnInboundReceivingQuantity;

    @CsvBindByName(column = "afn-researching-quantity")
    private String afnResearchingQuantity;

    @CsvBindByName(column = "afn-reserved-future-supply")
    private String afnReservedFutureSupply;

    @CsvBindByName(column = "afn-future-supply-buyable")
    private String afnFutureSupplyBuyable;


    /**
     * 配送渠道：mfn-listing-exists=true为卖家自配送；afn-listing-exists=true为亚马逊配送
     */
    public String switchDeliveryChannels(){
        if ("YES".equalsIgnoreCase(this.mfnListingExists)){
            return "selfDelivery";
        }
        if ("YES".equalsIgnoreCase(this.afnListingExists)){
            return "amazonDelivery";
        }
        return "";
    }

    public Integer mfnFulfillableQuantityCheckToInt(){
        if (StringUtils.isBlank(this.mfnFulfillableQuantity)){
            return 0;
        }
        return Integer.parseInt(this.mfnFulfillableQuantity);
    }
    public Integer afnInboundWorkingQuantityCheckToInt(){
        if (StringUtils.isBlank(this.afnInboundWorkingQuantity)){
            return 0;
        }
        return Integer.parseInt(this.afnInboundWorkingQuantity);
    }
    public Integer afnInboundShippedQuantityCheckToInt(){
        if (StringUtils.isBlank(this.afnInboundShippedQuantity)){
            return 0;
        }
        return Integer.parseInt(this.afnInboundShippedQuantity);
    }
    public Integer afnInboundReceivingQuantityCheckToInt(){
        if (StringUtils.isBlank(this.afnInboundReceivingQuantity)){
            return 0;
        }
        return Integer.parseInt(this.afnInboundReceivingQuantity);
    }
    public Integer afnFulfillableQuantityCheckToInt(){
        if (StringUtils.isBlank(this.afnFulfillableQuantity)){
            return 0;
        }
        return Integer.parseInt(this.afnFulfillableQuantity);
    }
    public Integer afnReservedQuantityCheckToInt(){
        if (StringUtils.isBlank(this.afnReservedQuantity)){
            return 0;
        }
        return Integer.parseInt(this.afnReservedQuantity);
    }
    public Integer afnResearchingQuantityCheckToInt(){
        if (StringUtils.isBlank(this.afnResearchingQuantity)){
            return 0;
        }
        return Integer.parseInt(this.afnResearchingQuantity);
    }
    public Integer afnUnsellableQuantityCheckToInt(){
        if (StringUtils.isBlank(this.afnUnsellableQuantity)){
            return 0;
        }
        return Integer.parseInt(this.afnUnsellableQuantity);
    }
}
