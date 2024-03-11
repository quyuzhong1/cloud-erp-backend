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


    public static final String NAME = "name";

    public static final String CODE = "code";

    public static final String KINGDEE_DEPT_ID = "kingdee_dept_id";

    public static final String USE_ORG_ID = "use_org_id";

    public static final String USE_ORG_NAME = "use_org_name";

    public static final String KINGDEE_ID = "kingdee_id";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}