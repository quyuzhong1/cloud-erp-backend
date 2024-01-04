package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.util.Map;

/**
 * 平台api
 * @TableName platform_api
 */
@TableName(value ="platform_api")
@Data
public class PlatformApiEntity extends BaseEntity<PlatformApiEntity> {

    /**
     * 平台表id
     */
    @TableField(value = "dict_platform")
    private String dictPlatform;

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
     * api共用参数
     */
    @TableField(value = "api_common_param", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> apiCommonParam;

    /**
     * 单据类型
     */
    @TableField(value = "bill_type")
    private String billType;

    /**
     * 操作类型
     */
    @TableField(value = "operate_type")
    private String operateType;

    /**
     * 禁用状态：false：启用 true：禁用
     */
    @TableField(value = "disabled")
    private Boolean disabled;

    /**
     * 间隔时间
     */
    @TableField(value = "interval_time")
    private Integer intervalTime;

    /**
     * 超时时间:单位秒:默认3600
     */
    @TableField(value = "timeout_seconds")
    private Integer timeoutSeconds;

    /**
     * 同步操作
     */
    @TableField(value = "sync_operate")
    private String syncOperate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}