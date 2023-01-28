package com.erp.model.dmp.gyy;


import com.erp.model.dmp.gyy.bean.DeliveryDetailsBean;
import com.erp.model.dmp.gyy.bean.DeliveryStatusInfoBean;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class GyyDeliveryDetailEntity {
    /**
     * create_date : 2022-09-14 08:54:06
     * modify_date : 2022-12-08 15:29:26 -
     * code : SDO524472277623
     * qty : 2.0
     * pay_time : 2022-09-13 20:55:50
     * cod : false
     * refund : 0
     * invoiceDate : null -
     * bigchar : null -
     * cancel : 0 -
     * picture_bill : null
     * post_fee : 10.0000
     * cod_fee : 0
     * discount_fee : 3.0000
     * post_cost : 0
     * unpaid_amount : 0
     * pictureBill : false -
     * platform_code : 220913-323186933220499
     * plan_delivery_date : 2022-09-14 20:55:51 -
     * buyer_memo : null
     * seller_memo : null
     * receiver_name : vwjJ63Ub
     * receiver_phone : null
     * receiver_mobile : $OjROWL2a2/s+$
     * receiver_zip : null -
     * receiver_address : 新疆维吾尔自治区塔城地区乌苏市新区广隅新城*栋一单元*
     * create_name : 张伟霞
     * express_no : null
     * vip_name : vwjJ63Ub
     * shop_name : 拼多多-Ulanzi3C数码配件官方旗舰店
     * area_name : 新疆维吾尔自治区-塔城地区-乌苏市
     * warehouse_name : B2C天猫京东仓
     * express_code : SF-PDD
     * express_name : 拼多多--顺丰陆运
     * tag_name :  -
     * seller_memo_late : null -
     * shelf_no : null -
     * details : [{"qty":1,"discount":1,"refund":0,"itemCategoryName":null,"itemUnitName":null,"barcode":"3190","tariff":0,"memo":null,"picUrl":null,"oid":null,"discount_fee":"0","amount_after":"0","lack":0,"trade_code":"SO524352464025","origin_price":0,"origin_amount":0,"platform_item_name":null,"platform_sku_name":null,"item_id":"524228892871","item_sku_id":null,"item_code":"3190","item_name":"引流卡","sku_code":null,"sku_name":null,"sku_note":null,"combine_item_code":null,"location_code":"","platform_code":"220913-323186933220499","tax_rate":0,"tax_amount":0,"order_type":"Sales","platform_flag":0,"detail_unique":null,"detail_batch":null,"is_gift":1,"businessman_name":"吴晓锋","item_add_attribute":0,"gift_source_view":"【满就送赠品】-【买就送2922（A5版本）】","currency_code":null,"currency_name":null,"tax_no":null,"post_cost":"0","other_service_fee":"0","total_cost_price":"0","price":"0","amount":"0","post_fee":"0","plat_discount_amount":null,"distribution_post_fee":null,"sale_unit_name":null,"exchange_rate":null,"lack_qty":0,"lack_reason":null},{"qty":1,"discount":1,"refund":0,"itemCategoryName":"单品","itemUnitName":null,"barcode":"6972436382934","tariff":0,"memo":null,"picUrl":null,"oid":"182081636930","discount_fee":"3.0000","amount_after":"96.0000","lack":0,"trade_code":"SO524352464025","origin_price":99,"origin_amount":99,"platform_item_name":"2287||Ulanzi优篮子VL49RGB迷你补光灯彩色氛围vlog拍照便携磁吸打光灯[182081636930]","platform_sku_name":"||VL49 RGB补光灯[710883325301]","item_id":"313099350594","item_sku_id":null,"item_code":"2287","item_name":"Ulanzi VL49 RGB补光灯","sku_code":null,"sku_name":null,"sku_note":null,"combine_item_code":null,"location_code":"A-17-01","platform_code":"220913-323186933220499","tax_rate":0,"tax_amount":0,"order_type":"Sales","platform_flag":0,"detail_unique":null,"detail_batch":null,"is_gift":0,"businessman_name":"吴晓锋","item_add_attribute":0,"gift_source_view":null,"currency_code":null,"currency_name":null,"tax_no":null,"post_cost":"0","other_service_fee":"0","total_cost_price":"52.8000","price":"99.0000","amount":"99.0000","post_fee":"10.0000","plat_discount_amount":null,"distribution_post_fee":null,"sale_unit_name":null,"exchange_rate":null,"lack_qty":0,"lack_reason":null}]
     * delivery_statusInfo : {"scan":false,"weight":false,"wms":0,"delivery":0,"cancel":false,"intercept":false,"print_express":false,"express_print_name":null,"express_print_date":null,"print_delivery":false,"delivery_print_name":null,"delivery_print_date":null,"scan_name":null,"scan_date":null,"weight_name":null,"weight_date":null,"wms_order":0,"delivery_name":null,"delivery_date":null,"cancel_name":null,"cancel_date":null,"weight_qty":"0.0","thermal_print":1,"thermal_print_status":-1,"picking_user":null,"picking_date":null,"standard_weight":0.147,"pick_finish":false,"logistics_printed_bitch":null,"logistics_serial_no":null,"wms_date":null,"volume_total":null}
     * invoices : []
     * vip_code : OjROWL2a2/s+
     * warehouse_code : CK062
     * shop_code : 13
     * vip_real_name : vwjJ63Ub -
     * vip_id_card : null -
     * package_center_code : null  -
     * package_center_name : null -
     * sync_status : 0
     * sync_memo : null -
     * drp_tenant_name : null -
     * drp_tenant_mobile : null -
     * payment : 106.0000
     * amount : 106.0000
     * store_name : null -
     * store_code : null -
     * finance_approver : null -
     * appointment_no : null -
     * insure_amount : 0.0
     * sub_type_name : null -
     * stock_location : []
     * distribution_channel : null -
     * tax_amount_total : 0
     */

    private String _id;

    @SerializedName("create_date")
    private String createDate;
    @SerializedName("modify_date")
    private String modifyDate;
    @SerializedName("code")
    private String code;
    @SerializedName("qty")
    private Double qty;
    @SerializedName("pay_time")
    private String payTime;
    @SerializedName("cod")
    private Boolean cod;
    @SerializedName("refund")
    private Integer refund;
    @SerializedName("invoiceDate")
    private String invoiceDate;
    @SerializedName("bigchar")
    private String bigchar;
    @SerializedName("cancel")
    private Boolean cancel;
    @SerializedName("post_fee")
    private BigDecimal postFee;
    @SerializedName("cod_fee")
    private String codFee;
    @SerializedName("discount_fee")
    private String discountFee;
    @SerializedName("post_cost")
    private String postCost;
    @SerializedName("unpaid_amount")
    private String unpaidAmount;
    @SerializedName("pictureBill")
    private Boolean pictureBill;
    @SerializedName("platform_code")
    private String platformCode;
    @SerializedName("plan_delivery_date")
    private String planDeliveryDate;
    @SerializedName("buyer_memo")
    private String buyerMemo;
    @SerializedName("seller_memo")
    private String sellerMemo;
    @SerializedName("receiver_name")
    private String receiverName;
    @SerializedName("receiver_phone")
    private String receiverPhone;
    @SerializedName("receiver_mobile")
    private String receiverMobile;
    @SerializedName("receiver_zip")
    private String receiverZip;
    @SerializedName("receiver_address")
    private String receiverAddress;
    @SerializedName("create_name")
    private String createName;
    @SerializedName("express_no")
    private String expressNo;
    @SerializedName("vip_name")
    private String vipName;
    @SerializedName("shop_name")
    private String shopName;
    @SerializedName("area_name")
    private String areaName;
    @SerializedName("warehouse_name")
    private String warehouseName;
    @SerializedName("express_code")
    private String expressCode;
    @SerializedName("express_name")
    private String expressName;
    @SerializedName("tag_name")
    private String tagName;
    @SerializedName("seller_memo_late")
    private String sellerMemoLate;
    @SerializedName("shelf_no")
    private String shelfNo;
    @SerializedName("delivery_statusInfo")
    private DeliveryStatusInfoBean deliveryStatusInfo;
    @SerializedName("vip_code")
    private String vipCode;
    @SerializedName("warehouse_code")
    private String warehouseCode;
    @SerializedName("shop_code")
    private String shopCode;
    @SerializedName("vip_real_name")
    private String vipRealName;
    @SerializedName("vip_id_card")
    private String vipIdCard;
    @SerializedName("package_center_code")
    private String packageCenterCode;
    @SerializedName("package_center_name")
    private String packageCenterName;
    @SerializedName("sync_status")
    private Integer syncStatus;
    @SerializedName("sync_memo")
    private String syncMemo;
    @SerializedName("drp_tenant_name")
    private String drpTenantName;
    @SerializedName("drp_tenant_mobile")
    private String drpTenantMobile;
    @SerializedName("payment")
    private String payment;
    @SerializedName("amount")
    private BigDecimal amount;
    @SerializedName("store_name")
    private String storeName;
    @SerializedName("store_code")
    private String storeCode;
    @SerializedName("finance_approver")
    private String financeApprover;
    @SerializedName("appointment_no")
    private String appointmentNo;
    @SerializedName("insure_amount")
    private Double insureAmount;
    @SerializedName("sub_type_name")
    private String subTypeName;
    @SerializedName("distribution_channel")
    private String distributionChannel;
    @SerializedName("tax_amount_total")
    private String taxAmountTotal;
    @SerializedName("details")
    private List<DeliveryDetailsBean> details;
    @SerializedName("invoices")
    private List<Object> invoices;
    @SerializedName("stock_location")
    private List<Object> stockLocation;

    @Override
    public String toString() {
        return "GyyDeliveryDetailEntity{" +
                "createDate='" + createDate + '\'' +
                ", modifyDate='" + modifyDate + '\'' +
                ", code='" + code + '\'' +
                ", qty=" + qty +
                ", payTime='" + payTime + '\'' +
                ", cod=" + cod +
                ", refund=" + refund +
                ", invoiceDate='" + invoiceDate + '\'' +
                ", bigchar='" + bigchar + '\'' +
                ", cancel=" + cancel +
                ", postFee=" + postFee +
                ", codFee='" + codFee + '\'' +
                ", discountFee='" + discountFee + '\'' +
                ", postCost='" + postCost + '\'' +
                ", unpaidAmount='" + unpaidAmount + '\'' +
                ", pictureBill=" + pictureBill +
                ", platformCode='" + platformCode + '\'' +
                ", planDeliveryDate='" + planDeliveryDate + '\'' +
                ", buyerMemo='" + buyerMemo + '\'' +
                ", sellerMemo='" + sellerMemo + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", receiverPhone='" + receiverPhone + '\'' +
                ", receiverMobile='" + receiverMobile + '\'' +
                ", receiverZip='" + receiverZip + '\'' +
                ", receiverAddress='" + receiverAddress + '\'' +
                ", createName='" + createName + '\'' +
                ", expressNo='" + expressNo + '\'' +
                ", vipName='" + vipName + '\'' +
                ", shopName='" + shopName + '\'' +
                ", areaName='" + areaName + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", expressCode='" + expressCode + '\'' +
                ", expressName='" + expressName + '\'' +
                ", tagName='" + tagName + '\'' +
                ", sellerMemoLate='" + sellerMemoLate + '\'' +
                ", shelfNo='" + shelfNo + '\'' +
                ", deliveryStatusInfo=" + deliveryStatusInfo +
                ", vipCode='" + vipCode + '\'' +
                ", warehouseCode='" + warehouseCode + '\'' +
                ", shopCode='" + shopCode + '\'' +
                ", vipRealName='" + vipRealName + '\'' +
                ", vipIdCard='" + vipIdCard + '\'' +
                ", packageCenterCode='" + packageCenterCode + '\'' +
                ", packageCenterName='" + packageCenterName + '\'' +
                ", syncStatus=" + syncStatus +
                ", syncMemo='" + syncMemo + '\'' +
                ", drpTenantName='" + drpTenantName + '\'' +
                ", drpTenantMobile='" + drpTenantMobile + '\'' +
                ", payment='" + payment + '\'' +
                ", amount=" + amount +
                ", storeName='" + storeName + '\'' +
                ", storeCode='" + storeCode + '\'' +
                ", financeApprover='" + financeApprover + '\'' +
                ", appointmentNo='" + appointmentNo + '\'' +
                ", insureAmount=" + insureAmount +
                ", subTypeName='" + subTypeName + '\'' +
                ", distributionChannel='" + distributionChannel + '\'' +
                ", taxAmountTotal='" + taxAmountTotal + '\'' +
                ", details=" + details +
                '}';
    }
}
