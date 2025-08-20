package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


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
     * 来源id(物流单)
     */
    @TableField("source_id")
    private String sourceId;
    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 来源编码(货件/计划单号)
     */
    @TableField("source_code")
    private String sourceCode;
    /**
     * 运输单号
     */
    @TableField("transport_no")
    private String transportNo;
    /**
     * 业务编号
     */
    @TableField("business_code")
    private String businessCode;
    /**
     * 运输状态
     */
    @TableField("transport_status")
    private String transportStatus;
    /**
     * 关联单号
     */
    @TableField("relation_code")
    private String relationCode;
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
     * 计费规则
     */
    @TableField("fee_rule")
    private String feeRule;
    /**
     * 计费规则名称
     */
    @TableField("fee_rule_name")
    private String feeRuleName;
    /**
     * 物流渠道id
     */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
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
     * 运费用
     */
    @TableField("shipping_cost")
    private BigDecimal shippingCost;
    /**
     * 运费用币别
     */
    @TableField("shipping_cost_currency")
    private String shippingCostCurrency;
    /**
     * 报关费用
     */
    @TableField("declare_cost")
    private BigDecimal declareCost;
    /**
     * 报关费用币别
     */
    @TableField("declare_cost_currency")
    private String declareCostCurrency;
    /**
     * 其他费用
     */
    @TableField("other_cost")
    private BigDecimal otherCost;
    /**
     * 其他费用币别
     */
    @TableField("other_cost_currency")
    private String otherCostCurrency;
    /**
     * 其他税费
     */
    @TableField("other_tax_cost")
    private BigDecimal otherTaxCost;
    /**
     * 其他税费币别
     */
    @TableField("other_tax_currency")
    private String otherTaxCurrency;
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

    /**
     * 对账次数 默认1
     */
    @TableField("reconciliation_count")
    private Integer reconciliationCount;

    /**
     * 对账月份（取值为对账周期末值所在月份）
     */
    @TableField(exist = false)
    private LocalDate reconciliationMonth;
    /**
     * 币别
     */
    @TableField(exist = false)
    private String currency;
    /**
     * 直接汇率
     */
    @TableField(exist = false)
    private BigDecimal exchangeRate;

    /**
     * 账单类型： actual=实际， initPeriod=期初
     * ReconciliationTypeEnum
     */
    @TableField("reconciliation_type")
    private String reconciliationType;
    /**
     * 供应商id
     *
     */
    @TableField(exist = false)
    private String logisticsSupplierId;
    /**
     * 供应商id
     *
     */
    @TableField(exist = false)
    private String logisticsSupplierName;
    /**
     * 供应商类型
     * SupplierTypeEnum
     */
    @TableField(exist = false)
    private String supplierType;
    /**
     * 费用项id
     */
    @TableField(exist = false)
    private String costId;

    /**
     * 费用编辑（导入数据返回）
     */
    @TableField(exist = false)
    private List<TmsCostDetailDTO.UpdateDTO> updateList;


    public void setTotalLogisticsCost(){
        this.setTotalLogisticsCost(this.shippingCost.add(this.declareCost).add(this.otherCost).add(this.otherTaxCost));
    }


    public static final String MAIN_ID = "main_id";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_CODE = "source_code";

    public static final String TRACK_NO = "track_no";

    public static final String RELATION_CODE = "relation_code";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String FROM_COUNTRY = "from_country";

    public static final String TO_COUNTRY = "to_country";

    public static final String RECEIVE_DATE = "receive_date";

    public static final String FEE_RULE = "fee_rule";

    public static final String FEE_RULE_NAME = "fee_rule_name";

    public static final String FIELD_TYPE = "type";

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

    public static final String FIELD_REMARK = "remark";

    public static final String FIELD_STATUS = "status";

    public static final String CONFIRM_DATE = "confirm_date";

    public static final String CONFIRM_USER_ID = "confirm_user_id";

    public static final String CONFIRM_USER_NAME = "confirm_user_name";

}