package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * @description: API同步任务(失败后写入)
 * @author Will
 * @date: 2023/1/11 11:29
 */
@Data
@TableName(value ="api_sync_task")
public class ApiSyncTaskEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 平台名称
     */
    @TableField(value = "api_platform")
    private String apiPlatform;

    /**
     * 平台id
     */
    @TableField(value = "api_platform_id")
    private String apiPlatformId;

    /**
     * 模块类型 ApiModuleTypeEnum枚举,（0产品信息，1BOM管理）
     */
    @TableField(value = "module_type")
    private Integer moduleType;

    /**
     * 业务id(模块数据对应主键id)
     */
    @TableField(value = "business_id")
    private String businessId;

    /**
     * API请求参数
     */
    @TableField(value = "request_param_json")
    private String requestParamJson;

    /**
     * 请求次数(重试)
     */
    @TableField(value = "retry_count")
    private Integer retryCount;
}
