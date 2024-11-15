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
 * 金蝶岗位表
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kingdee_post")
public class KingdeePostEntity extends BaseEntity<KingdeePostEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 金蝶code
    */
    @TableField("code")
    private String code;
    /**
    * 金蝶部门表id  kingdee_department 表id
    */
    @TableField("kingdee_dept_id")
    private String kingdeeDeptId;
    /**
    * 使用组织id
    */
    @TableField("use_org_id")
    private String useOrgId;
    /**
    * 使用组织名称
    */
    @TableField("use_org_name")
    private String useOrgName;
    /**
    * 金蝶id
    */
    @TableField("kingdee_id")
    private String kingdeeId;
    /**
    * 启用禁用
    */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * erp岗位表id 对应 sys_post 表id
     */
    @TableField("erp_post_id")
    private String erpPostId;

    @TableField(exist = false)
    private String kingdeeDeptCode;

    @TableField(exist = false)
    private String useOrgCode;



    public static final String FIELD_NAME = "name";

    public static final String FIELD_CODE = "code";

    public static final String KINGDEE_DEPT_ID = "kingdee_dept_id";

    public static final String USE_ORG_ID = "use_org_id";

    public static final String USE_ORG_NAME = "use_org_name";

    public static final String KINGDEE_ID = "kingdee_id";

    public static final String FIELD_DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}