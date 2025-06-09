package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CfgDataArchivingDTO {

    /**
     * 归档的表名
     */
    private String tableName;

    /**
     * 归档前数据的全路径名
     */
    private String sourceFullPath;

    /**
     * 归档后数据的全路径名
     */
    private String archiveFullPath;

    /**
     * 归档频率
     */
    private String archiveFrequency;

    /**
     * 保留策略
     */
    private String retentionPolicy;

    /**
     * 关联sql 占位符用%s
     */
    private String refSql;

    /**
     * 是否禁用
     */
    private Boolean disabled;
}
