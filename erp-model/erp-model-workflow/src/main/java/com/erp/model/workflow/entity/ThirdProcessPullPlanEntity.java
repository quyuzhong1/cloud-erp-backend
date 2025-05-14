package com.erp.model.workflow.entity;

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
 * 三方流程实例拉取任务
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_process_pull_plan")
public class ThirdProcessPullPlanEntity extends BaseEntity<ThirdProcessPullPlanEntity> {

    /**
    * 审批创建时间
    */
    @TableField("start_time")
    private String startTime;
    /**
    * 审批完成时间
    */
    @TableField("end_time")
    private String endTime;
    /**
    * 状态
    */
    @TableField("status")
    private String status;
    /**
    * 审批定义 Code
    */
    @TableField("approval_code")
    private String approvalCode;


    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String STATUS = "status";

    public static final String APPROVAL_CODE = "approval_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}