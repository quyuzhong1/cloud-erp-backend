package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 年度目标表
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("bi_target_year")
public class BiTargetYearEntity extends BaseEntity<BiTargetYearEntity> {

    /**
    * 年
    */
    @TableField("year")
    private String year;

    /**
     * 考核字标多个逗号分割
     */
    @TableField("metrics")
    private String metrics;

    /**
    * 货币
    */
    @TableField("currency")
    private String currency;
    /**
    * 货币符号
    */
    @TableField("currency_symbol")
    private String currencySymbol;
    /**
    * 部门id
    */
    @TableField("dept_id")
    private String deptId;
    /**
    * 部门名称
    */
    @TableField("dept_name")
    private String deptName;


    public static final String YEAR = "year";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}