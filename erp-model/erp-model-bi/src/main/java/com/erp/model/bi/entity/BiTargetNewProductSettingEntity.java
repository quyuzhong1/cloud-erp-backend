package com.erp.model.bi.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.erp.model.bi.enums.MetricsEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 新品目标设置表
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("bi_target_new_product_setting")
public class BiTargetNewProductSettingEntity extends BaseEntity<BiTargetNewProductSettingEntity> {

    /**
    * 主表id 对应 target_year 表id
    */
    @TableField("main_id")
    private String mainId;

    /**
    * 月
    */
    @TableField("month")
    private Integer month;
    /**
    * 对应值
    */
    @TableField("value")
    private BigDecimal value;
    /**
    * 指标维度
    */
    @TableField("metrics")
    private MetricsEnum metrics;
    /**
    * 员工id
    */
    @TableField("staff_id")
    private String staffId;
    /**
    * 员工名称
    */
    @TableField("staff_name")
    private String staffName;
    /**
    * 占比
    */
    @TableField("rate")
    private BigDecimal rate;

    public static final String MAIN_ID = "main_id";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String MONTH = "month";

    public static final String VALUE = "value";

    public static final String METRICS = "metrics";

    public static final String STAFF_ID = "staff_id";

    public static final String STAFF_NAME = "staff_name";

    public static final String RATE  = "rate";

    @Override
    public Serializable pkVal() {
        return null;
    }

}