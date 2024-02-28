package com.sdk.oms.mercado.dto;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.oms.mercado.dto.mercado.listing.AttributesBean;
import com.sdk.oms.mercado.dto.mercado.listing.ResultsBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformMercadoListingDTO extends CleanBaseDTO {


    private ResultsBean resultsBean;

    private String shopId;

    /**
     * 初始化
     */
    public PlatformMercadoListingDTO(ResultsBean resultsBean, JobTaskDTO dto) {
        this.resultsBean = resultsBean;
        this.setIsClean(0);
        this.shopId = dto.getShopId();
        super.setPlatform(PlatformDictEnum.WALMART.getCode());
        this.setUniqueId(resultsBean.getId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformProductDTO convertDTO(PlatformMercadoListingDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformWalmartListingDTO 转换 DTO
     */
    private static PlatformProductDTO initPlatformProductDTO(PlatformMercadoListingDTO dto) {
        ResultsBean resultsBean = dto.getResultsBean();

        PlatformProductDTO resultDto = new PlatformProductDTO();
        AttributesBean attributesBean = resultsBean.getAttributes().stream().filter(req -> "seller_sku".equals(req.getId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(attributesBean)) {

        }

        //平台sku
        resultDto.setPlatformSkuNo(attributesBean.getValueName());
        // 平台产品名称
        resultDto.setPlatformSkuName(attributesBean.getValueName());
        // 类型 platform 平台  warehouse 仓库
        resultDto.setPlatformType("platform");
        //产品规格
        resultDto.setProductSpec("");
        // 平台
        resultDto.setPlatform(dto.getPlatform());
        //平台产品id
        resultDto.setPlatformProductNo(resultsBean.getId());
        // 平台产品名称
        resultDto.setPlatformProductName(resultsBean.getName());
        //图片
        resultDto.setPlatformProductName(resultsBean.getPictures().get(0).getUrl());
        resultDto.setUniqueId(resultsBean.getId());
        resultDto.setShopId(dto.getShopId());
        resultDto.setPlatformUpdateTime(LocalDateTime.now());
        return resultDto;
    }
}
