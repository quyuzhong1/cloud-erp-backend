package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 归档配置表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_table_archive")
public class CfgTableArchiveEntity extends BaseEntity<CfgTableArchiveEntity> {

    /**
     * 业务名称
     */
    @TableField("biz_name")
    private String bizName;

    /**
     * 系统代码
     */
    @TableField("system_code")
    private String systemCode;

    /**
     * 表名
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 时间字段
     */
    @TableField("time_field")
    private String timeField;

    /**
     * 保留天数
     */
    @TableField("retention_day")
    private Integer retentionDay;

    /**
     * 一次限制条数
     */
    @TableField("limit_count")
    private Integer limitCount;

    /**
     * 扩展sql语句
     */
    @TableField("ext_sql")
    private String extSql;


    public static final String BIZ_NAME = "biz_name";

    public static final String SYSTEM_CODE = "system_code";

    public static final String TABLE_NAME = "table_name";

    public static final String TIME_FIELD = "time_field";

    public static final String RETENTION_DAY = "retention_day";

    public static final String LIMIT_COUNT = "limit_count";

    public static final String EXT_SQL = "ext_sql";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
