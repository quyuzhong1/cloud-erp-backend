package com.erp.server.wms.convert;

import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.SoB2cProcessingDTO;
import com.erp.model.wms.entity.SoB2cProcessingEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyPrintSkuReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * b2c虚拟仓订单
 * @author will
 * @date 2025/1/3 9:10
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface SoB2cProcessingConverter {
    SoB2cProcessingConverter INSTANCE = Mappers.getMapper(SoB2cProcessingConverter.class);

    SoB2cProcessingDTO.AddOrUpdateDTO entityToAdd(SoB2cProcessingEntity entity);

    SoB2cProcessingEntity addToEntity(SoB2cProcessingDTO.AddOrUpdateDTO addOrUpdateDTO);

    @Mapping(target = "quantity", source = "qty")
    @Mapping(target = "code", source = "platformSpuNo")
    TikTokFullyPrintSkuReq.PlatformSkuItemsDTO convertToPlatformSkuItemsDTO(SoB2cDeliveryDTO.PrintSkuBarcodeDTO printSkuBarcodeDTO);
    List<TikTokFullyPrintSkuReq.PlatformSkuItemsDTO> convertToPlatformSkuItemsDTO(List<SoB2cDeliveryDTO.PrintSkuBarcodeDTO> printSkuBarcodeDTOList);
}
