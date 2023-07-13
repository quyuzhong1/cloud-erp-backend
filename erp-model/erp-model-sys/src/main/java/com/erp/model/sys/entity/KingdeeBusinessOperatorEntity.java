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
 * 金蝶业务员
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("kingdee_business_operator")
public class KingdeeBusinessOperatorEntity extends BaseEntity<KingdeeBusinessOperatorEntity> {

    /**
     * 类型 seller  销售员 purchase 员 warehouseKeeper 仓管员
     */
    @TableField("type")
    private String type;

    /**
     * 金蝶类型id
     */
    @TableField("kingdee_type_id")
    private String kingdeeTypeId;

    /**
     * 组织code
     */
    @TableField("kingdee_org_code")
    private String kingdeeOrgCode;

    /**
     * 金蝶组织名
     */
    @TableField("kingdee_org_name")
    private String kingdeeOrgName;

    /**
     * 金蝶的业务员类型
     */
    @TableField("kingdee_type")
    private String kingdeeType;


    /**
     * 金蝶的业务员类型
     */
    @TableField("type_name")
    private String typeName;

    /**
     * 金蝶的的任刚编码
     */
    @TableField("kingdee_post_code")
    private String kingdeePostCode;

    /**
     * 金蝶的用户编码
     */
    @TableField("kingdee_user_code")
    private String kingdeeUserCode;

    /**
     * 金蝶的用户编码
     */
    @TableField("kingdee_user_id")
    private String kingdeeUserId;

    /**
     * 业务员名称
     */
    @TableField("kingdee_user_name")
    private String kingdeeUserName;

    /**
     * 用户id
     */
    @TableField("erp_user_id")
    private String erpUserId;

    /**
     * 是否禁用 true 禁用
     */
    @TableField("disabled")
    private Boolean disabled;


    public static final String TYPE = "type";

    public static final String KINGDEE_TYPE_ID = "kingdee_type_id";

    public static final String KINGDEE_ORG_CODE = "kingdee_org_code";

    public static final String KINGDEE_ORG_NAME = "kingdee_org_name";

    public static final String KINGDEE_TYPE = "kingdee_type";

    public static final String KINGDEE_POST_CODE = "kingdee_post_code";

    public static final String KINGDEE_USER_CODE = "kingdee_user_code";

    public static final String KING_USER_NAME = "king_user_name";

    public static final String ERP_USER_ID = "erp_user_id";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
