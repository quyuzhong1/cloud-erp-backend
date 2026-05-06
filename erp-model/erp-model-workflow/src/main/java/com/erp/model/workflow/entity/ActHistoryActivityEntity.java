package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author yl
 * @since 2022-08-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_history_activity")
public class ActHistoryActivityEntity extends BaseEntity<ActHistoryActivityEntity> {

    /**
     * 流程id
     */
    @TableField("process_instance_id")
    private String processInstanceId;

    /**
     * 活动id
     */
    @TableField("activity_id")
    private String activityId;

    /**
     * 上一个活动id
     */
    @TableField("pre_activity_id")
    private String preActivityId;

    /**
     * 下一个活动id
     */
    @TableField("next_activity_id")
    private String nextActivityId;

    /**
     * 审核状态
     */
    @TableField("audit_status")
    private String auditStatus;

}
