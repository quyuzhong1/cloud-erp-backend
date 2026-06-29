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
 * 月结文件解析配置文件夹映射子表。
 *
 * @author jack
 * @since 2026-06-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_file_parse_folder")
public class CfgFileParseFolderEntity extends BaseEntity<CfgFileParseFolderEntity> {

    /**
     * 月结文件解析配置主表 ID。
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 账号类型：thirdWarehouse=三方仓账号，shop=店铺。
     */
    @TableField("account_type")
    private String accountType;
    /**
     * 账号或店铺 ID。
     */
    @TableField("account_id")
    private String accountId;
    /**
     * 账号或店铺编码。
     */
    @TableField("account_code")
    private String accountCode;
    /**
     * 账号或店铺名称。
     */
    @TableField("account_name")
    private String accountName;
    /**
     * 排序号。
     */
    @TableField("sort")
    private Integer sort;

    public static final String MAIN_ID = "main_id";
    public static final String ACCOUNT_TYPE = "account_type";
    public static final String ACCOUNT_ID = "account_id";
    public static final String ACCOUNT_CODE = "account_code";
    public static final String ACCOUNT_NAME = "account_name";
    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }
}
