package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


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
    @TableField("referer")
    private String referer;
    /**
    * 秘钥
    */
    @TableField("secret_key")
    private String secretKey;


    public static final String REFERER = "referer";

    public static final String SECRET_KEY = "secret_key";

    @Override
    public Serializable pkVal() {
        return null;
    }

}