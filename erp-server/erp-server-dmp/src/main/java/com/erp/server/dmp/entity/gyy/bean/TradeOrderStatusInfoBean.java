package com.erp.server.dmp.entity.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class TradeOrderStatusInfoBean {
    /**
     * id : 546015421746
     * createDate : 1668082609000
     * modifyDate : 1668584602000
     * version : 7
     * tenantId : 307494129406
     * approve : true
     * approveName : 吴锦辉
     * approveDate : 1668130816000
     * cancel : false
     * cancelName : null
     * cancelDate : null
     * delivery : true
     * deliveryDate : 1668145716000
     * platformDelivery : true
     * platformDeliveryDate : 1668145716000
     * assignState : 2
     * deliveryState : 2
     */

    @SerializedName("id")
    private Long id;
    @SerializedName("createDate")
    private Long createDate;
    @SerializedName("modifyDate")
    private Long modifyDate;
    @SerializedName("version")
    private Integer version;
    @SerializedName("tenantId")
    private Long tenantId;
    @SerializedName("approve")
    private Boolean approve;
    @SerializedName("approveName")
    private String approveName;
    @SerializedName("approveDate")
    private Long approveDate;
    @SerializedName("cancel")
    private Boolean cancel;
    @SerializedName("cancelName")
    private String cancelName;
    @SerializedName("cancelDate")
    private String cancelDate;
    @SerializedName("delivery")
    private Boolean delivery;
    @SerializedName("deliveryDate")
    private Long deliveryDate;
    @SerializedName("platformDelivery")
    private Boolean platformDelivery;
    @SerializedName("platformDeliveryDate")
    private Long platformDeliveryDate;
    @SerializedName("assignState")
    private Integer assignState;
    @SerializedName("deliveryState")
    private Integer deliveryState;
}
