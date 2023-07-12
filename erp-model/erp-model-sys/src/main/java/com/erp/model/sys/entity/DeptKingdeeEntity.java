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
 * @since 2023-07-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("kingdee_dept")
public class DeptKingdeeEntity extends BaseEntity<DeptKingdeeEntity> {

    /**
     * 部门id
     */
    @TableField("dept_id")
    private String deptId;

    /**
     * 部门名称
     */
    @TableField("dept_name")
    private String deptName;

    /**
     * 金蝶部门code
     */
    @TableField("kingdee_dept_code")
    private String kingdeeDeptCode;

    /**
     * 金额部门名称
     */
    @TableField("kingdee_dept_name")
    private String kingdeeDeptName;

    /**
     * 使用组织(金蝶)
     */
    @TableField("use_org_code")
    private String useOrgCode;

    /**
     * 使用组织名(金蝶)
     */
    @TableField("use_org_name")
    private String useOrgName;

    /**
     * 组织id(对应自研系统核算公司id)
     */
    @TableField("use_org_id")
    private String useOrgId;


    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    public static final String KINGDEE_DEPT_CODE = "kingdee_dept_code";

    public static final String KINGDEE_DEPT_NAME = "kingdee_dept_name";

    public static final String USE_ORG_ID = "use_org_id";

    public static final String USE_ORG_NAME = "use_org_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
