package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * sku 对照表
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sku_maping")
public class SkuMapingEntity extends BaseEntity<SkuMapingEntity> {

    /**
     * 店铺表id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 平台字典值
     */
    @TableField("platform_dict")
    private String platformDict;

    /**
     * 平台名称
     */
    @TableField("platform_name")
    private String platformName;

    /**
     * 产品sku id
     */
    @TableField("product_sku_id")
    private String productSkuId;

    /**
     * 产品sku no
     */
    @TableField("product_sku_no")
    private String productSkuNo;

    /**
     * 平台sku no
     */
    @TableField("platform_sku_no")
    private String platformSkuNo;

    /**
     * 平台sku 名称
     */
    @TableField("platform_sku_name")
    private String platformSkuName;

    /**
     * 匹配结果
     */
    @TableField("match_result")
    private Boolean matchResult;

    /**
     * 生效日期
     */
    @TableField("effective_date")
    private Date effectiveDate;

    /**
     * 失效日期
     */
    @TableField("expire_date")
    private Date expireDate;


    public static final String SHOP_ID = "shop_id";

    public static final String PLATFORM_DICT = "platform_dict";

    public static final String PLATFORM_NAME = "platform_name";

    public static final String PRODUCT_SKU_ID = "product_sku_id";

    public static final String PRODUCT_SKU_NO = "product_sku_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PLATFORM_SKU_NAME = "platform_sku_name";

    public static final String MATCH_RESULT = "match_result";

    public static final String EFFECTIVE_DATE = "effective_date";

    public static final String EXPIRE_DATE = "expire_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
