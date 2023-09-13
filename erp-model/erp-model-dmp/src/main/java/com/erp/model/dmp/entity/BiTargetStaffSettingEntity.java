package com.erp.model.dmp.entity;

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
 * 人员目标设置表
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("bi_target_staff_setting")
public class BiTargetStaffSettingEntity extends BaseEntity<BiTargetStaffSettingEntity> {

    /**
    * 主表id 对应 target_year 表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 员工id
    */
    @TableField("staff_id")
    private String staffId;
    /**
    * 员工名
    */
    @TableField("staff_name")
    private String staffName;
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

    public static final String STAFF_ID = "staff_id";

    public static final String STAFF_NAME = "staff_name";

    public static final String MONTH = "month";

    public static final String VALUE = "value";

    public static final String METRICS = "metrics";

    @Override
    public Serializable pkVal() {
        return null;
    }

}