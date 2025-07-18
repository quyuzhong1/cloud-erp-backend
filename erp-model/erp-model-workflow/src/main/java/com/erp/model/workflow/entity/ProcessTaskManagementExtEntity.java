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
 * process_task_management拓展表
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("process_task_management_ext")
public class ProcessTaskManagementExtEntity extends BaseEntity<ProcessTaskManagementExtEntity> {

    /**
    * process_task_management_id
    */
    @TableField("process_task_management_id")
    private String processTaskManagementId;
    /**
    * message_id
    */
    @TableField("message_id")
    private String messageId;
    /**
    * 来源平台
    */
    @TableField("souce_platform")
    private String soucePlatform;


    public static final String PROCESS_TASK_MANAGEMENT_ID = "process_task_management_id";

    public static final String MESSAGE_ID = "message_id";

    public static final String SOUCE_PLATFORM = "souce_platform";

    @Override
    public Serializable pkVal() {
        return null;
    }

}