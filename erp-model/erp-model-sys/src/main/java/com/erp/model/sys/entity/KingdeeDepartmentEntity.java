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
 * 
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kingdee_department")
public class KingdeeDepartmentEntity extends BaseEntity<KingdeeDepartmentEntity> {

    /**
    * 金蝶id
    */
    @TableField("kingdee_id")
    private String kingdeeId;
    /**
    * 金蝶code
    */
    @TableField("kingdee_dept_code")
    private String kingdeeDeptCode;
    /**
    * 金蝶部门名称
    */
    @TableField("kingdee_dept_name")
    private String kingdeeDeptName;
    /**
    * erp部门id
    */
    @TableField("erp_dept_id")
    private String erpDeptId;
    /**
    * 使用组织id
    */
    @TableField("use_org_id")
    private String useOrgId;

    @TableField("use_org_name")
    private String useOrgName;
    /**
    * 父级id 
    */
    @TableField("parent_id")
    private String parentId;


    /**
     * 父级金蝶code
     */
    @TableField("parent_kingdee_code")
    private String parentKingdeeCode;

    @TableField(exist = false)
    private String useOrgCode;




    public static final String KINGDEE_ID = "kingdee_id";

    public static final String KINGDEE_DEPT_CODE = "kingdee_dept_code";

    public static final String KINGDEE_DEPT_NAME = "kingdee_dept_name";

    public static final String ERP_DEPT_ID = "erp_dept_id";

    public static final String USE_ORG_ID = "use_org_id";

    public static final String PARENT_ID = "parent_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}