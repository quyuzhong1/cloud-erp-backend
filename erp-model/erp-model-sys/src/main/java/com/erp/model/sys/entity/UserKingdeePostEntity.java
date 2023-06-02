package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Lambda
 * @since 2023-06-02
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("user_kingdee_post")
public class UserKingdeePostEntity extends BaseEntity<UserKingdeePostEntity> {

    /**
     * 金蝶 员工编码
     */
    @TableField("kingdee_user_code")
    private String kingdeeUserCode;

    /**
     * 本系统的员工id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 用户
     */
    @TableField("user_name")
    private String userName;

    /**
     * 金蝶用户编码
     */
    @TableField("kingdee_post_code")
    private String kingdeePostCode;

    /**
     * 岗位名称
     */
    @TableField("post_name")
    private String postName;

    /**
     * 使用组织
     */
    @TableField("use_org_id")
    private String useOrgId;

    /**
     * 使用组织名
     */
    @TableField("use_org_name")
    private String useOrgName;


    public static final String KINGDEE_USER_CODE = "kingdee_user_code";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String KINGDEE_POST_CODE = "kingdee_post_code";

    public static final String POST_NAME = "post_name";

    public static final String USE_ORG_ID = "use_org_id";

    public static final String USE_ORG_NAME = "use_org_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
