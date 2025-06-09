package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 物流授权字段值表
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_auth_field")
public class LogisticsAuthFieldEntity extends BaseEntity<LogisticsAuthFieldEntity> {

    /**
    * 物流商授权表id
    */
    @TableField("logistics_auth_id")
    private String logisticsAuthId;
    /**
    * 字段
    */
    @TableField("field_code")
    private String fieldCode;
    /**
    * 字段值
    */
    @TableField("field_value")
    private String fieldValue;


    public static final String LOGISTICS_AUTH_ID = "logistics_auth_id";

    public static final String FIELD_CODE = "field_code";

    public static final String FIELD_VALUE = "field_value";

    @Override
    public Serializable pkVal() {
        return null;
    }

}