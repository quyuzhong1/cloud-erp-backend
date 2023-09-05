package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@TableName("sku_mapping")
public class SkuMappingEntity extends BaseEntity<SkuMappingEntity> {

    /**
     * 店铺表id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 平台字典值
     */
    @TableField("dict_platform")
    private String dictPlatform;

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
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * type
     */
    @TableField("type")
    private RuleTypeEnum type;

    /**
     * listing_id
     */
    @TableField("listing_id")
    private String listingId;


    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
     * 仓库名
     */
    @TableField("warehouse_name")
    private String warehouseName;


    /**
     * 生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;


    /**
     * 是否失效
     * true 失效
     * false 未失效
     */
    @TableField("is_expire")
    private Boolean isExpire;



    @Override
    public Serializable pkVal() {
        return null;
    }

}
