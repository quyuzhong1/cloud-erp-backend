package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 物流商对账明细（行级，对应 logistics_bill_cost）
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@TableName("logistics_recon_detail")
public class LogisticsReconDetailEntity extends BaseEntity<LogisticsReconDetailEntity> {

    /**
     * 对账单 id（→ logistics_recon.id）
     */
    @TableField("main_id")
    private String mainId;

    /**
     * Excel 原始行号，用于导出排序/排错
     */
    @TableField("row_no")
    private Integer rowNo;

    /**
     * 销售单号（配置字段 sourceCode；沿用系统已有定义 soCode）
     */
    @TableField("so_code")
    private String soCode;

    /**
     * 平台订单号（配置字段 platformCode）
     */
    @TableField("platform_order_no")
    private String platformOrderNo;

    /**
     * 物流跟踪号（配置字段 trackNo；沿用 logistics_bill_cost.track_no）
     */
    @TableField("track_no")
    private String trackNo;

    /**
     * 物流运单号（配置字段 transportNo；沿用 logistics_bill_cost.transport_no）
     */
    @TableField("transport_no")
    private String transportNo;

    /**
     * 发货单号（配置字段 soDeliveryCode）
     */
    @TableField("so_delivery_code")
    private String soDeliveryCode;

    /**
     * 匹配到的 ERP 销售单号（匹配成功快照，解绑置空，用于界面第三方/ERP 对照展示）
     */
    @TableField("erp_so_code")
    private String erpSoCode;

    /**
     * 匹配到的 ERP 平台订单号（匹配成功快照，解绑置空）
     */
    @TableField("erp_platform_order_no")
    private String erpPlatformOrderNo;

    /**
     * 匹配到的 ERP 物流跟踪号（匹配成功快照，解绑置空）
     */
    @TableField("erp_track_no")
    private String erpTrackNo;

    /**
     * 匹配到的 ERP 发货单号（匹配成功快照，解绑置空）
     */
    @TableField("erp_so_delivery_code")
    private String erpSoDeliveryCode;

    /**
     * 币别（配置字段 currency；沿用 logistics_bill_cost.currency）
     */
    @TableField("currency")
    private String currency;

    /**
     * 对账类型 enum：pay 付费 / refund 退费（配置字段 payType；沿用 logistics_bill_cost.pay_type）
     */
    @TableField("pay_type")
    private String payType;

    /**
     * 物流商实重（配置字段 thirdActualWeight；沿用 logistics_bill_cost.weight_logistics）
     */
    @TableField("weight_logistics")
    private BigDecimal weightLogistics;

    /**
     * 物流商体积重（沿用 logistics_bill_cost.volume_weight_logistics）
     */
    @TableField("volume_weight_logistics")
    private BigDecimal volumeWeightLogistics;

    /**
     * 物流商计费重（配置字段 billingWeightLogistics）
     */
    @TableField("billing_weight_logistics")
    private BigDecimal billingWeightLogistics;

    /**
     * 物流商重量单位（配置字段 logisticsWeightUnit；沿用 logistics_bill_cost.weight_unit）
     */
    @TableField("weight_unit")
    private String weightUnit;

    /**
     * 物流商尺寸-长（配置字段 thirdLength）
     */
    @TableField("third_length")
    private BigDecimal thirdLength;

    /**
     * 物流商尺寸-宽（配置字段 thirdWidth）
     */
    @TableField("third_width")
    private BigDecimal thirdWidth;

    /**
     * 物流商尺寸-高（配置字段 thirdHeight）
     */
    @TableField("third_height")
    private BigDecimal thirdHeight;

    /**
     * 该行展开的费用项条数（= 关联 logistics_recon_detail_sub 条数，冗余加速展示）
     */
    @TableField("cost_count")
    private Integer costCount;

    public static final String MAIN_ID = "main_id";
    public static final String ROW_NO = "row_no";
    public static final String SO_CODE = "so_code";
    public static final String PLATFORM_ORDER_NO = "platform_order_no";
    public static final String TRACK_NO = "track_no";
    public static final String TRANSPORT_NO = "transport_no";
    public static final String SO_DELIVERY_CODE = "so_delivery_code";
    public static final String ERP_SO_CODE = "erp_so_code";
    public static final String ERP_PLATFORM_ORDER_NO = "erp_platform_order_no";
    public static final String ERP_TRACK_NO = "erp_track_no";
    public static final String ERP_SO_DELIVERY_CODE = "erp_so_delivery_code";
    public static final String CURRENCY = "currency";
    public static final String PAY_TYPE = "pay_type";
    public static final String WEIGHT_LOGISTICS = "weight_logistics";
    public static final String VOLUME_WEIGHT_LOGISTICS = "volume_weight_logistics";
    public static final String BILLING_WEIGHT_LOGISTICS = "billing_weight_logistics";
    public static final String WEIGHT_UNIT = "weight_unit";
    public static final String THIRD_LENGTH = "third_length";
    public static final String THIRD_WIDTH = "third_width";
    public static final String THIRD_HEIGHT = "third_height";
    public static final String COST_COUNT = "cost_count";
    @Override
    public Serializable pkVal() {
        return this.getId();
    }
}
