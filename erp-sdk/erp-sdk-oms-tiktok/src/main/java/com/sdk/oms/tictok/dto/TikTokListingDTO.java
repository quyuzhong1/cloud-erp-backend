package com.sdk.oms.tictok.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.oms.tictok.dto.tiktok.listing.view.DataBean;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class TikTokListingDTO extends CleanBaseDTO {
    private DataBean dataBean;

    private String shopId;

    /**
     * 初始化
     */
    public TikTokListingDTO(DataBean dataBean, JobTaskDTO dto) {
        this.dataBean = dataBean;
        this.setIsClean(0);
        this.shopId = dto.getShopId();
        super.setPlatform(PlatformDictEnum.MERCADOLIBRE.getCode());
        this.setUniqueId(dataBean.getFid());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformProductDTO convertDTO(TikTokListingDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformWalmartListingDTO 转换 DTO
     */
    private static PlatformProductDTO initPlatformProductDTO(TikTokListingDTO dto) {

        return null;
    }
}
