package com.sdk.oms.shopee.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 平台亚马逊订单DTO
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformShopeeOrderDTO extends CleanBaseDTO {

    private PlatformOrderDTO platformOrderDTO;

    public PlatformShopeeOrderDTO(PlatformOrderDTO platformOrderDTO, JobTaskDTO dto) {
        this.platformOrderDTO = platformOrderDTO;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.SHOPIFY.getCode());
        this.setUniqueId(platformOrderDTO.getUniqueId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(PlatformShopeeOrderDTO dto) {
        return dto.getPlatformOrderDTO();
    }

}
