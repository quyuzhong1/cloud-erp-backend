package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName dmp_error_log
 */
@TableName(value ="dmp_error_log")
@Data
public class DmpErrorLogEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务表id
     */
    @TableField(value = "task_id")
    private Long taskId;

    /**
     * 请求参数
     */
    @TableField(value = "params")
    private String params;

    /**
     * 返回信息
     */
    @TableField(value = "return_msg")
    private String returnMsg;

    /**
     * 错误信息
     */
    @TableField(value = "error_msg")
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}