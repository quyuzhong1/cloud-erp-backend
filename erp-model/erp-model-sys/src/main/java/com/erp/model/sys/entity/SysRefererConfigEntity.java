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
 * 第三方系统配置
 * </p>
 *
 * @author shukai
 * @since 2024-05-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_referer_config")
public class SysRefererConfigEntity extends BaseEntity<SysRefererConfigEntity> {

    /**
    * 第三方系统
    */
    @TableField("app_id")
    private String appId;
    /**
    * 秘钥
    */
    @TableField("app_secret")
    private String appSecret;

    /**
    * 应用类型：飞书、PDA、微信小程序、ERP
    */
    @TableField("app_type")
    private String appType;

    /**
    * 逻辑类型 OLD 旧逻辑 NEW 优化的新逻辑  com.erp.model.sys.enums.LogicTypeEnum
    */
    @TableField("logic_type")
    private String logicType;

    /**
    * 非对称加密公钥（RSA公钥等）
    */
    @TableField("public_key")
    private String publicKey;

    /**
    * 非对称加密私钥（RSA私钥等）
    */
    @TableField("private_key")
    private String privateKey;

    /**
    * 对称加密算法：AES HMAC
    */
    @TableField("encrypt_algorithm")
    private String encryptAlgorithm;

    /**
    * 是否开启签名验证
    */
    @TableField("signature_disabled")
    private Boolean signatureDisabled;

    /**
    * 是否开启单点登录
    */
    @TableField("sso_disabled")
    private Boolean ssoDisabled;


    public static final String APP_ID = "app_id";

    public static final String APP_SECRET = "app_secret";

    public static final String APP_TYPE = "app_type";

    public static final String LOGIC_TYPE = "logic_type";

    public static final String PUBLIC_KEY = "public_key";

    public static final String PRIVATE_KEY = "private_key";

    public static final String ENCRYPT_ALGORITHM = "encrypt_algorithm";

    public static final String SIGNATURE_DISABLED = "signature_disabled";

    public static final String SSO_DISABLED = "sso_disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}