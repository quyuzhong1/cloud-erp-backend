package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 盘点计划表
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("stocktaking_plan")
public class StocktakingPlanEntity extends BaseEntity<StocktakingPlanEntity> {


    /**
    * 盘点计划单号
    */
    @TableField("code")
    private String code;

    /**
    * 盘点计划名称
    */
    @TableField("name")
    private String name;

    /**
    * 单据审核状态
    */
    @TableField("approve_status")
    private String approveStatus;

    /**
    * 盘点状态
    */
    @TableField("status")
    private String status;

    /**
    * 盘点方式
    */
    @TableField("mode")
    private String mode;

    /**
    * 分单规则
    */
    @TableField("separate_rule")
    private String separateRule;

    /**
    * 盘点类型
    */
    @TableField("type")
    private String type;

    /**
    * 提交审核时间
    */
    @TableField("submit_time")
    private LocalDateTime submitTime;

    /**
    * 提交审核人id
    */
    @TableField("submit_user_id")
    private String submitUserId;

    /**
    * 提交审核人名称
    */
    @TableField("submit_user_name")
    private String submitUserName;

    /**
    * 最后审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
    * 最后审核人名称
    */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
    * 最后审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
    * 动销开始时间
    */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
    * 动销结束时间
    */
    @TableField("end_time")
    private LocalDateTime endTime;


    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String STATUS = "status";

    public static final String MODE = "mode";

    public static final String SEPARATE_RULE = "separate_rule";

    public static final String TYPE = "type";

    public static final String SUBMIT_TIME = "submit_time";

    public static final String SUBMIT_USER_ID = "submit_user_id";

    public static final String SUBMIT_USER_NAME = "submit_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}