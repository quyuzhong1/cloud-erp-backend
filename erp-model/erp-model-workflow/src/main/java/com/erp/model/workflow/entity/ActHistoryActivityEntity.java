package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

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
public class ActHistoryActivityEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

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


    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
