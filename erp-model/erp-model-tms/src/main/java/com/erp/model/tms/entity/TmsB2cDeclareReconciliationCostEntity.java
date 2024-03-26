package com.erp.model.tms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * B2c报关单
 * </p>
 *
 * @author will
 * @since 2024-03-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_b2c_declare_reconciliation_cost")
public class TmsB2cDeclareReconciliationCostEntity extends BaseEntity<TmsB2cDeclareReconciliationCostEntity> {

    /**
    * 主表id（对账单明细id）
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 费用值
    */
    @TableField("cost_value")
    private BigDecimal costValue;
    /**
    * 币别
    */
    @TableField("currency")
    private String currency;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 费用设置id
    */
    @TableField("cfg_cost_id")
    private String cfgCostId;
    /**
    * 类型（estimated预估、actual实际）
    */
    @TableField("type")
    private String type;


    public static final String MAIN_ID = "main_id";

    public static final String COST_VALUE = "cost_value";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String CFG_COST_ID = "cfg_cost_id";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}