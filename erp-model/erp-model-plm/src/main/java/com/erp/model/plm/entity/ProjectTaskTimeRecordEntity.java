package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author Cloud
 * @since 2023-02-23
 */
@Data
@Accessors(chain = true)
@TableName("project_task_time_record")
public class ProjectTaskTimeRecordEntity extends BaseEntity<ProjectTaskTimeRecordEntity> {

    /**
     * 任务开始时间
     */
    @TableField("reality_start_time")
    private Date realityStartTime;

    /**
     * 任务结束时间
     */
    @TableField("reality_end_time")
    private Date realityEndTime;

    /**
     * 操作人类型
     */
    @TableField("operator_type")
    private String operatorType;

    /**
     * 项目任务id
     */
    @TableField("project_task_id")
    private String projectTaskId;

    /**
     * 工时(天)
     */
    @TableField("task_time")
    private Integer taskTime;


    public static final String REALITY_START_TIME = "reality_start_time";

    public static final String REALITY_END_TIME = "reality_end_time";

    public static final String OPERATOR_TYPE = "operator_type";

    public static final String PROJECT_TASK_ID = "project_task_id";

    public static final String TASK_TIME = "task_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
