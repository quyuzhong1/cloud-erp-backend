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
import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * b2c报关对账单明细
 * </p>
 *
 * @author will
 * @since 2024-03-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_b2c_declare_reconciliation_detail")
public class TmsB2cDeclareReconciliationDetailEntity extends BaseEntity<TmsB2cDeclareReconciliationDetailEntity> {

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
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
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
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售订单编码
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 入库预报日期
    */
    @TableField("date")
    private LocalDate date;
    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;

    /**
    * 国家
    */
    @TableField("country")
    private String country;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流渠道名称
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
     * 物流商Id
     */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;
    /**
     * 物流商名称
     */
    @TableField("logistics_supplier_name")
    private String logisticsSupplierName;
    /**
    * 产品数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 预估重量
    */
    @TableField("estimate_weight")
    private BigDecimal estimateWeight;
    /**
    * 预估重量单位
    */
    @TableField("estimate_weight_unit")
    private String estimateWeightUnit;
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
    * 实际计费重
    */
    @TableField("actual_billing_weight")
    private BigDecimal actualBillingWeight;
    /**
    * 实际物流运费
    */
    @TableField("actual_shipping_cost")
    private BigDecimal actualShippingCost;
    /**
     * 实际物流运费币别
     */
     @TableField("actual_shipping_currency")
     private String actualShippingCurrency;
    /**
    * 实际报关费
    */
    @TableField("actual_declare_cost")
    private BigDecimal actualDeclareCost;
    /**
     * 实际报关费币别
     */
    @TableField("actual_declare_currency")
    private String actualDeclareCurrency;
    /**
    * 实际其他费
    */
    @TableField("actual_other_cost")
    private BigDecimal actualOtherCost;
    /**
     * 实际其他费币别
     */
    @TableField("actual_other_currency")
    private String actualOtherCurrency;
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
     * 费用编辑（导入数据返回）
     */
    @TableField(exist = false)
    private List<TmsCostDetailDTO.UpdateDTO> updateList;


    public static final String MAIN_ID = "main_id";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_CODE = "source_code";

    public static final String SO_ID = "so_id";

    public static final String SO_DETAIL_ID = "so_detail_id";

    public static final String SO_CODE = "so_code";

    public static final String FIELD_DATE = "date";

    public static final String FIELD_COUNTRY = "country";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String FIELD_QTY = "qty";

    public static final String ESTIMATE_WEIGHT = "estimate_weight";

    public static final String ESTIMATE_WEIGHT_UNIT = "estimate_weight_unit";

    public static final String ACTUAL_WEIGHT = "actual_weight";

    public static final String ACTUAL_WEIGHT_UNIT = "actual_weight_unit";

    public static final String ACTUAL_BILLING_WEIGHT = "actual_billing_weight";

    public static final String ACTUAL_SHIPPING_COST = "actual_shipping_cost";

    public static final String ACTUAL_DECLARE_COST = "actual_declare_cost";

    public static final String ACTUAL_OTHER_COST = "actual_other_cost";

    public static final String FIELD_REMARK = "remark";

    public static final String FIELD_STATUS = "status";

    public static final String CONFIRM_DATE = "confirm_date";

    public static final String CONFIRM_USER_ID = "confirm_user_id";

    public static final String CONFIRM_USER_NAME = "confirm_user_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}