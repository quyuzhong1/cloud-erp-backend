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
 * 预估销量文件
 * </p>
 *
 * @author liao
 * @since 2025-02-19
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_rule_sales_estimate_file")
public class CfgRuleSalesEstimateFileEntity extends BaseEntity<CfgRuleSalesEstimateFileEntity> {

    /**
     * 销量表cfg_rule_sales_qty id id
     */
    @TableField("sales_qty_id")
    private String salesQtyId;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件地址
     */
    @TableField("file_url")
    private String fileUrl;


    public static final String SALES_QTY_ID = "sales_qty_id";

    public static final String FILE_NAME = "file_name";

    public static final String FILE_URL = "file_url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
