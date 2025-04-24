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
 * 物流商授权字段配置表
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_logistics_auth_field")
public class CfgLogisticsAuthFieldEntity extends BaseEntity<CfgLogisticsAuthFieldEntity> {

    /**
    * 物流平台
    */
    @TableField("logistics_platform")
    private String logisticsPlatform;
    /**
    * 字段
    */
    @TableField("field_code")
    private String fieldCode;
    /**
    * 字段名
    */
    @TableField("field_name")
    private String fieldName;
    /**
    * 物流平台名
    */
    @TableField("logistics_platform_name")
    private String logisticsPlatformName;


    public static final String OGISTICS_PLATFORM = "ogistics_platform";

    public static final String FIELD_CODE = "field_code";

    public static final String FIELD_NAME = "field_name";

    public static final String OGISTICS_PLATFORM_NAME = "ogistics_platform_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}