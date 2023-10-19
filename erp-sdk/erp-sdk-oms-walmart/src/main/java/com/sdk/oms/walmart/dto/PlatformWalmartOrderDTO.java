package com.sdk.oms.walmart.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.oms.walmart.dto.walmart.item.ItemResponseBean;
import com.sdk.oms.walmart.dto.walmart.order.OrderBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformWalmartOrderDTO extends CleanBaseDTO {

    private OrderBean orderBean;

    /**
     * 初始化
     */
    public PlatformWalmartOrderDTO(OrderBean orderBean, JobTaskDTO dto) {
        this.orderBean = orderBean;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.SHOPIFY.getCode());
        this.setUniqueId(orderBean.getCustomerOrderId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(PlatformWalmartOrderDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformWalmartListingDTO 转换 DTO
     */
    private static PlatformOrderDTO initPlatformProductDTO(PlatformWalmartOrderDTO dto) {

        PlatformOrderDTO orderDTO = new PlatformOrderDTO();

        return orderDTO;
    }

}
