package com.sdk.oms.shopee.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * 平台亚马逊产品DTO
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Data
@NoArgsConstructor
public class PlatformShopeeListingDTO extends CleanBaseDTO {
    //
    private PlatformProductDTO platformProductDTO;

    /**
     * 初始化
     */
    public PlatformShopeeListingDTO(PlatformProductDTO platformProductDTO, JobTaskDTO dto) {
        this.platformProductDTO = platformProductDTO;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.SHOPIFY.getCode());
        this.setUniqueId(platformProductDTO.getUniqueId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static List<PlatformProductDTO> convertDTO(PlatformShopeeListingDTO dto) {
        // 原商品信息
        PlatformProductDTO platformProductDTO = dto.getPlatformProductDTO();
        return initPlatformProductDTO(platformProductDTO, dto);
    }

    /**
     * 根据ShopifyVariant变体(SKU) 转换 DTO
     */
    private static List<PlatformProductDTO> initPlatformProductDTO(PlatformProductDTO platformProductDTO, PlatformShopeeListingDTO dto) {
        return Collections.emptyList();
    }

}
