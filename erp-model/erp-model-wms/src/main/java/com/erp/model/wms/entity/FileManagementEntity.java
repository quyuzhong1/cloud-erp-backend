package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 文件管理
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("file_management")
public class FileManagementEntity extends BaseEntity<FileManagementEntity> {

    /**
    * 单据编码(WDGL开头)
    */
    @TableField("code")
    private String code;
    /**
    * skuid
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 文件类型
    */
    @TableField("file_type")
    private String fileType;
    /**
    * 一级品类id
    */
    @TableField("first_category_id")
    private String firstCategoryId;
    /**
    * 一级品类名称
    */
    @TableField("first_category_name")
    private String firstCategoryName;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 备注
    */
    @TableField("remark")
    private Integer remark;
    /**
    * 文件id
    */
    @TableField("file_id")
    private String fileId;


    public static final String CODE = "code";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String FILE_TYPE = "file_type";

    public static final String first_category_id = "first_category_id";

    public static final String first_category_name = "first_category_name";

    public static final String PRODUCT_NAME = "product_name";

    public static final String REMARK = "remark";
    @Override
    public Serializable pkVal() {
        return null;
    }

}