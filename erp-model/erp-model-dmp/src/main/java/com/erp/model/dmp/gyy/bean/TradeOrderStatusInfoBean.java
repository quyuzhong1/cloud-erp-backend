package com.erp.model.dmp.gyy.bean;

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
    /**
     * 订单ID
     */
    @SerializedName("id")
    private Long id;
    /**
     * 创建时间
     */
    @SerializedName("createDate")
    private Long createDate;
    /**
     * 修改时间
     */
    @SerializedName("modifyDate")
    private Long modifyDate;
    /**
     * 版本号
     */
    @SerializedName("version")
    private Integer version;
    /**
     * 租户ID
     */
    @SerializedName("tenantId")
    private Long tenantId;
    /**
     * 是否审核
     */
    @SerializedName("approve")
    private Boolean approve;
    /**
     * 审核人
     */
    @SerializedName("approveName")
    private String approveName;
    /**
     * 审核时间
     */
    @SerializedName("approveDate")
    private Long approveDate;
    /**
     * 是否取消
     */
    @SerializedName("cancel")
    private Boolean cancel;
    /**
     * 取消人
     */
    @SerializedName("cancelName")
    private String cancelName;
    /**
     * 取消时间
     */
    @SerializedName("cancelDate")
    private String cancelDate;
    /**
     * 是否发货
     */
    @SerializedName("delivery")
    private Boolean delivery;
    /**
     * 发货时间
     */
    @SerializedName("deliveryDate")
    private Long deliveryDate;
    /**
     * 是否平台发货
     */
    @SerializedName("platformDelivery")
    private Boolean platformDelivery;
    /**
     * 平台发货时间
     */
    @SerializedName("platformDeliveryDate")
    private Long platformDeliveryDate;
    /**
     * 是否分配 0:未分配 1:部分配货 2:全部配货
     */
    @SerializedName("assignState")
    private Integer assignState;
    /**
     * 是否发货 0:未发货 1:部分发货 2:全部发货
     */
    @SerializedName("delivery_state")
    private Integer deliveryState;
}
