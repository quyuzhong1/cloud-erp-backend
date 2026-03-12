package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.PlatformOutboundDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.enums.B2BThirdDeliveryCancelResultEnum;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * B2B三方仓出库状态消息组装
 */
@Component
public class B2bThirdOutboundMqAssembler {

    public PlatformOutboundDTO toDto(B2bThirdDeliveryEntity entity, ThirdWarehouseQueryFbaOutboundResponse response, String providerCode) {
        if (Objects.isNull(entity) || Objects.isNull(response)) {
            return null;
        }
        String targetStatus = convertStatus(response.getStatus());
        if (CharSequenceUtil.isBlank(targetStatus)) {
            return null;
        }
        PlatformOutboundDTO dto = new PlatformOutboundDTO();
        dto.setUniqueId(entity.getCode());
        dto.setPlatform(providerCode);
        dto.setProvider(providerCode);
        dto.setReferenceNo(entity.getCode());
        dto.setOrderCode(response.getPlatformOrderCode());
        dto.setThirdOrderStatus(response.getStatus());
        dto.setOrderStatus(targetStatus);
        dto.setTrackNo(response.getTrackNo());
        dto.setAbnormalProblemReason(response.getErrorType());
        dto.setOutBoundTime(parseDateTime(response.getDeliveryTimeStr()));
        return dto;
    }

    private String convertStatus(String thirdOrderStatus) {
        B2BThirdDeliveryCancelResultEnum statusEnum = B2BThirdDeliveryCancelResultEnum.getByCode(thirdOrderStatus);
        if (Objects.isNull(statusEnum)) {
            return null;
        }
        switch (statusEnum) {
            case SUCCESS:
                return ThirdDeliveryStatusEnum.SHIPPED.getCode();
            case DISCARD:
            case PROBLEM:
                return ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode();
            case BLOCK:
            case DISCARD_PROCESSED:
                return ThirdDeliveryStatusEnum.INTERCEPTING.getCode();
            case EXCEPTION:
                return ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode();
            default:
                return null;
        }
    }

    private LocalDateTime parseDateTime(String deliveryTimeStr) {
        if (CharSequenceUtil.isBlank(deliveryTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
        }
        return null;
    }
}
