package com.erp.sdk.oms.amz.spapi.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.csv.ReportListingCsvEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 平台亚马逊产品DTO
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformAmazonListingDTO extends CleanBaseDTO {

    private ReportListingCsvEntity reportEntity;

    private String shopId;

    public PlatformAmazonListingDTO(ReportListingCsvEntity reportEntity, ShopInfoEntity shopInfoEntity) {
        this.reportEntity = reportEntity;
        this.shopId = shopInfoEntity.getId();
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformProductDTO convertDTO(PlatformAmazonListingDTO dto) {
        // 原商品信息
        ReportListingCsvEntity sourceEntity = dto.getReportEntity();

        // 图片默认
        String imageUrl = sourceEntity.getImageUrl();
        // 时间
        LocalDateTime openDate = LocalDateTime.parse(sourceEntity.getOpenDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        PlatformProductDTO resultDto = new PlatformProductDTO()
                // 平台spu no
                .setPlatformProductNo(sourceEntity.getProductId())
                // 平台sku no
                .setPlatformSkuNo(sourceEntity.getSellerSku())
                // 平台产品名称
                .setPlatformProductName(sourceEntity.getItemName())
                // 类型 platform 平台  warehouse 仓库
                .setPlatformType("platform")
                // 产品图片 url
                .setProductImageUrl(imageUrl)
                // 平台最后修改时间
                .setPlatformUpdateTime(DateUtil.utcSamePlus8(openDate))
                ;

        // 平台
        resultDto.setPlatform(dto.getPlatform());
        resultDto.setUniqueId(sourceEntity.getListingId());
        return resultDto;
    }
}
