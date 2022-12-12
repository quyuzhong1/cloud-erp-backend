package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName platform_api
 */
@TableName(value ="platform_api")
@Data
public class PlatformApiEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id")
    private Integer id;

    /**
     * 平台表id
     */
    @TableField(value = "platform_id")
    private Integer platformId;

    /**
     * 平台api
     */
    @TableField(value = "api_code")
    private String apiCode;

    /**
     * 平台api名称
     */
    @TableField(value = "api_name")
    private String apiName;

    /**
     * 是否生成了任务 1：已生成 0 ：未生成
     */
    @TableField(value = "is_finished_task")
    private Integer isFinishedTask;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}