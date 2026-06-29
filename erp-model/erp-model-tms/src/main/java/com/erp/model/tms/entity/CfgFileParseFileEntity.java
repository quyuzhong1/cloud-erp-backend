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
 * 月结文件解析配置文件识别规则子表。
 *
 * @author jack
 * @since 2026-06-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_file_parse_file")
public class CfgFileParseFileEntity extends BaseEntity<CfgFileParseFileEntity> {

    /**
     * 月结文件解析配置主表 ID。
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 单据类型。
     */
    @TableField("business_type")
    private String businessType;
    /**
     * 数据来源，api=API，excel=Excel。
     */
    @TableField("type")
    private String type;
    /**
     * 识别名称，Excel 表格名称或 API 名称。
     */
    @TableField("file_keyword")
    private String fileKeyword;
    /**
     * Excel sheet 名称，API 时可为空。
     */
    @TableField("sheet_name")
    private String sheetName;
    /**
     * 默认开始行，API 时为 0。
     */
    @TableField("header_row")
    private Integer headerRow;
    /**
     * 排序号。
     */
    @TableField("sort")
    private Integer sort;

    public static final String MAIN_ID = "main_id";
    public static final String BUSINESS_TYPE = "business_type";
    public static final String TYPE = "type";
    public static final String FILE_KEYWORD = "file_keyword";
    public static final String SHEET_NAME = "sheet_name";
    public static final String HEADER_ROW = "header_row";
    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }
}
