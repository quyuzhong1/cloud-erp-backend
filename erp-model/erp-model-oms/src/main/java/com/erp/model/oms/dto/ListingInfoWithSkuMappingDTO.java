package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ListingInfoWithSkuMappingDTO {

    /**
     * 店铺表id
     */
    private String shopId;

    /**
     * 平台字典值
     */
    private String dictPlatform;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 产品sku id
     */
    private String productSkuId;

    /**
     * 产品sku no
     */
    private String productSkuNo;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 类型
     * platform=平台
     * warehouse=仓库
     * {@link com.erp.model.oms.enums.RuleTypeEnum}
     */
    private String type;
    
    /**
     * 仓库id
     */
    private String warehouseId;
    /**
     * 仓库名
     */
    private String warehouseName;


    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否失效
     * true 失效
     * false 未失效
     */
    private Boolean isExpire;

    /**
     * 对照关系是否映射到改服务商所有仓库: f=否, t=是
     */
    private Boolean hasMappingAll;

    /**
     * listing_id
     */
    private String listingId;

    /**
     * 平台sku no
     */
    private String platformSkuNo;

    /**
     * 平台产品Sku名称
     */
    private String platformSkuName;

    /**
     * 平台产品(spu) no或id
     */
    private String platformSpuNo;

    /**
     * 平台产品SPU名称
     */
    private String platformSpuName;

    /**
     * 平台
     */
    private String platform;
    

    /**
     * 匹配结果吧true 已匹配 false 未匹配
     */
    private Boolean matchResult;

    /**
     * 产品图片 url
     */
    private String productImageUrl;

    /**
     * 产品规格信息
     */
    private String productSpec;

    /**
     * 产品包装信息
     */
    private String productPacking;

    /**
     * 平台最后修改时间
     */
    private LocalDateTime platformUpdateTime;

    public String checkAndGetProductSkuId() {
        if (StringUtils.isNotBlank(this.productSkuId)){
            return this.productSkuId;
        }
        return "";
    }

    public String checkAndGetProductSkuNo() {
        if (StringUtils.isNotBlank(this.productSkuNo)){
            return this.productSkuNo;
        }
        return "";
    }

    public String checkAndGetProductName() {
        if (StringUtils.isNotBlank(this.productName)){
            return this.productName;
        }
        return "";
    }

    public String checkAndGetProductImageUrl() {
        if (StringUtils.isNotBlank(this.productImageUrl)){
            return this.productImageUrl;
        }
        return "";
    }
}