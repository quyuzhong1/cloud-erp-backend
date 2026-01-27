package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 费用项配置字段配置
 * </p>
 *
 * @author jack
 * @since 2026-01-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_logistics_cost_import_detail")
public class CfgLogisticsCostImportDetailEntity extends BaseEntity<CfgLogisticsCostImportDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 物流商抬头字段
    */
    @TableField("source_field")
    private String sourceField;
    /**
    * 物流商明细字段
    */
    @TableField("source_detail_field")
    private String sourceDetailField;
    /**
    * 是否唯一
    */
    @TableField("is_unique_key")
    private Boolean isUniqueKey;
    /**
    * 是否绝对值
    */
    @TableField("is_absolute_value")
    private Boolean isAbsoluteValue;
    /**
    * ERP字段id
    */
    @TableField("target_field_id")
    private String targetFieldId;
    /**
    * ERP字段
    */
    @TableField("target_field")
    private String targetField;
    /**
    * ERP字段名称
    */
    @TableField("target_field_name")
    private String targetFieldName;
    /**
    * ERP字段类型
    */
    @TableField("target_field_type")
    private String targetFieldType;
    @TableField(exist = false)
    private String targetFieldTypeName;

    @TableField("index")
    private Integer index;

    //映射下标
    @TableField(exist = false)
    private Integer mappingIndex;

    public static final String SOURCE_FIELD = "source_field";

    public static final String SOURCE_DETAIL_FIELD = "source_detail_field";

    public static final String IS_UNIQUE_KEY = "is_unique_key";

    public static final String IS_ABSOLUTE_VALUE = "is_absolute_value";

    public static final String TARGET_FIELD_ID = "target_field_id";

    public static final String TARGET_FIELD = "target_field";

    public static final String TARGET_FIELD_NAME = "target_field_name";

    public static final String TARGET_FIELD_TYPE = "target_field_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}