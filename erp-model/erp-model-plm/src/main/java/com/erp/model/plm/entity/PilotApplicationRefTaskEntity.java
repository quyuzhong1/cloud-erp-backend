package com.erp.model.plm.entity;

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
 * 试产/量产 关联任务
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pilot_application_ref_task")
public class PilotApplicationRefTaskEntity extends BaseEntity<PilotApplicationRefTaskEntity> {

    /**
    * 试产/量产主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 任务ID
    */
    @TableField("task_id")
    private String taskId;


    public static final String MAIN_ID = "main_id";

    public static final String TASK_ID = "task_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}