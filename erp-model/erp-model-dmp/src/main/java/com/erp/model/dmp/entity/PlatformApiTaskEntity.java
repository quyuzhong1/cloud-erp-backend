package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName platform_api_task
 */
@TableName(value ="platform_api_task")
@Data
public class PlatformApiTaskEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 平台表id
     */
    @TableField(value = "platform_id")
    private Integer platformId;

    /**
     * 平台接口表id
     */
    @TableField(value = "api_id")
    private Integer apiId;

    /**
     * 平台api接口
     */
    @TableField(value = "api_code")
    private String apiCode;

    /**
     * 平台api名称
     */
    @TableField(value = "api_name")
    private String apiName;

    /**
     * 间隙时间
     */
    @TableField(value = "interval_time")
    private Integer intervalTime;

    /**
     * 上次执行时间
     */
    @TableField(value = "last_time")
    private LocalDateTime lastTime;

    /**
     * 下次执行时间
     */
    @TableField(value = "next_time")
    private LocalDateTime nextTime;

    /**
     * 任务状态：1：待拉取  2：拉取中
     */
    @TableField(value = "state")
    private Integer state;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除 FALSE 未删除 TRUE 已删除
     */
    @TableField(value = "is_deleted")
    private Boolean isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}