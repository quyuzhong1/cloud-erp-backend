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
 * 
 * </p>
 *
 * @author will
 * @since 2024-03-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_logistics_bill_cost_detail")
public class TmsLogisticsBillCostDetailEntity extends BaseEntity<TmsLogisticsBillCostDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 费用编码
    */
    @TableField("cost_code")
    private String costCode;
    /**
    * 费用名称
    */
    @TableField("cost_name")
    private String costName;
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


    public static final String MAIN_ID = "main_id";

    public static final String COST_CODE = "cost_code";

    public static final String COST_NAME = "cost_name";

    public static final String COST_VALUE = "cost_value";

    public static final String CURRENCY = "currency";

    @Override
    public Serializable pkVal() {
        return null;
    }

}