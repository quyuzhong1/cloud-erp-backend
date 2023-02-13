package com.erp.model.dmp.gyy;

import com.erp.model.dmp.gyy.bean.ReturnOrderDetailsBean;
import com.erp.model.dmp.gyy.bean.ReturnOrderPayments;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class GyyReturnOrderEntity {
    /**
     * code : RGO315677282722
     * reason : 7天无理由退换货
     * note :
     * create_date : 2021-03-09 10:00:12
     * approve_date : null
     * approve : 0
     * receive_date : null
     * cancel : true
     * wms : 0
     * shop_name : 天猫-Uurig唯迹专卖店
     * shop_code : 03
     * platform_code : 1560753362709029282
     * vip_code : null
     * warehousein_code : null
     * warehouseout_code : null
     * express_code : null
     * express_num : 75443686708148
     * receiver_phone : null
     * details : [{"id":315677284382,"qty":2,"note":"7天无理由退换货","oid":null,"discount":1,"total_cost_price":"0","cost_price":"0","post_fee":null,"sku_code":null,"item_sku_id":null,"sku_note":null,"item_name":null,"amount_after":"0","item_code":null,"item_id":null,"real_in":0,"price":"146.0000","amount":"292.0000","discount_fee":"0","is_gift":0,"detail_batch":null,"detail_unique":null,"combine_item_code":null,"other_service_fee":0,"location_code":null,"platform_code":"1560753362709029282","origin_amount":"0","origin_price":"0","location_name":null,"item_unit_name":null}]
     * payments : [{"payment":292,"account":"2021020522001100261434628493","note":null,"pay_type_code":"zhifubao","pay_time":null}]
     * return_type : null
     * receive : 0
     * agree_refuse : null
     * order_code :
     * modify_date : 2021-08-24 11:13:16
     * business_man : null
     * receiver_name : icymoon_2008
     * receiver_mobile : null
     * receiver_zip : null
     * receiver_address : 唯迹专卖店-潘楚明， 17150305423， 广东省东莞市    凤岗镇金凤凰大道凤芝美工业园K栋5A楼1号电梯 ， 518000
     * area_name : null
     * platform_refund_id : 101429569742028292
     * sanwu_package : false
     * refund_type : 1
     * refund_phase : 1
     * express_name : 中通快递
     * drp_tenant_name : null
     * drp_tenant_mobile : null
     * cancel_date : 2021-08-24 11:13:16
     * warehousein_name : null
     * vip_name : null
     * platform_status : 退换货/退款成功
     * refund_codes : []
     * store_name : null
     * store_code : null
     * commodity_status : 0
     * ag_status : 0
     * tag_id : null
     * create_order : false
     * create_refund : false
     * trade_refund_codes : null
     * change_out_express_code : null
     * change_out_express_name : null
     * wms_order : 0
     * wms_date : null
     * approve_name : null
     * kingdee_sync_status : null
     * kingdee_sync_memo : null
     * source_type : null
     * refund_state : null
     * distribution_channel : null
     * stock_location : []
     */

    private String _id;

    @SerializedName("code")
    private String code;
    @SerializedName("reason")
    private String reason;
    @SerializedName("note")
    private String note;
    @SerializedName("create_date")
    private LocalDateTime createDate;
    @SerializedName("approve_date")
    private LocalDateTime approveDate;
    @SerializedName("approve")
    private Integer approve;
    @SerializedName("receive_date")
    private String receiveDate;
    @SerializedName("cancel")
    private Boolean cancel;
    @SerializedName("wms")
    private Integer wms;
    @SerializedName("shop_name")
    private String shopName;
    @SerializedName("shop_code")
    private String shopCode;
    @SerializedName("platform_code")
    private String platformCode;
    @SerializedName("vip_code")
    private String vipCode;
    @SerializedName("warehousein_code")
    private String warehouseinCode;
    @SerializedName("warehouseout_code")
    private String warehouseoutCode;
    @SerializedName("express_code")
    private String expressCode;
    @SerializedName("express_num")
    private String expressNum;
    @SerializedName("receiver_phone")
    private String receiverPhone;
    @SerializedName("return_type")
    private String returnType;
    @SerializedName("receive")
    private String receive;
    @SerializedName("agree_refuse")
    private Integer agreeRefuse;
    @SerializedName("order_code")
    private String orderCode;
    @SerializedName("modify_date")
    private String modifyDate;
    @SerializedName("business_man")
    private String businessMan;
    @SerializedName("receiver_name")
    private String receiverName;
    @SerializedName("receiver_mobile")
    private String receiverMobile;
    @SerializedName("receiver_zip")
    private String receiverZip;
    @SerializedName("receiver_address")
    private String receiverAddress;
    @SerializedName("area_name")
    private String areaName;
    @SerializedName("platform_refund_id")
    private String platformRefundId;
    @SerializedName("sanwu_package")
    private Boolean sanwuPackage;
    @SerializedName("refund_type")
    private Integer refundType;
    @SerializedName("refund_phase")
    private Integer refundPhase;
    @SerializedName("express_name")
    private String expressName;
    @SerializedName("drp_tenant_name")
    private String drpTenantName;
    @SerializedName("drp_tenant_mobile")
    private String drpTenantMobile;
    @SerializedName("cancel_date")
    private String cancelDate;
    @SerializedName("warehousein_name")
    private String warehouseinName;
    @SerializedName("vip_name")
    private String vipName;
    @SerializedName("platform_status")
    private String platformStatus;
    @SerializedName("store_name")
    private String storeName;
    @SerializedName("store_code")
    private String storeCode;
    @SerializedName("commodity_status")
    private Integer commodityStatus;
    @SerializedName("ag_status")
    private Integer agStatus;
    @SerializedName("tag_id")
    private String tagId;
    @SerializedName("create_order")
    private Boolean createOrder;
    @SerializedName("create_refund")
    private Boolean createRefund;
    @SerializedName("trade_refund_codes")
    private String tradeRefundCodes;
    @SerializedName("change_out_express_code")
    private String changeOutExpressCode;
    @SerializedName("change_out_express_name")
    private String changeOutExpressName;
    @SerializedName("wms_order")
    private String wmsOrder;
    @SerializedName("wms_date")
    private String wmsDate;
    @SerializedName("approve_name")
    private String approveName;
    @SerializedName("kingdee_sync_status")
    private String kingdeeSyncStatus;
    @SerializedName("kingdee_sync_memo")
    private String kingdeeSyncMemo;
    @SerializedName("source_type")
    private String sourceType;
    @SerializedName("refund_state")
    private Integer refundState;
    @SerializedName("distribution_channel")
    private String distributionChannel;
    @SerializedName("details")
    private List<ReturnOrderDetailsBean> details;
    @SerializedName("payments")
    private List<ReturnOrderPayments> payments;
    @SerializedName("refund_codes")
    private List<?> refundCodes;
    @SerializedName("stock_location")
    private List<?> stockLocation;
}
