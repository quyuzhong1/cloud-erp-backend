package com.erp.model.tms.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 头程对账单明细
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_first_mile_reconciliation_detail")
public class TmsFirstMileReconciliationDetailEntity extends BaseEntity<TmsFirstMileReconciliationDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;
    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 来源编码
     */
    @TableField("source_code")
    private String sourceCode;
    /**
     * 物流跟踪号
     */
    @TableField("track_no")
    private String trackNo;
    /**
     * 货件/计划单号
     */
    @TableField("business_code")
    private String businessCode;
    /**
     * 店铺ID
     */
    @TableField("shop_id")
    private String shopId;
    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;
    /**
     * 发货国家代号
     */
    @TableField("from_country")
    private String fromCountry;
    /**
     * 发货国家代号
     */
    @TableField("to_country")
    private String toCountry;
    /**
     * 签收日期
     */
    @TableField("receive_date")
    private LocalDate receiveDate;
    /**
     * 计费方式
     */
    @TableField("billing_method")
    private String billingMethod;
    /**
     * 计费方式名称
     */
    @TableField("billing_method_name")
    private String billingMethodName;
    /**
     * 类型(对账类型)
     */
    @TableField("type")
    private String type;
    /**
     * 总物流费用
     */
    @TableField("total_logistics_cost")
    private BigDecimal totalLogisticsCost;
    /**
     * 实际重量
     */
    @TableField("actual_weight")
    private BigDecimal actualWeight;
    /**
     * 实际重量单位
     */
    @TableField("actual_weight_unit")
    private String actualWeightUnit;
    /**
     * 体积重
     */
    @TableField("volume_weight")
    private BigDecimal volumeWeight;
    /**
     * 体积重单位
     */
    @TableField("volume_weight_unit")
    private String volumeWeightUnit;
    /**
     * 计费重
     */
    @TableField("billing_weight")
    private BigDecimal billingWeight;
    /**
     * 计费重单位
     */
    @TableField("billing_weight_unit")
    private String billingWeightUnit;
    /**
     * 实际物流运费用
     */
    @TableField("actual_shipping_cost")
    private BigDecimal actualShippingCost;
    /**
     * 实际报关费用
     */
    @TableField("actual_declare_cost")
    private BigDecimal actualDeclareCost;
    /**
     * 实际其他费用
     */
    @TableField("actual_other_cost")
    private BigDecimal actualOtherCost;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 对账状态
     */
    @TableField("status")
    private String status;
    /**
     * 确认时间
     */
    @TableField("confirm_date")
    private LocalDate confirmDate;
    /**
     * 确认人id
     */
    @TableField("confirm_user_id")
    private String confirmUserId;
    /**
     * 确认人名称
     */
    @TableField("confirm_user_name")
    private String confirmUserName;


    public static final String MAIN_ID = "main_id";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_CODE = "source_code";

    public static final String TRACK_NO = "track_no";

    public static final String BUSINESS_CODE = "business_code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String FROM_COUNTRY = "from_country";

    public static final String TO_COUNTRY = "to_country";

    public static final String RECEIVE_DATE = "receive_date";

    public static final String BILLING_METHOD = "billing_method";

    public static final String BILLING_METHOD_NAME = "billing_method_name";

    public static final String TYPE = "type";

    public static final String TOTAL_LOGISTICS_COST = "total_logistics_cost";

    public static final String ACTUAL_WEIGHT = "actual_weight";

    public static final String ACTUAL_WEIGHT_UNIT = "actual_weight_unit";

    public static final String VOLUME_WEIGHT = "volume_weight";

    public static final String VOLUME_WEIGHT_UNIT = "volume_weight_unit";

    public static final String BILLING_WEIGHT = "billing_weight";

    public static final String BILLING_WEIGHT_UNIT = "billing_weight_unit";

    public static final String ACTUAL_SHIPPING_COST = "actual_shipping_cost";

    public static final String ACTUAL_DECLARE_COST = "actual_declare_cost";

    public static final String ACTUAL_OTHER_COST = "actual_other_cost";

    public static final String REMARK = "remark";

    public static final String STATUS = "status";

    public static final String CONFIRM_DATE = "confirm_date";

    public static final String CONFIRM_USER_ID = "confirm_user_id";

    public static final String CONFIRM_USER_NAME = "confirm_user_name";

}