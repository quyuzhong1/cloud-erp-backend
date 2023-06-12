package com.erp.model.plm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("product_customs")
public class ProductCustomsEntity extends BaseEntity<ProductCustomsEntity> {


    /**
    * sku
    */
    @TableField("sku_id")
    private String skuId;

    /**
    * 国家
    */
    @TableField("country")
    private String country;

    /**
    * 海关编码
    */
    @TableField("customs_code")
    private String customsCode;

    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
    * 海关类型：出关 exitCustoms 清关 clearanceCustoms 默认：清关
    */
    @TableField("type")
    private String type;


    public static final String SKU_ID = "sku_id";

    public static final String COUNTRY = "country";

    public static final String CUSTOMS_CODE = "customs_code";

    public static final String TAX_RATE = "tax_rate";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}