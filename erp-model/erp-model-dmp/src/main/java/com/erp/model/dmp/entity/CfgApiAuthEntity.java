package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Will
 * @description: API授权信息表
 * @date: 2023/1/11 11:23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "cfg_api_auth")
public class CfgApiAuthEntity extends BaseEntity<CfgApiAuthEntity> {

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
     * 组别,默认default
     */
    @TableField(value = "api_group")
    private String apiGroup;

    /**
     * 键 (英文描述)
     */
    @TableField(value = "key")
    private String key;

    /**
     * 授权信息（存json字符串）
     */
    @TableField(value = "value")
    private String value;

}
