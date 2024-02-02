package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.tms.aliexpress.model.handover.ParcelOrder;
import com.erp.tms.aliexpress.model.handover.SlaveBigbag;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName HandoverQueryResponse
 * @description: TODO
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
public class HandoverQueryResponse implements Serializable {
    /**
     * bigbag：大包约揽 batch：批次约揽
     */
    @JSONField(name = "appointment_type")
    private String appointmentType;

    /**
     * bigbag：大包约揽 batch：批次约揽
     */
    @JSONField(name = "appointment_type_name")
    private String appointmentTypeName;
    /**
     * DOOR_PICKUP:上门揽收 SELF_POST:自寄 SELF_SEND：自送
     */
    @JSONField(name = "pickup_type")
    private String pickupType;
    /**
     * DOOR_PICKUP:上门揽收 SELF_POST:自寄 SELF_SEND：自送
     */
    @JSONField(name = "pickup_type_name")
    private String pickupTypeName;
    /**
     * master：主大包，slave：子大包(批次揽收才有)
     */
    @JSONField(name = "bigbag_type")
    private String bigbagType;
    /**
     * 批次约揽下的子大包列表
     */
    @JSONField(name = "slave_bigbag_list")
    private List<SlaveBigbag> SlaveBigbagList;
    /**
     * 交接物物流订单编号
     */
    @JSONField(name = "order_code")
    private String orderCode;
    /**
     * 交接物运单号
     */
    @JSONField(name = "tracking_number")
    private String trackingNumber;
    /**
     * 交接物状态
     */
    @JSONField(name = "status")
    private String status;
    /**
     * bigbag：大包约揽 batch：批次约揽
     */
    @JSONField(name = "parcel_order_list")
    private List<ParcelOrder> parcelOrderList;
    /**
     * 预估重量
     */
    @JSONField(name = "estimate_weight")
    private String estimateWeight;
    /**
     * 实际重量
     */
    @JSONField(name = "actual_weight")
    private String actualWeight;
    /**
     * 重量单位
     */
    @JSONField(name = "weight_unit")
    private String weightUnit;
    /**
     * 预估费用
     */
    @JSONField(name = "estimate_fee")
    private String estimateFee;
    /**
     * 实际费用
     */
    @JSONField(name = "actual_fee")
    private String actualFee;
    /**
     * 费用币种
     */
    @JSONField(name = "fee_currency")
    private String feeCurrency;
    /**
     * 费用单位
     */
    @JSONField(name = "fee_unit")
    private String feeUnit;
    /**
     * 交接物状态
     */
    @JSONField(name = "status_name")
    private String statusName;
    /**
     * 交接物关联的交接单状态code
     */
    @JSONField(name = "handover_order_status")
    private String handoverOrderStatus;
    /**
     * 交接物关联的交接单状态名称
     */
    @JSONField(name = "handover_order_status_name")
    private String handoverOrderStatusName;
}
