package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 个人访问令牌
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_api_token")
public class SysApiTokenEntity extends BaseEntity<SysApiTokenEntity> {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 令牌名称
     */
    @TableField("token_name")
    private String tokenName;

    /**
     * 令牌SHA-256哈希，用于认证查询
     */
    @TableField("token_hash")
    private String tokenHash;

    /**
     * AES-GCM加密后的完整令牌，仅复制时解密
     */
    @TableField("encrypted_token")
    private String encryptedToken;

    /**
     * 令牌预览前缀，用于列表脱敏展示
     */
    @TableField("token_preview_prefix")
    private String tokenPreviewPrefix;

    /**
     * 令牌预览后缀，用于列表脱敏展示
     */
    @TableField("token_preview_suffix")
    private String tokenPreviewSuffix;

    /**
     * 令牌过期时间，为空表示永不过期
     */
    @TableField("expires_time")
    private LocalDateTime expiresTime;

    public static final String USER_ID = "user_id";

    public static final String TOKEN_HASH = "token_hash";

    @Override
    public Serializable pkVal() {
        return this.getId();
    }
}
