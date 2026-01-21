package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 费用项配置字段基础表
 * </p>
 *
 * @author jack
 * @since 2026-01-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_logistics_cost_import_field")
public class CfgLogisticsCostImportFieldEntity extends BaseEntity<CfgLogisticsCostImportFieldEntity> {

    /**
    * 系统分类
    */
    @TableField("sys_classify")
    private String sysClassify;
    /**
    * 配置单据
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 数据表
    */
    @TableField("table_name")
    private String tableName;
    /**
    * 数据表(中文 )
    */
    @TableField("table_cn_name")
    private String tableCnName;
    /**
    * class_path
    */
    @TableField("class_path")
    private String classPath;
    /**
    * 父id
    */
    @TableField("parent_id")
    private String parentId;
    /**
    * ERP字段
    */
    @TableField("field")
    private String field;
    /**
    * ERP字段名称
    */
    @TableField("field_name")
    private String fieldName;
    /**
    * ERP字段类型：api=API,excel=线下表格:  枚举：CfgLogisticsCostImportFieldFieldTypeEnum
    */
    @TableField("field_type")
    private String fieldType;


    public static final String SYS_CLASSIFY = "sys_classify";

    public static final String BUSSINESS_TYPE = "bussiness_type";

    public static final String TABLE_NAME = "table_name";

    public static final String TABLE_CN_NAME = "table_cn_name";

    public static final String CLASS_PATH = "class_path";

    public static final String PARENT_ID = "parent_id";

    public static final String FIELD = "field";

    public static final String FIELD_NAME = "field_name";

    public static final String FIELD_TYPE = "field_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
