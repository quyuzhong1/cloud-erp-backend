package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 任务评论关联表
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("task_comment_ref")
public class TaskCommentRefEntity extends BaseEntity<TaskCommentRefEntity> {


    /**
    * 任务评论表id
    */
    @TableField("task_comment_id")
    private String taskCommentId;

    /**
    * 关联人员id
    */
    @TableField("ref_user_id")
    private String refUserId;

    /**
    * 发送通知结果true 成功
    */
    @TableField("send_notice_result")
    private Boolean sendNoticeResult;

    /**
    * 任务id
    */
    @TableField("task_id")
    private String taskId;


    public static final String TASK_COMMENT_ID = "task_comment_id";

    public static final String REF_USER_ID = "ref_user_id";

    public static final String SEND_NOTICE_RESULT = "send_notice_result";

    public static final String TASK_ID = "task_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}