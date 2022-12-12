package com.erp.server.dmp.entity.dmp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName dmp_order_info
 */
@TableName(value ="dmp_order_info")
@Data
public class DmpOrderInfoEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 平台订单id
     */
    @TableField(value = "platform_order_id")
    private String platformOrderId;

    /**
     * 订单状态 2.配货中 3.已发货 4.已完成 5.已作废
     */
    @TableField(value = "order_state")
    private Integer orderState;

    /**
     * 买家账号
     */
    @TableField(value = "buyer_user_id")
    private String buyerUserId;

    /**
     * 买家姓名
     */
    @TableField(value = "buyer_name")
    private String buyerName;

    /**
     * 店铺编号
     */
    @TableField(value = "shop_no")
    private String shopNo;

    /**
     * 店铺名称
     */
    @TableField(value = "shop_name")
    private String shopName;

    /**
     * 订单成本价
     */
    @TableField(value = "order_cost")
    private BigDecimal orderCost;

    /**
     * 待审核订单 1.否 2.是
     */
    @TableField(value = "can_send")
    private Integer canSend;

    /**
     * 是否退货 1.退货 2.非退货
     */
    @TableField(value = "is_returned")
    private Integer isReturned;

    /**
     * 是否退款 1.退款 2.非退款
     */
    @TableField(value = "is_refund")
    private Integer isRefund;

    /**
     * 订单付款时间
     */
    @TableField(value = "paid_time")
    private Date paidTime;

    /**
     * 平台交易号
     */
    @TableField(value = "sales_record_number")
    private String salesRecordNumber;

    /**
     * 平台的订单状态
     */
    @TableField(value = "platform_order_status")
    private String platformOrderStatus;

    /**
     * 订单金额
     */
    @TableField(value = "order_fee")
    private BigDecimal orderFee;

    /**
     * 订单来源平台
     */
    @TableField(value = "source_platform")
    private String sourcePlatform;

    /**
     * 是否合并订单 1.合并订单 2.非合并订单
     */
    @TableField(value = "is_union")
    private Integer isUnion;

    /**
     * 是否拆分订单 1.拆分订单 2.非拆分订单
     */
    @TableField(value = "is_split")
    private Integer isSplit;

    /**
     * 是否重发订单 1.重发订单 2.非重发订单
     */
    @TableField(value = "is_resend")
    private Integer isResend;

    /**
     * 缺货订单 0 正在计算是否缺货 1有货 2缺货 3 已补货
     */
    @TableField(value = "has_goods")
    private Integer hasGoods;

    /**
     * 所属区域
     */
    @TableField(value = "district")
    private String district;

    /**
     * 买家城市
     */
    @TableField(value = "city")
    private String city;

    /**
     * 买家省份
     */
    @TableField(value = "province")
    private String province;

    /**
     * 买家地址1
     */
    @TableField(value = "man_street")
    private String manStreet;

    /**
     * 买家地址2
     */
    @TableField(value = "second_street")
    private String secondStreet;

    /**
     * 交易关闭时间
     */
    @TableField(value = "close_date")
    private Date closeDate;

    /**
     * 买家电话1
     */
    @TableField(value = "man_phone")
    private String manPhone;

    /**
     * 买家电话2
     */
    @TableField(value = "second_phone")
    private String secondPhone;

    /**
     * 是否平台发货订单 1.否 2.是
     */
    @TableField(value = "fba_flag")
    private Integer fbaFlag;

    /**
     * 平台备注
     */
    @TableField(value = "seller_message")
    private String sellerMessage;

    /**
     * 币种
     */
    @TableField(value = "currency_code")
    private String currencyCode;

    /**
     * 汇率
     */
    @TableField(value = "currency_rate")
    private BigDecimal currencyRate;

    /**
     * 商品总售价
     */
    @TableField(value = "item_total")
    private BigDecimal itemTotal;

    /**
     * 运费收入
     */
    @TableField(value = "shipping_fee")
    private BigDecimal shippingFee;

    /**
     * 平台费
     */
    @TableField(value = "platform_fee")
    private BigDecimal platformFee;

    /**
     * 原始运费收入
     */
    @TableField(value = "shipping_total_origin")
    private BigDecimal shippingTotalOrigin;

    /**
     * 商品原始总售价
     */
    @TableField(value = "item_total_origin")
    private BigDecimal itemTotalOrigin;

    /**
     * 商品总成本
     */
    @TableField(value = "item_total_cost")
    private BigDecimal itemTotalCost;

    /**
     * 补贴金额
     */
    @TableField(value = "subsidy_amount")
    private BigDecimal subsidyAmount;

    /**
     * 国家英文名称
     */
    @TableField(value = "country_name_en")
    private String countryNameEn;

    /**
     * 国家英文名称
     */
    @TableField(value = "country_name_cn")
    private String countryNameCn;

    /**
     * 平台标识
     */
    @TableField(value = "platform_sign")
    private String platformSign;

    /**
     * 企业编号
     */
    @TableField(value = "company_id")
    private String companyId;

    /**
     * 企业名称
     */
    @TableField(value = "company_name")
    private String companyName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT_UPDATE)
    private Date createTime;

    /**
     * 平台订单时间
     */
    @TableField(value = "platform_create_time")
    private Date platformCreateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DmpOrderInfoEntity{" +
                "platformOrderId='" + platformOrderId + '\'' +
                ", orderState=" + orderState +
                ", buyerUserId='" + buyerUserId + '\'' +
                ", buyerName='" + buyerName + '\'' +
                ", shopNo='" + shopNo + '\'' +
                ", shopName='" + shopName + '\'' +
                ", orderCost=" + orderCost +
                ", canSend=" + canSend +
                ", isReturned=" + isReturned +
                ", isRefund=" + isRefund +
                ", paidTime=" + paidTime +
                ", salesRecordNumber='" + salesRecordNumber + '\'' +
                ", platformOrderStatus='" + platformOrderStatus + '\'' +
                ", orderFee=" + orderFee +
                ", sourcePlatform='" + sourcePlatform + '\'' +
                ", isUnion=" + isUnion +
                ", isSplit=" + isSplit +
                ", isResend=" + isResend +
                ", hasGoods=" + hasGoods +
                ", district='" + district + '\'' +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", manStreet='" + manStreet + '\'' +
                ", secondStreet='" + secondStreet + '\'' +
                ", closeDate=" + closeDate +
                ", manPhone='" + manPhone + '\'' +
                ", secondPhone='" + secondPhone + '\'' +
                ", fbaFlag=" + fbaFlag +
                ", sellerMessage='" + sellerMessage + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", currencyRate=" + currencyRate +
                ", itemTotal=" + itemTotal +
                ", shippingFee=" + shippingFee +
                ", platformFee=" + platformFee +
                ", shippingTotalOrigin=" + shippingTotalOrigin +
                ", itemTotalOrigin=" + itemTotalOrigin +
                ", itemTotalCost=" + itemTotalCost +
                ", subsidyAmount=" + subsidyAmount +
                ", countryNameEn='" + countryNameEn + '\'' +
                ", countryNameCn='" + countryNameCn + '\'' +
                ", platformSign='" + platformSign + '\'' +
                ", companyId='" + companyId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", platformCreateTime=" + platformCreateTime +
                '}';
    }
}