package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Will
 * @description: API同步任务(失败后写入)
 * @date: 2023/1/11 11:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "api_sync_task")
public class ApiSyncTaskEntity extends BaseEntity<ApiSyncTaskEntity> {

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
     * 授权id
     */
    @TableField(value = "api_auth_id")
    private String apiAuthId;

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
     * 发送状态（1发送成功，2发送失败）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * API请求参数
     */
    @TableField(value = "request_param_json")
    private String requestParamJson;

    /**
     * 返回信息
     */
    @TableField(value = "msg")
    private String msg;

    /**
     * 请求次数(重试)
     */
    @TableField(value = "retry_count")
    private Integer retryCount;
}
