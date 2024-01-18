package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 亚马逊报告字段配置
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_amz_report_field")
public class CfgAmzReportFieldEntity extends BaseEntity<CfgAmzReportFieldEntity> {

    /**
    * 备注 需要的时候 用到
    */
    @TableField("remark")
    private String remark;
    /**
    * 报告列表名称
    */
    @TableField("column_name")
    private String columnName;
    /**
    * 本地报告字段名称
    */
    @TableField("field_name")
    private String fieldName;
    /**
    * 报告类型
    */
    @TableField("report_type")
    private String reportType;
    /**
    * 启用状态
    */
    @TableField("status")
    private Boolean status;


    public static final String REMARK = "remark";

    public static final String COLUMN_NAME = "column_name";

    public static final String FIELD_NAME = "field_name";

    public static final String REPORT_TYPE = "report_type";

    public static final String STATUS = "status";

}