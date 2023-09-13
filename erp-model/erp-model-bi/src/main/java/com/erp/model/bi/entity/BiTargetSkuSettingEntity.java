package com.erp.model.bi.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * sku 目标设置表
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("bi_target_sku_setting")
public class BiTargetSkuSettingEntity extends BaseEntity<BiTargetSkuSettingEntity> {

    /**
    * 主表id 对应 target_year 表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * sku id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku no
    */
    @TableField("sku_no")
    private String skuNo;
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
    private String metrics;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String MONTH = "month";

    public static final String VALUE = "value";

    public static final String METRICS = "metrics";

    @Override
    public Serializable pkVal() {
        return null;
    }

}