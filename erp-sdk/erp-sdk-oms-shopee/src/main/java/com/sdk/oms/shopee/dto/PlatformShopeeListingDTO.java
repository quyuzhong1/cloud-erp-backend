package com.sdk.oms.shopee.dto;

import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.oms.shopee.dto.product.response.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 平台亚马逊产品DTO
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Slf4j
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlatformShopeeListingDTO extends CleanBaseDTO {
    //
    private ItemInfo itemInfo;
    private String shopId;

    /**
     * 初始化
     */
    public PlatformShopeeListingDTO(ItemInfo itemInfo, JobTaskDTO dto) {
        this.itemInfo = itemInfo;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.SHOPEE.getCode());
        this.setUniqueId(itemInfo.getId() + "_" + dto.getShopId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
        this.shopId = dto.getShopId();
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformProductDTO convertDTO(PlatformShopeeListingDTO dto) {
        // 原商品信息
        ItemInfo itemInfo = dto.getItemInfo();
        if (Objects.isNull(itemInfo)) {
            return null;
        }
        Long updateTime = itemInfo.getUpdateTime();
        Instant instant = null;
        if(Objects.nonNull(updateTime)){
            instant = Instant.ofEpochSecond(updateTime);
        }
        ZoneId zone = ZoneId.systemDefault();
        Image image = itemInfo.getImage();
        String imageUrl = null;
        if (Objects.nonNull(image)) {
            List<String> urls = image.getUrls();
            if (CollectionUtils.isNotEmpty(urls)) {
                imageUrl = urls.get(0).toString();
            }
        }
        PlatformProductDTO productDTO = new PlatformProductDTO()
                // 类型 platform 平台  warehouse 仓库
                .setPlatformType("platform")
                // 平台spu no
                .setPlatformProductNo(String.valueOf(itemInfo.getId()))
                // 平台sku no
                .setPlatformSkuNo(itemInfo.getItemSku())
                //sku名称
                .setPlatformSkuName(itemInfo.getName())
                // 平台产品名称
                .setPlatformProductName(itemInfo.getName())
                //产品包装信息
                .setProductPacking(processDimension(itemInfo.getDimension(), itemInfo.getWeight()))
                //产品规格信息
                .setProductSpec(processProductSpec(itemInfo.getAttributes()))
                // 产品图片 url
                .setProductImageUrl(imageUrl)
                //店铺
                .setShopId(dto.getShopId());
        //平台最后修改时间
        if (Objects.nonNull(instant)){
            productDTO.setPlatformUpdateTime(LocalDateTime.ofInstant(instant, zone));
        }
        productDTO.setPlatform(PlatformDictEnum.SHOPEE.getCode());
        productDTO.setUniqueId(dto.getUniqueId());
        return productDTO;
    }

    public static String processProductSpec(List<Attribute> attributes) {
        if (CollectionUtils.isEmpty(attributes)) return "";
        StringBuffer stringBuffer = new StringBuffer();
        attributes.forEach(attribute -> {
            stringBuffer.append(attribute.getAttributeName()).append(":");
            List<AttributeValue> attributeValueList = attribute.getAttributeValueList();
            if (CollectionUtils.isNotEmpty(attributeValueList)) {
                attributeValueList.forEach(attributeValue -> {
                    stringBuffer.append(attributeValue.getValueName());
                });
                stringBuffer.append(";");
            }
        });
        return stringBuffer.toString();
    }

    /***
     *
     * @param dimension
     * @param weight  重量 kg
     * @return
     */
    public static String processDimension(Dimension dimension, String weight) {
        if (Objects.isNull(dimension) && StringUtils.isBlank(weight)) {
            return "";
        }
        return StrUtil.format("长度:{};宽度:{};高度:{};重量:{};", dimension.getPackageLength(), dimension.getPackageWidth(), dimension.getPackageHeight(), weight);
    }

    @Override
    public String toString() {
        return "PlatformShopeeListingDTO{" +
                "itemInfo=" + itemInfo +
                '}';
    }
}
