package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款表
 * @TableName dmp_refund_info
 */
@TableName(value ="dmp_refund_info")
@Data
public class DmpRefundInfoEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 平台订单编号
     */
    @TableField(value = "platform_order_id")
    private String platformOrderId;

    /**
     * 币别编号
     */
    @TableField(value = "currency_code")
    private String currencyCode;

    /**
     * 退货金额
     */
    @TableField(value = "refund_amount")
    private BigDecimal refundAmount;

    /**
     * 退款类型：1、未收到货部分退款 2、未收到货全额退款 3、已收到货部分退款 4、已收到货全额退款 
     */
    @TableField(value = "refund_type")
    private Integer refundType;

    /**
     * 退款原因
     */
    @TableField(value = "refund_reason_desc")
    private String refundReasonDesc;

    /**
     * 退款备注
     */
    @TableField(value = "refund_remark")
    private String refundRemark;

    /**
     * 退款状态：1、新建退款 2、审核中 3、财务审核 4、成功 5、失败 6、作废
     */
    @TableField(value = "refund_status")
    private Integer refundStatus;

    /**
     * 申请时间
     */
    @TableField(value = "refund_create_time")
    private Date refundCreateTime;

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
     * 平台名称
     */
    @TableField(value = "platform_name")
    private String platformName;

    /**
     * 退款时间
     */
    @TableField(value = "refund_time")
    private Date refundTime;

    /**
     * 汇率
     */
    @TableField(value = "currency_rate")
    private BigDecimal currencyRate;

    /**
     * 国家二字码 例如：US
     */
    @TableField(value = "country_code")
    private String countryCode;

    /**
     * 国家中文名
     */
    @TableField(value = "country_cn")
    private String countryCn;

    /**
     * 国家英文名
     */
    @TableField(value = "country_en")
    private String countryEn;

    /**
     * 平台交易号
     */
    @TableField(value = "sales_record_number")
    private String salesRecordNumber;

    /**
     * 买家用户Id
     */
    @TableField(value = "buyer_user_id")
    private String buyerUserId;

    /**
     * 买家用户名
     */
    @TableField(value = "buyer_name")
    private String buyerName;

    /**
     * 原始订单金额
     */
    @TableField(value = "item_total_origin")
    private BigDecimal itemTotalOrigin;

    /**
     * 原始订单运费金额
     */
    @TableField(value = "shipping_total_origin")
    private BigDecimal shippingTotalOrigin;

    /**
     * 订单时间
     */
    @TableField(value = "order_time")
    private Date orderTime;

    /**
     * 发货时间
     */
    @TableField(value = "express_time")
    private Date expressTime;

    /**
     * 退货图片多个用英文 , 隔开
     */
    @TableField(value = "picture_url")
    private String pictureUrl;

    /**
     * 平台最后修改时间
     */
    @TableField(value = "platform_update_time")
    private Date platformUpdateTime;

    /**
     * 包裹单号
     */
    @TableField(value = "track_number")
    private String trackNumber;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT_UPDATE)
    private Date createTime;

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
     * 退款单号
     */
    @TableField(value = "refund_id")
    private String refundId;

    /**
     * 平台标识
     */
    @TableField(value = "platform_sign")
    private String platformSign;

    /**
     * cny-结算汇率
     */
    @TableField(value = "cny_settle_rate")
    private String cnySettleRate;

    /**
     * 清洗状态  1 未清洗 2 清洗完成
     */
    @TableField(value = "clean_state")
    private Integer cleanState;

    /**
     * 重试次数
     */
    @TableField(value = "retry_count")
    private Integer retryCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DmpRefundInfoEntity{" +
                "platformOrderId='" + platformOrderId + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", refundAmount=" + refundAmount +
                ", refundType=" + refundType +
                ", refundReasonDesc='" + refundReasonDesc + '\'' +
                ", refundRemark='" + refundRemark + '\'' +
                ", refundStatus=" + refundStatus +
                ", refundCreateTime=" + refundCreateTime +
                ", shopNo='" + shopNo + '\'' +
                ", shopName='" + shopName + '\'' +
                ", platformName='" + platformName + '\'' +
                ", refundTime=" + refundTime +
                ", currencyRate=" + currencyRate +
                ", countryCode='" + countryCode + '\'' +
                ", countryCn='" + countryCn + '\'' +
                ", countryEn='" + countryEn + '\'' +
                ", salesRecordNumber='" + salesRecordNumber + '\'' +
                ", buyerUserId='" + buyerUserId + '\'' +
                ", buyerName='" + buyerName + '\'' +
                ", itemTotalOrigin=" + itemTotalOrigin +
                ", shippingTotalOrigin=" + shippingTotalOrigin +
                ", expressTime=" + expressTime +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", platformUpdateTime=" + platformUpdateTime +
                ", trackNumber='" + trackNumber + '\'' +
                ", companyId='" + companyId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", refundId='" + refundId + '\'' +
                ", platformSign='" + platformSign + '\'' +
                '}';
    }
}