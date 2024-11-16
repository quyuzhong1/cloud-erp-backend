package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 历史导入记录
 * </p>
 *
 * @author will
 * @since 2024-08-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("history_import_record")
public class HistoryImportRecordEntity extends BaseEntity<HistoryImportRecordEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 类型
    */
    @TableField("type")
    private String type;
    /**
    * 模块，SourceTypeEnum枚举
    */
    @TableField("module")
    private String module;
    /**
    * fastdfs文件url
    */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 业务id
     */
    @TableField("business_id")
    private String businessId;

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String MODULE = "module";

    public static final String FILE_URL = "file_url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}