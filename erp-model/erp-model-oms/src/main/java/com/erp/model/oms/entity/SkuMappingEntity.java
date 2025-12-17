package com.erp.model.oms.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.enums.RuleTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
@NoArgsConstructor
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

    /**
     * 对照关系是否映射到改服务商所有仓库: f=否, t=是
     */
    @TableField("has_mapping_all")
    private Boolean hasMappingAll;

    /**
     * 匹配规则id
     */
    @TableField("rule_id")
    private String ruleId;



    public SkuMappingEntity(ListingInfoEntity entity, String shopId) {
        PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(entity.getPlatform());
        if (null == platformDictEnum){
            String msg = StrUtil.format("【listing消费】未找到对应平台枚举：Platform={}, UniqueId={}", entity.getPlatform(), entity.getPlatformSkuNo());
            throw new ServiceException(msg);
        }
        RuleTypeEnum ruleTypeEnum = RuleTypeEnum.getByCode(entity.getType());
        if (null == ruleTypeEnum){
            String msg = StrUtil.format("【listing消费】未找到对应type枚举：type={}, UniqueId={}", entity.getType(), entity.getPlatformSkuNo());
            throw new ServiceException(msg);
        }
        this.shopId = shopId;
        this.dictPlatform = platformDictEnum.getCode();
        this.platformName = platformDictEnum.getDesc();
        this.productSkuId = "";
        this.productSkuNo = "";
        this.productName = "";
        this.type = ruleTypeEnum;
        this.listingId = entity.getId();
        this.warehouseId = "";
        this.warehouseName = "";
        this.effectiveTime = LocalDateTime.now(ZoneId.systemDefault()).minusYears(MathUtil.NUMBER_100);
        this.expireTime = LocalDateTime.now(ZoneId.systemDefault()).plusYears(MathUtil.NUMBER_100);
        this.isExpire = false;
    }
}
