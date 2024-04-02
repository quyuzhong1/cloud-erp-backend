package com.sdk.oms.tictok.dto;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class TikTokOrderDTO extends CleanBaseDTO {

    private String test;

    private String shopId;

    /**
     * 初始化
     */
    public TikTokOrderDTO(String test, JobTaskDTO dto, String shopId) {
        this.test = test;
        this.shopId = shopId;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.MERCADOLIBRE.getCode());
        this.setUniqueId(combineUnique(test, this.shopId));
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    public static String combineUnique(String orderId, String shopId){
        return StrUtil.format("{}_{}", orderId, shopId);
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(TikTokOrderDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformMercadoListingDTO 转换 DTO
     */
    private static PlatformOrderDTO initPlatformProductDTO(TikTokOrderDTO dto) {
        return null;
    }
}
