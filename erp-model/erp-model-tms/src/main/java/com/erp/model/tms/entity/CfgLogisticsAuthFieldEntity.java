package com.erp.model.tms.entity;

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
    @TableField("ogistics_platform")
    private String ogisticsPlatform;
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
    @TableField("ogistics_platform_name")
    private String ogisticsPlatformName;


    public static final String OGISTICS_PLATFORM = "ogistics_platform";

    public static final String FIELD_CODE = "field_code";

    public static final String FIELD_NAME = "field_name";

    public static final String OGISTICS_PLATFORM_NAME = "ogistics_platform_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}