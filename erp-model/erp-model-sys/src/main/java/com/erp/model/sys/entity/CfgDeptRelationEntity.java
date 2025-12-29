package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 部门关联表
 * </p>
 *
 * @author lrp
 * @since 2025-12-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_dept_relation")
public class CfgDeptRelationEntity extends BaseEntity<CfgDeptRelationEntity> {

    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 部门id
    */
    @TableField("dept_id")
    private String deptId;
    /**
    * 军区id
    */
    @TableField("partition_id")
    private String partitionId;
    /**
    * 平台编码
    */
    @TableField("dict_platform")
    private String dictPlatform;


    public static final String DISABLED = "disabled";

    public static final String DEPT_ID = "dept_id";

    public static final String PARTITION_ID = "partition_id";

    public static final String DICT_PLATFORM = "dict_platform";

    @Override
    public Serializable pkVal() {
        return null;
    }

}