package com.erp.server.oms.convert;

import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.dto.SoMultiChannelDetailDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface SoMultiChannelConverter {

    SoMultiChannelConverter INSTANCE = Mappers.getMapper(SoMultiChannelConverter.class);

    @Mappings({
            @Mapping(target = "abnormalType", ignore = true),
            @Mapping(target = "billStatus", ignore = true),
            @Mapping(target = "deliveryCode", ignore = true),
            @Mapping(target = "shopId", source = "shopInfoEntity.id"),
            @Mapping(target = "shopName", source = "shopInfoEntity.name"),
            @Mapping(target = "deliveryStatus", ignore = true),
            @Mapping(target = "deliveryTime", ignore = true),
            @Mapping(target = "hasOutstock", constant = "false"),
            @Mapping(target = "platformCode", source = "soB2cEntity.platformCode"),
            @Mapping(target = "shipmentCode", ignore = true),
            @Mapping(target = "signOrderError", ignore = true),
            @Mapping(target = "soCode", source = "soB2cEntity.code"),
            @Mapping(target = "soId", source = "soB2cEntity.id"),
            @Mapping(target = "remark", source = "dto.remark"),
            @Mapping(target = "trackNo", ignore = true),
            @Mapping(target = "deliveryPlatform", source = "shopInfoEntity.dictPlatform"),
            @Mapping(target = "dictPlatform", source = "soB2cEntity.dictPlatform"),
            @Mapping(target = "logisticsChannelId", source = "channelEntity.id"),
            @Mapping(target = "logisticsChannelName", source = "channelEntity.name"),
            @Mapping(target = "deliveryWarehouseId", source = "shopInfoEntity.warehouseId"),
            @Mapping(target = "deliveryWarehouseName", source = "shopInfoEntity.warehouseName"),
            @Mapping(target = "shippingMethod", source = "dto.shippingMethod")
    })
    SoMultiChannelDTO.AddDTO soB2cToAddDTO(SoMultiChannelDTO.SaveDTO dto, ShopInfoEntity shopInfoEntity, SoB2cEntity soB2cEntity, LogisticsChannelEntity channelEntity);
    @Mappings({
            @Mapping(target = "mainId", ignore = true)
    })
    SoMultiChannelDetailDTO.AddDTO soB2cDetailToAddDTO(SoMultiChannelDTO.SoViewDTO detailList);
    List<SoMultiChannelDetailDTO.AddDTO> soB2cDetailToAddDTO(List<SoMultiChannelDTO.SoViewDTO> detailList);
}
