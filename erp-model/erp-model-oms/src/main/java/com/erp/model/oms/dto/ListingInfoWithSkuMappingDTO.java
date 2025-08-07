package com.erp.model.oms.dto;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
public class ListingInfoWithSkuMappingDTO {

    /**
     * skuMapping的ID
     */
    private String tableId;

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

    private String authId;
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
    private String matchResult;

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
     * fnSku
     */
    private String platformFnSku;
    /**
     * 平台SKUID
     */
    private String platformSkuId;
    /**
     * 平台最后修改时间
     */
    private LocalDateTime platformUpdateTime;

    /**
     * 平台状态
     */
    private String platformStatus;

    /**
     * ture:父产品
     */
    private Boolean isParent;

    /**
     * 仓库发货配置map
     * key:
     * 仓库经营类型:selfBuild=自建,thirdParty=第三方
     * {@link com.erp.model.wms.enums.WarehouseManageTypeEnum}
     * value:
     * 发货类型:single=子件发货,combine=捆绑Sku发货
     * {@link com.erp.model.wms.enums.WarehouseDeliveryTypeEnum}
     *
     */
    private Map<String, String> extendMap = new HashMap<>();

    /**
     * 过滤取指定过期时间匹配小于或等于过期时间/空=最新匹配
     */
    public static ListingInfoWithSkuMappingDTO getActiveOne(@NotNull List<ListingInfoWithSkuMappingDTO> dtoList,
                                                            @NotNull LocalDateTime lastExpireDate) {
        if (1 == dtoList.size()){
            return dtoList.get(0);
        }
        if (null == lastExpireDate){
            // 无指定日期提供最新映射关系
           return dtoList.stream()
                   .filter(e-> !e.getIsExpire())
                   .max(Comparator.comparing(ListingInfoWithSkuMappingDTO::getExpireTime))
                   .orElse(null);
        }

        Optional<ListingInfoWithSkuMappingDTO> optional = dtoList.stream()
                // 指定时间=生效时间 或 生效时间 < 指定时间 < 结束时间
                .filter(dto -> lastExpireDate.isEqual(dto.getEffectiveTime()) || (lastExpireDate.isAfter(dto.getEffectiveTime()) && lastExpireDate.isBefore(dto.getExpireTime())))
                .max(Comparator.comparing(ListingInfoWithSkuMappingDTO::getExpireTime));

        return optional.orElseGet(() -> dtoList
                .stream()
                .max(Comparator.comparing(ListingInfoWithSkuMappingDTO::getExpireTime))
                .orElse(null));
    }

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

    public String checkAndGetProductSpuNo() {
        if (StringUtils.isNotBlank(this.platformSpuNo)){
            return this.platformSpuNo;
        }
        return "";
    }
}