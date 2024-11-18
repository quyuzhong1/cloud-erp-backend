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
@TableName("kingdee_operator_type")
public class KingdeeOperatorTypeEntity extends BaseEntity<KingdeeOperatorTypeEntity> {


    /**
    * 金蝶code
    */
    @TableField("code")
    private String code;
    /**
    * 金蝶name
    */
    @TableField("name")
    private String name;


    public static final String KINGDEE_ID = "kingdee_id";

    public static final String FIELD_CODE = "code";

    public static final String FIELD_NAME = "name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}