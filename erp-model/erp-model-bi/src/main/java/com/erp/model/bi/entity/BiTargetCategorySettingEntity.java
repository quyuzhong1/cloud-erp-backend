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
 * 分类 目标设置表
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("bi_target_category_setting")
public class BiTargetCategorySettingEntity extends BaseEntity<BiTargetCategorySettingEntity> {

    /**
    * 主表id 对应 target_year 表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 分类id
    */
    @TableField("category_id")
    private String categoryId;
    /**
    * 分类名
    */
    @TableField("category_name")
    private String categoryName;
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

    public static final String CATEGORY_ID = "category_id";

    public static final String CATEGORY_NAME = "category_name";

    public static final String MONTH = "month";

    public static final String VALUE = "value";

    public static final String METRICS = "metrics";

    @Override
    public Serializable pkVal() {
        return null;
    }

}