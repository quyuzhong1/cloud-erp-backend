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
 * 月结文件解析配置主表。
 *
 * @author jack
 * @since 2026-06-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_file_parse")
public class CfgFileParseEntity extends BaseEntity<CfgFileParseEntity> {

    /**
     * 配置编码。
     */
    @TableField("code")
    private String code;
    /**
     * 任务名称。
     */
    @TableField("name")
    private String name;
    /**
     * 清洗时间维度，monthly=每月。
     */
    @TableField("period_type")
    private String periodType;
    /**
     * 清洗仓库编码。
     */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
     * 清洗仓库名称。
     */
    @TableField("dict_platform_name")
    private String dictPlatformName;
    /**
     * 文件夹类型。
     */
    @TableField("folder_type")
    private String folderType;
    /**
     * 是否停用，false=启用，true=停用。
     */
    @TableField("disabled")
    private Boolean disabled;
    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;

    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String PERIOD_TYPE = "period_type";
    public static final String DICT_PLATFORM = "dict_platform";
    public static final String DICT_PLATFORM_NAME = "dict_platform_name";
    public static final String FOLDER_TYPE = "folder_type";
    public static final String DISABLED = "disabled";
    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }
}
