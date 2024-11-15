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
 * PDA用户跳过版本升级记录表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pda_user_skip_version")
public class PdaUserSkipVersionEntity extends BaseEntity<PdaUserSkipVersionEntity> {

    /**
    * 用户id
    */
    @TableField("user_id")
    private String userId;
    /**
    * 用户名称
    */
    @TableField("user_name")
    private String userName;
    /**
    * 版本id
    */
    @TableField("version_id")
    private String versionId;


    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String VERSION_ID = "version_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}