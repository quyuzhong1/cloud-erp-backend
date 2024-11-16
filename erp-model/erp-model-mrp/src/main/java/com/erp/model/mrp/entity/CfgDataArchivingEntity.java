package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 归档配置
 * </p>
 *
 * @author liaohui
 * @since 2024-09-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_data_archiving")
public class CfgDataArchivingEntity extends BaseEntity<CfgDataArchivingEntity> {

    /**
     * 归档的表名
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 归档前数据的全路径名
     */
    @TableField("source_full_path")
    private String sourceFullPath;

    /**
     * 归档后数据的全路径名
     */
    @TableField("archive_full_path")
    private String archiveFullPath;

    /**
     * 归档频率
     */
    @TableField("archive_frequency")
    private String archiveFrequency;

    /**
     * 保留策略
     */
    @TableField("retention_policy")
    private String retentionPolicy;

    @TableField("ref_sql")
    private String refSql;

    /**
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;


    public static final String TABLE_NAME = "table_name";

    public static final String SOURCE_FULL_PATH = "source_full_path";

    public static final String ARCHIVE_FULL_PATH = "archive_full_path";

    public static final String ARCHIVE_FREQUENCY = "archive_frequency";

    public static final String RETENTION_POLICY = "retention_policy";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
