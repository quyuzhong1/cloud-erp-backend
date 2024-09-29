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
 * 金蝶业务员表
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kingdee_operator_ref_post")
public class KingdeeOperatorRefPostEntity extends BaseEntity<KingdeeOperatorRefPostEntity> {

    /**
    * 金蝶业务员类型表code kingdee_operator_type
    */
    @TableField("type_code")
    private String typeCode;

    /**
    * 金蝶员工任岗表id  kingdee_user_ref_post 表
    */
    @TableField("user_post_id")
    private String userPostId;

    @TableField("kingdee_id")
    private String kingdeeId;

    @TableField("use_org_id")
    private String useOrgId;


    @TableField("use_org_name")
    private String useOrgName;

    @TableField(exist = false)
    private String useOrgCode;


    public static final String TYPE_ID = "type_id";

    public static final String USER_POST_ID = "user_post_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}