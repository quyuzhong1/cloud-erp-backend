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
 * 金蝶员工任岗表
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kingdee_user_ref_post")
public class KingdeeUserRefPostEntity extends BaseEntity<KingdeeUserRefPostEntity> {

    /**
    * erp 员工id
    */
    @TableField("erp_user_id")
    private String erpUserId;

    /**
    * 金蝶岗位表id kingdee_post 表
    */
    @TableField("kingdee_post_id")
    private String kingdeePostId;

    /**
     * 金蝶id
     */
    @TableField("kingdee_id")
    private String kingdeeId;

    /**
     * 金蝶code
     */
    @TableField("code")
    private String code;

    /**
     * 使用组织id
     */
    @TableField("use_org_id")
    private String useOrgId;

    /**
     * 使用组织名
     */
    @TableField("use_org_name")
    private String useOrgName;


    @TableField(exist = false)
    private String useOrgCode;

    /**
     * 金蝶部门id
     */
    @TableField("kingdee_department_id")
    private String kingdeeDepartmentId;


    public static final String ERP_USER_ID = "erp_user_id";

    public static final String KINGDEE_POST_ID = "kingdee_post_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}