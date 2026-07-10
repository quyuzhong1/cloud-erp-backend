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
 * API Token 接口白名单配置
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_api_token_whitelist")
public class SysApiTokenWhitelistEntity extends BaseEntity<SysApiTokenWhitelistEntity> {

    /**
     * 白名单路径匹配模式，支持Ant路径匹配
     */
    @TableField("path_pattern")
    private String pathPattern;

    public static final String PATH_PATTERN = "path_pattern";

    @Override
    public Serializable pkVal() {
        return this.getId();
    }
}
