package com.erp.model.dmp.gyy;

import com.erp.model.dmp.dto.CleanBaseDTO;
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
public class GyyReturnOrderEntity extends CleanBaseDTO {
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
    /**
     * 单据编号
     */
    @SerializedName("code")
    private String code;
    /**
     * 平台退货原因 bug，如果有用户有修改备注，此部分会变为返回备注
     */
    @SerializedName("reason")
    private String reason;
    /**
     * 备注
     */
    @SerializedName("note")
    private String note;
    /**
     * 创建时间
     */
    @SerializedName("create_date")
    private String createDate;
    /**
     * 审核时间
     */
    @SerializedName("approve_date")
    private String approveDate;
    /**
     * 审核状态 0:未审核 1:已审核
     */
    @SerializedName("approve")
    private Integer approve;
    /**
     * 入库时间
     */
    @SerializedName("receive_date")
    private String receiveDate;
    /**
     * 是否取消
     */
    @SerializedName("cancel")
    private Boolean cancel;
    /**
     * 是否已推送wms
     */
    @SerializedName("wms")
    private Integer wms;
    /**
     * 店铺名称
     */
    @SerializedName("shop_name")
    private String shopName;
    /**
     * 店铺编码
     */
    @SerializedName("shop_code")
    private String shopCode;
    /**
     * 平台编码
     */
    @SerializedName("platform_code")
    private String platformCode;
    /**
     * 会员编码
     */
    @SerializedName("vip_code")
    private String vipCode;
    /**
     * 退入仓库代码
     */
    @SerializedName("warehousein_code")
    private String warehouseinCode;
    /**
     * 退出仓库代码
     */
    @SerializedName("warehouseout_code")
    private String warehouseoutCode;
    /**
     * 退入快递公司代码
     */
    @SerializedName("express_code")
    private String expressCode;
    /**
     * 退入快递单号
     */
    @SerializedName("express_num")
    private String expressNum;
    /**
     * 退货人手机
     */
    @SerializedName("receiver_phone")
    private String receiverPhone;
    /**
     * 退货原因代码  系统退货原因
     */
    @SerializedName("return_type")
    private String returnType;
    /**
     * 入库状态 0:未入库 1:入库成功
     */
    @SerializedName("receive")
    private String receive;
    /**
     * 同意退货状态 0:未处理 1:同意 2:拒绝
     */
    @SerializedName("agree_refuse")
    private Integer agreeRefuse;
    /**
     * 销售订单号
     */
    @SerializedName("order_code")
    private String orderCode;
    /**
     * 修改时间
     */
    @SerializedName("modify_date")
    private String modifyDate;
    /**
     * 业务员
     */
    @SerializedName("business_man")
    private String businessMan;
    /**
     * 收货人
     */
    @SerializedName("receiver_name")
    private String receiverName;
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
     * 收货人省市区
     */
    @SerializedName("area_name")
    private String areaName;
    /**
     * 平台退款单号
     */
    @SerializedName("platform_refund_id")
    private String platformRefundId;
    /**
     * 是否三无包装 true：三无包裹 false：非三无包裹
     */
    @SerializedName("sanwu_package")
    private Boolean sanwuPackage;
    /**
     * 售后类型 1:退货 2:换货 3:补发 4:其他 5:维修
     */
    @SerializedName("refund_type")
    private Integer refundType;
    /**
     * 售后阶段 1：买家确认收货前 2：买家确认收货后
     */
    @SerializedName("refund_phase")
    private Integer refundPhase;
    /**
     * 退入快递名称
     */
    @SerializedName("express_name")
    private String expressName;
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
     * 作废日期
     */
    @SerializedName("cancel_date")
    private String cancelDate;
    /**
     * 退入仓库名称
     */
    @SerializedName("warehousein_name")
    private String warehouseinName;
    /**
     * 会员名称
     */
    @SerializedName("vip_name")
    private String vipName;
    /**
     * 平台状态
     */
    @SerializedName("platform_status")
    private String platformStatus;
    /**
     * 门店名称
     */
    @SerializedName("store_name")
    private String storeName;
    /**
     * 门店编码
     */
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
    @Override
    public String toString() {
        return "GyyReturnOrderEntity{" +
                "code='" + code + '\'' +
                ", reason='" + reason + '\'' +
                ", note='" + note + '\'' +
                ", createDate='" + createDate + '\'' +
                ", approveDate='" + approveDate + '\'' +
                ", approve=" + approve +
                ", receiveDate='" + receiveDate + '\'' +
                ", cancel=" + cancel +
                ", wms=" + wms +
                ", shopName='" + shopName + '\'' +
                ", shopCode='" + shopCode + '\'' +
                ", platformCode='" + platformCode + '\'' +
                ", vipCode='" + vipCode + '\'' +
                ", warehouseinCode='" + warehouseinCode + '\'' +
                ", warehouseoutCode='" + warehouseoutCode + '\'' +
                ", expressCode='" + expressCode + '\'' +
                ", expressNum='" + expressNum + '\'' +
                ", receiverPhone='" + receiverPhone + '\'' +
                ", returnType='" + returnType + '\'' +
                ", receive='" + receive + '\'' +
                ", agreeRefuse=" + agreeRefuse +
                ", orderCode='" + orderCode + '\'' +
                ", modifyDate='" + modifyDate + '\'' +
                ", businessMan='" + businessMan + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", receiverMobile='" + receiverMobile + '\'' +
                ", receiverZip='" + receiverZip + '\'' +
                ", receiverAddress='" + receiverAddress + '\'' +
                ", areaName='" + areaName + '\'' +
                ", platformRefundId='" + platformRefundId + '\'' +
                ", sanwuPackage=" + sanwuPackage +
                ", refundType=" + refundType +
                ", refundPhase=" + refundPhase +
                ", expressName='" + expressName + '\'' +
                ", drpTenantName='" + drpTenantName + '\'' +
                ", drpTenantMobile='" + drpTenantMobile + '\'' +
                ", cancelDate='" + cancelDate + '\'' +
                ", warehouseinName='" + warehouseinName + '\'' +
                ", vipName='" + vipName + '\'' +
                ", platformStatus='" + platformStatus + '\'' +
                ", storeName='" + storeName + '\'' +
                ", storeCode='" + storeCode + '\'' +
                ", commodityStatus=" + commodityStatus +
                ", agStatus=" + agStatus +
                ", tagId='" + tagId + '\'' +
                ", createOrder=" + createOrder +
                ", createRefund=" + createRefund +
                ", tradeRefundCodes='" + tradeRefundCodes + '\'' +
                ", changeOutExpressCode='" + changeOutExpressCode + '\'' +
                ", changeOutExpressName='" + changeOutExpressName + '\'' +
                ", wmsOrder='" + wmsOrder + '\'' +
                ", wmsDate='" + wmsDate + '\'' +
                ", approveName='" + approveName + '\'' +
                ", kingdeeSyncStatus='" + kingdeeSyncStatus + '\'' +
                ", kingdeeSyncMemo='" + kingdeeSyncMemo + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", refundState=" + refundState +
                ", distributionChannel='" + distributionChannel + '\'' +
                ", details=" + details +
                ", payments=" + payments +
                ", refundCodes=" + refundCodes +
                ", stockLocation=" + stockLocation +
                '}';
    }
}
