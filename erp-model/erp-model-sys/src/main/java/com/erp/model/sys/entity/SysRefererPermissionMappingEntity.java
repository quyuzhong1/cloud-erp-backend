package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 第三方系统权限映射表
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_referer_permission_mapping")
public class SysRefererPermissionMappingEntity extends BaseEntity<SysRefererPermissionMappingEntity> {

    /**
     * 应用ID，关联sys_referer_config.app_id
     */
    @TableField("app_id")
    private String appId;

    /**
     * 服务名称
     */
    @TableField("service_name")
    private String serviceName;

    /**
     * 是否启用该权限
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 权限描述
     */
    @TableField("description")
    private String description;

    public static final String APP_ID = "app_id";

    public static final String SERVICE_NAME = "service_name";

    public static final String DISABLED = "disabled";

    public static final String DESCRIPTION = "description";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
