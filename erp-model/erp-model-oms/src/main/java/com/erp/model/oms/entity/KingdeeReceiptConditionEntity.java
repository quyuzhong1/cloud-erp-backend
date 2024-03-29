package com.erp.model.oms.entity;

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
 * 金蝶收款条件
 * </p>
 *
 * @author Lambda
 * @since 2024-03-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kingdee_receipt_condition")
public class KingdeeReceiptConditionEntity extends BaseEntity<KingdeeReceiptConditionEntity> {

    /**
    * 金蝶id
    */
    @TableField("kingdee_id")
    private String kingdeeId;
    /**
    * 金蝶状态
    */
    @TableField("kingdee_status")
    private String kingdeeStatus;
    /**
    * false
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 金蝶名称
    */
    @TableField("name")
    private String name;
    /**
    * 金蝶code
    */
    @TableField("code")
    private String code;


    public static final String KINGDEE_ID = "kingdee_id";

    public static final String KINGDEE_STATUS = "kingdee_status";

    public static final String DISABLED = "disabled";

    public static final String NAME = "name";

    public static final String CODE = "code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}