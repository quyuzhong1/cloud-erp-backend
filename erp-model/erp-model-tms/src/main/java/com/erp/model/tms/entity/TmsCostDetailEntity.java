package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 自发货费用明细
 * </p>
 *
 * @author will
 * @since 2024-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_logistics_bill_cost_detail")
public class TmsCostDetailEntity extends BaseEntity<TmsCostDetailEntity> {

    /**
    * 主表id
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

    public static final String COST_CODE = "cost_code";

    public static final String COST_NAME = "cost_name";

    public static final String COST_VALUE = "cost_value";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String CFG_COST_ID = "cfg_cost_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}