package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 
 * </p>
 *
 * @author wtr
 * @since 2025-12-22
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("awd_outstock_detail")
public class AwdOutstockDetailEntity extends BaseEntity<AwdOutstockDetailEntity> {

    @TableField("main_Id")
    private String mainId;
    /**
    * 平台产品id
    */
    @TableField("asin")
    private String asin;
    /**
    * 平台sku
    */
    @TableField("msku")
    private String msku;
    @TableField("fnsku")
    private String fnsku;
    @TableField("sku_id")
    private String skuId;
    @TableField("sku_no")
    private String skuNo;
    @TableField("product_name")
    private String productName;
    /**
    * 发货数量
    */
    @TableField("qty")
    private Integer qty;


    public static final String MAIN_ID = "main_Id";

    public static final String ASIN = "asin";

    public static final String MSKU = "msku";

    public static final String FNSKU = "fnsku";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String QTY = "qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}