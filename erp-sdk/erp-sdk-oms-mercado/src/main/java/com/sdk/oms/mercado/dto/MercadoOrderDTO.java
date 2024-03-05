package com.sdk.oms.mercado.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class MercadoOrderDTO extends CleanBaseDTO {

    private OrderViewDTO orderBean;

    private String shopId;

    /**
     * 初始化
     */
    public MercadoOrderDTO(OrderViewDTO orderBean, JobTaskDTO dto, String shopId) {
        this.orderBean = orderBean;
        this.shopId = shopId;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.WALMART.getCode());
        this.setUniqueId(String.valueOf(orderBean.getId()));
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(OrderViewDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformWalmartListingDTO 转换 DTO
     */
    private static PlatformOrderDTO initPlatformProductDTO(OrderViewDTO dto) {

        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        return orderDTO;
    }

}
