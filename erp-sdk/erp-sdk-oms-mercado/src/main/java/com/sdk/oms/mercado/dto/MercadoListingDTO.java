package com.sdk.oms.mercado.dto;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.oms.mercado.dto.mercado.listing.AttributesBean;
import com.sdk.oms.mercado.dto.mercado.listing.BodyBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MercadoListingDTO extends CleanBaseDTO {

    private BodyBean bodyBean;

    private String shopId;

    /**
     * 初始化
     */
    public MercadoListingDTO(BodyBean bodyBean, JobTaskDTO dto) {
        this.bodyBean = bodyBean;
        this.setIsClean(0);
        this.shopId = dto.getShopId();
        super.setPlatform(PlatformDictEnum.MERCADO.getCode());
        this.setUniqueId(bodyBean.getFid());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformProductDTO convertDTO(MercadoListingDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformWalmartListingDTO 转换 DTO
     */
    private static PlatformProductDTO initPlatformProductDTO(MercadoListingDTO dto) {
        BodyBean bodyBean = dto.getBodyBean();
        PlatformProductDTO resultDto = new PlatformProductDTO();
        AttributesBean attributesBean = bodyBean.getAttributes().stream().filter(req -> "seller_sku".equals(req.getFid())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(attributesBean)) {
            //平台sku
            resultDto.setPlatformSkuNo(attributesBean.getValueName());
            // 平台sku名称
            resultDto.setPlatformSkuName(bodyBean.getTitle());
        }
        // 类型 platform 平台  warehouse 仓库
        resultDto.setPlatformType("platform");
        //产品规格
        resultDto.setProductSpec("");
        // 平台
        resultDto.setPlatform(dto.getPlatform());
        //平台产品id
        resultDto.setPlatformProductNo(bodyBean.getFid());
        // 平台产品名称
        resultDto.setPlatformProductName(bodyBean.getTitle());
        //图片
        resultDto.setProductImageUrl(bodyBean.getPictures().get(0).getUrl());
        resultDto.setUniqueId(bodyBean.getFid());
        resultDto.setShopId(dto.getShopId());
        resultDto.setPlatformUpdateTime(LocalDateTime.now());
        return resultDto;
    }
}
