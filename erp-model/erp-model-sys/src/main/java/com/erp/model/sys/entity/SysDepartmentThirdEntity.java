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
 * 第三方 部门信息
 * </p>
 *
 * @author jack
 * @since 2025-05-15
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_department_third")
public class SysDepartmentThirdEntity extends BaseEntity<SysDepartmentThirdEntity> {

    /**
    * erp部门id
    */
    @TableField("dept_id")
    private String deptId;
    /**
    * 第三方平台类型
    */
    @TableField("platform")
    private String platform;
    /**
    * 第三方部门open_dept_id
    */
    @TableField("third_open_dept_id")
    private String thirdOpenDeptId;
    /**
    * 第三方部门自定义的dept_id
    */
    @TableField("third_dept_id")
    private String thirdDeptId;
    /**
    * 第三方部门department_name
    */
    @TableField("third_department_name")
    private String thirdDepartmentName;
    /**
    * 第三方父级open_dept_id
    */
    @TableField("third_parent_open_dept_id")
    private String thirdParentOpenDeptId;


    public static final String DEPT_ID = "dept_id";

    public static final String PLATFORM = "platform";

    public static final String THIRD_OPEN_DEPT_ID = "third_open_dept_id";

    public static final String THIRD_DEPT_ID = "third_dept_id";

    public static final String THIRD_DEPARTMENT_NAME = "third_department_name";

    public static final String THIRD_PARENT_OPEN_DEPT_ID = "third_parent_open_dept_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}