package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 自发货费用
 * </p>
 *
 * @author Will
 * @since 2023-11-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_bill_cost")
public class LogisticsBillCostEntity extends BaseEntity<LogisticsBillCostEntity> {

    /**
    * 对账状态（字典reconciliationStatus）
    */
    @TableField("reconciliation_status")
    private String reconciliationStatus;

    /**
     * 类型，枚举DictCostAttributionEnum，firstMile头程， selfDeliver自发货，lastMile尾程
     */
    @TableField("type")
    private String type;

    /**
    * 物流渠道id
    */
    @TableField("channel_id")
    private String channelId;

    /**
    * 物流单id
    */
    @TableField("logistics_bill_id")
    private String logisticsBillId;

    /**
     * 物流单明细id
     */
    @TableField("logistics_bill_detail_id")
    private String logisticsBillDetailId;

    /**
     * 运输单号
     */
    @TableField("transport_no")
    private String transportNo;

    /**
     * 跟踪单号
     */
    @TableField("track_no")
    private String trackNo;

    /**
    * 实重
    */
    @TableField("actual_weight")
    private BigDecimal actualWeight;

    /**
    * 体积重
    */
    @TableField("volume_weight")
    private BigDecimal volumeWeight;

    /**
    * 计费重
    */
    @TableField("billing_weight")
    private BigDecimal billingWeight;

    /**
     * 重量单位
     */
    @TableField("weight_unit")
    private String weightUnit;

    /**
    * 计费重（物流商）
    */
    @TableField("billing_weight_logistics")
    private BigDecimal billingWeightLogistics;

    /**
    * 币别
    */
    @TableField("currency")
    private String currency;

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
     * 实际体积重(物流商)
     */
    @TableField("volume_weight_logistics")
    private BigDecimal volumeWeightLogistics;

    /**
     * 实重(物流商)
     */
    @TableField("weight_logistics")
    private BigDecimal weightLogistics;

    /**
     * 店铺负责人id
     */
    @TableField(value = "shop_charge_id")
    private String shopChargeId;

    /**
     * 店铺负责人名称
     */
    @TableField(value = "shop_charge_name")
    private String shopChargeName;

    /**
     * 费用规则
     */
    @TableField(value = "fee_rule")
    private String feeRule;
    /**
     * 账单确认时间
     */
    @TableField(value = "confirm_time")
    private LocalDateTime confirmTime;

    /**
     * 账单确认人id
     */
    @TableField(value = "confirm_user_id")
    private String confirmUserId;

    /**
     * 账单确认人名称
     */
    @TableField(value = "confirm_user_name")
    private String confirmUserName;

    /**
     * 对账单id
     */
    @TableField(value = "reconciliation_id")
    private String reconciliationId;

    /**
     * 费用编辑（导入数据返回）
     */
    @TableField(exist = false)
    private List<TmsCostDetailDTO.UpdateDTO> updateList;

    public static final String RECONCILIATION_STATUS = "reconciliation_status";

    public static final String CHANNEL_ID = "channel_id";

    public static final String LOGISTICS_BILL_ID = "logistics_bill_id";

    public static final String ACTUAL_WEIGHT = "actual_weight";

    public static final String VOLUME_WEIGHT = "volume_weight";

    public static final String BILLING_WEIGHT = "billing_weight";

    public static final String ESTIMATED_SHIPPING_COST  = "estimated_shipping_cost ";

    public static final String BILLING_WEIGHT_LOGISTICS = "billing_weight_logistics";

    public static final String DIFF_SHIPPING_COST = "diff_shipping_cost";

    public static final String FIELD_CURRENCY = "currency";

    public static final String FIELD_REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}