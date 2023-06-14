package com.erp.model.dmp.gyy;


import com.erp.model.dmp.dto.CleanBaseDTO;
import com.erp.model.dmp.gyy.bean.DeliveryDetailsBean;
import com.erp.model.dmp.gyy.bean.DeliveryStatusInfoBean;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode
@NoArgsConstructor
public class GyyDeliveryDetailEntity extends CleanBaseDTO {
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

    /**
     * 制单时间
     */
    @SerializedName("create_date")
    private String createDate;
    /**
     * 修改时间
     */
    @SerializedName("modify_date")
    private String modifyDate;
    /**
     * 单据编号
     */
    @SerializedName("code")
    private String code;
    /**
     * 商品总数
     */
    @SerializedName("qty")
    private Double qty;
    /**
     * 支付时间
     */
    @SerializedName("pay_time")
    private String payTime;
    /**
     * 是否货到付款
     */
    @SerializedName("cod")
    private Boolean cod;
    /**
     * 是否退款 0:无退款 1:有退款
     */
    @SerializedName("refund")
    private Integer refund;
    /**
     * 确认开票时间
     */
    @SerializedName("invoiceDate")
    private String invoiceDate;
    /**
     * 大头笔
     */
    @SerializedName("bigchar")
    private String bigchar;
    /**
     * 是否取消
     */
    @SerializedName("cancel")
    private Boolean cancel;
    /**
     * 物流费用
     */
    @SerializedName("post_fee")
    private BigDecimal postFee;
    /**
     * 货到付款服务费
     */
    @SerializedName("cod_fee")
    private String codFee;
    /**
     * 优惠金额
     */
    @SerializedName("discount_fee")
    private String discountFee;
    /**
     * 物流成本
     */
    @SerializedName("post_cost")
    private String postCost;
    /**
     * 未付金额
     */
    @SerializedName("unpaid_amount")
    private String unpaidAmount;
    /**
     * 是否多包裹 true：多包裹 false：单包裹
     */
    @SerializedName("picture_bill")
    private Boolean pictureBill;
    /**
     * 平台单号
     */
    @SerializedName("platform_code")
    private String platformCode;
    /**
     * 预计发货时间
     */
    @SerializedName("plan_delivery_date")
    private String planDeliveryDate;
    /**
     * 买家留言
     */
    @SerializedName("buyer_memo")
    private String buyerMemo;
    /**
     * 卖家留言
     */
    @SerializedName("seller_memo")
    private String sellerMemo;
    /**
     * 收货人姓名
     */
    @SerializedName("receiver_name")
    private String receiverName;
    /**
     * 收货人电话
     */
    @SerializedName("receiver_phone")
    private String receiverPhone;
    /**
     * 收货人手机
     */
    @SerializedName("receiver_mobile")
    private String receiverMobile;
    /**
     * 收货人邮编
     */
    @SerializedName("receiver_zip")
    private String receiverZip;
    /**
     * 收货人地址
     */
    @SerializedName("receiver_address")
    private String receiverAddress;
    /**
     * 制单人
     */
    @SerializedName("create_name")
    private String createName;
    /**
     * 快递单号
     */
    @SerializedName("express_no")
    private String expressNo;
    /**
     * 会员名称
     */
    @SerializedName("vip_name")
    private String vipName;
    /**
     * 店铺名称
     */
    @SerializedName("shop_name")
    private String shopName;
    /**
     * 收货地区
     */
    @SerializedName("area_name")
    private String areaName;
    /**
     * 仓库名称
     */
    @SerializedName("warehouse_name")
    private String warehouseName;
    /**
     * 库位代码
     */
    @SerializedName("location_code")
    private String locationCode;
    /**
     * 快递公司代码
     */
    @SerializedName("express_code")
    private String expressCode;
    /**
     * 快递公司名称
     */
    @SerializedName("express_name")
    private String expressName;
    /**
     * 订单标记名称
     */
    @SerializedName("tag_name")
    private String tagName;
    /**
     * 二次备注
     */
    @SerializedName("seller_memo_late")
    private String sellerMemoLate;
    /**
     * 格子号
     */
    @SerializedName("shelf_no")
    private String shelfNo;
    /**
     * 单据状态信息
     */
    @SerializedName("delivery_statusInfo")
    private DeliveryStatusInfoBean deliveryStatusInfo;
    /**
     * 会员代码
     */
    @SerializedName("vip_code")
    private String vipCode;
    /**
     * 仓库代码
     */
    @SerializedName("warehouse_code")
    private String warehouseCode;
    /**
     * 店铺代码
     */
    @SerializedName("shop_code")
    private String shopCode;
    /**
     * 真实姓名
     */
    @SerializedName("vip_real_name")
    private String vipRealName;
    /**
     * 会员身份证号
     */
    @SerializedName("vip_id_card")
    private String vipIdCard;
    /**
     * 集包地代码
     */
    @SerializedName("package_center_code")
    private String packageCenterCode;
    /**
     * 集包地名称
     */
    @SerializedName("package_center_name")
    private String packageCenterName;
    /**
     * 数据同步状态 -1:同步失败 0:未同步 1:已同步
     */
    @SerializedName("sync_status")
    private Integer syncStatus;
    /**
     * 数据同步备注
     */
    @SerializedName("sync_memo")
    private String syncMemo;
    /**
     * 分销商名称
     */
    @SerializedName("drp_tenant_name")
    private String drpTenantName;
    /**
     * 分销商手机号
     */
    @SerializedName("drp_tenant_mobile")
    private String drpTenantMobile;
    /**
     * 已付金额
     */
    @SerializedName("payment")
    private String payment;
    /**
     * 总金额
     */
    @SerializedName("amount")
    private BigDecimal amount;
    /**
     * 门店名称
     */
    @SerializedName("store_name")
    private String storeName;
    /**
     * 门店代码
     */
    @SerializedName("store_code")
    private String storeCode;
    /**
     * 保险金额
     */
    @SerializedName("insure_amount")
    private Double insureAmount;
    /**
     * 保险费
     */
    @SerializedName("tax_amount_total")
    private String taxAmountTotal;
    /**
     *商品列表
     */
    @SerializedName("details")
    private List<DeliveryDetailsBean> details;
    /**
     * 发票列表
     */
    @SerializedName("invoices")
    private List<Object> invoices;
    /**
     * 附件列表
     */
    @SerializedName("stock_location")
    private List<Object> stockLocation;

    /**
     * 数据下载状态
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    private Integer downloadStatus;

    /**
     * api编码，订单来源于历史订单还是当前订单接口区分
     */
    private String apiCode;


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
                ", insureAmount=" + insureAmount +
                ", taxAmountTotal='" + taxAmountTotal + '\'' +
                ", details=" + details +
                '}';
    }
}
