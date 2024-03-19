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
 * 头程物流单费用
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_first_mile_logistic_fee")
public class TmsFirstMileLogisticFeeEntity extends BaseEntity<TmsFirstMileLogisticFeeEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 系统配置id
    */
    @TableField("cfg_cost_id")
    private String cfgCostId;
    /**
    * 费用名称
    */
    @TableField("cost_name")
    private String costName;
    /**
    * 预估费用
    */
    @TableField("estimated_fee")
    private BigDecimal estimatedFee;


    public static final String MAIN_ID = "main_id";

    public static final String CFG_COST_ID = "cfg_cost_id";

    public static final String COST_NAME = "cost_name";

    public static final String ESTIMATED_FEE = "estimated_fee";

    @Override
    public Serializable pkVal() {
        return null;
    }

}