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
 * 补货建议主表
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("replenishment_suggestion")
public class ReplenishmentSuggestionEntity extends BaseEntity<ReplenishmentSuggestionEntity> {

    /**
     * 平台类型
     */
    @TableField("platform_type")
    private String platformType;

    /**
     * sku id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 国家
     */
    @TableField("country")
    private String country;

    /**
     * 店铺
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 区域
     */
    @TableField("area")
    private String area;

    /**
     * 平台
     */
    @TableField("platform")
    private String platform;

    /**
     * 是否补货,normal正常补货，notRestocking暂不补货
     */
    @TableField("replenishment_type")
    private String replenishmentType;

    /**
     * 补货原因
     */
    @TableField("replenishment_remark")
    private String replenishmentRemark;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String PLATFORM_TYPE = "platform_type";

    public static final String FAVORITE = "favorite";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String COUNTRY = "country";

    public static final String SHOP_ID = "shop_id";

    public static final String PLATFORM = "platform";

    public static final String BRAND_ID = "brand_id";

    public static final String CATEGORY_ID = "category_id";

    public static final String RESTOCK = "restock";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
