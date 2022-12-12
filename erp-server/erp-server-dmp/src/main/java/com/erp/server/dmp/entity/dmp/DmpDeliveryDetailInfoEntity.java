package com.erp.server.dmp.entity.dmp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @TableName dmp_delivery_detail_info
 */
@TableName(value ="dmp_delivery_detail_info")
@Data
public class DmpDeliveryDetailInfoEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 单据编号
     */
    @TableField(value = "bill_no")
    private String billNo;

    /**
     * 订单编号
     */
    @TableField(value = "order_no")
    private String orderNo;

    /**
     * 物流单号
     */
    @TableField(value = "logistics_no")
    private String logisticsNo;

    /**
     * 客户名称
     */
    @TableField(value = "customer_name")
    private String customerName;

    /**
     * 平台名称
     */
    @TableField(value = "platform_name")
    private String platformName;

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
    @TableField(value = "item_total_cost")
    private BigDecimal itemTotalCost;

    /**
     * 订单总价
     */
    @TableField(value = "order_total_cost")
    private BigDecimal orderTotalCost;

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
     * 所属区域
     */
    @TableField(value = "district")
    private String district;

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
     * 运费
     */
    @TableField(value = "shipping_fee")
    private BigDecimal shippingFee;

    /**
     * 补贴金额
     */
    @TableField(value = "subsidy_amount")
    private BigDecimal subsidyAmount;

    /**
     * 销售部门
     */
    @TableField(value = "sale_dept_name")
    private String saleDeptName;

    /**
     * 销售员编号
     */
    @TableField(value = "sales_man_id")
    private String salesManId;

    /**
     * 销售员名称
     */
    @TableField(value = "sales_man_name")
    private String salesManName;

    /**
     * 状态 1.已发货 2..已作废
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 平台单据审核时间
     */
    @TableField(value = "platform_approve_time")
    private Date platformApproveTime;

    /**
     * 平台单据创建时间
     */
    @TableField(value = "platform_create_time")
    private Date platformCreateTime;

    /**
     * 平台单据修改时间
     */
    @TableField(value = "platform_update_time")
    private Date platformUpdateTime;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 企业id
     */
    @TableField(value = "company_id")
    private String companyId;

    /**
     * 平台标识
     */
    @TableField(value = "platform_sign")
    private String platformSign;

    /**
     * 企业名称
     */
    @TableField(value = "company_name")
    private String companyName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 发货时间
     */
    @TableField(value = "delivery_date")
    private Date deliveryDate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DmpDeliveryDetailInfoEntity{" +
                "billNo='" + billNo + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", logisticsNo='" + logisticsNo + '\'' +
                ", customerName='" + customerName + '\'' +
                ", platformName='" + platformName + '\'' +
                ", shopNo='" + shopNo + '\'' +
                ", shopName='" + shopName + '\'' +
                ", itemTotalCost=" + itemTotalCost +
                ", orderTotalCost=" + orderTotalCost +
                ", countryNameEn='" + countryNameEn + '\'' +
                ", countryNameCn='" + countryNameCn + '\'' +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", manStreet='" + manStreet + '\'' +
                ", secondStreet='" + secondStreet + '\'' +
                ", district='" + district + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", currencyRate=" + currencyRate +
                ", shippingFee=" + shippingFee +
                ", subsidyAmount=" + subsidyAmount +
                ", saleDeptName='" + saleDeptName + '\'' +
                ", salesManId='" + salesManId + '\'' +
                ", salesManName='" + salesManName + '\'' +
                ", status=" + status +
                ", platformApproveTime=" + platformApproveTime +
                ", platformCreateTime=" + platformCreateTime +
                ", platformUpdateTime=" + platformUpdateTime +
                ", remark='" + remark + '\'' +
                ", companyId='" + companyId + '\'' +
                ", platformSign='" + platformSign + '\'' +
                ", companyName='" + companyName + '\'' +
                ", deliveryDate=" + deliveryDate +
                '}';
    }
}