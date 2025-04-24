package com.erp.model.scm.entity;

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
 * @since 2024-03-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kingdee_payment_condition")
public class KingdeePaymentConditionEntity extends BaseEntity<KingdeePaymentConditionEntity> {

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
    * false 禁用状态
    */
    @TableField("disabled")
    private Boolean disabled;
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


    public static final String KINGDEE_ID = "kingdee_id";

    public static final String KINGDEE_STATUS = "kingdee_status";

    public static final String FIELD_DISABLED = "disabled";

    public static final String FIELD_NAME = "name";

    public static final String FIELD_CODE = "code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}