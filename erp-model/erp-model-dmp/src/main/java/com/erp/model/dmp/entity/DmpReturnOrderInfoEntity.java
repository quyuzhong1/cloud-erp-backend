package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 退货订单表
 * @TableName dmp_return_order_info
 */
@TableName(value ="dmp_return_order_info")
@Data
public class DmpReturnOrderInfoEntity implements Serializable {
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
     * 退货单号
     */
    @TableField(value = "return_order_id")
    private String returnOrderId;

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
     * 付款时间
     */
    @TableField(value = "paid_time")
    private Date paidTime;

    /**
     * 发货时间
     */
    @TableField(value = "express_time")
    private Date expressTime;

    /**
     * 状态：1待处理 2已退款 3已重发 4已完成 5已作废
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 平台交易号
     */
    @TableField(value = "sales_record_number")
    private String salesRecordNumber;

    /**
     * 订单金额
     */
    @TableField(value = "order_fee")
    private BigDecimal orderFee;

    /**
     * 订单重量
     */
    @TableField(value = "order_weight")
    private BigDecimal orderWeight;

    /**
     * 平台名称
     */
    @TableField(value = "platform_name")
    private String platformName;

    /**
     * 国家英文名称
     */
    @TableField(value = "country_name_en")
    private String countryNameEn;

    /**
     * 国家中文名称
     */
    @TableField(value = "country_name_cn")
    private String countryNameCn;

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
     * 登记人编号
     */
    @TableField(value = "employee_id")
    private String employeeId;

    /**
     * 登记人名称
     */
    @TableField(value = "employee_name")
    private String employeeName;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 退货信息创建时间
     */
    @TableField(value = "return_create_time")
    private Date returnCreateTime;

    /**
     * 退款时间
     */
    @TableField(value = "refund_time")
    private Date refundTime;

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
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT_UPDATE)
    private Date createTime;

    /**
     * 平台标识
     */
    @TableField(value = "platform_sign")
    private String platformSign;

    /**
     * 企业id
     */
    @TableField(value = "company_id")
    private String companyId;

    /**
     * 企业名称
     */
    @TableField(value = "company_name")
    private String companyName;

    /**
     * 订单时间
     */
    @TableField(value = "order_time")
    private Date orderTime;

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
        return "DmpReturnOrderInfoEntity{" +
                "platformOrderId='" + platformOrderId + '\'' +
                ", returnOrderId='" + returnOrderId + '\'' +
                ", shopNo='" + shopNo + '\'' +
                ", shopName='" + shopName + '\'' +
                ", paidTime=" + paidTime +
                ", expressTime=" + expressTime +
                ", status=" + status +
                ", salesRecordNumber='" + salesRecordNumber + '\'' +
                ", orderFee=" + orderFee +
                ", orderWeight=" + orderWeight +
                ", platformName='" + platformName + '\'' +
                ", countryNameEn='" + countryNameEn + '\'' +
                ", countryNameCn='" + countryNameCn + '\'' +
                ", buyerUserId='" + buyerUserId + '\'' +
                ", buyerName='" + buyerName + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", remark='" + remark + '\'' +
                ", returnCreateTime=" + returnCreateTime +
                ", refundTime=" + refundTime +
                ", currencyCode='" + currencyCode + '\'' +
                ", currencyRate=" + currencyRate +
                ", platformSign='" + platformSign + '\'' +
                '}';
    }
}