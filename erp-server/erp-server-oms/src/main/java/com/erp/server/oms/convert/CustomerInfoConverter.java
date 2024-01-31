package com.erp.server.oms.convert;

import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.WalmartShipOrderDetailDTO;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface CustomerInfoConverter {

    CustomerInfoConverter INSTANCE = Mappers.getMapper(CustomerInfoConverter.class);

    @Mappings({
            @Mapping(target = "shopId", source = "soB2cEntity.shopId"),
            @Mapping(target = "platformCode", source = "soB2cEntity.platformCode"),
            @Mapping(target = "soCode", source = "soB2cEntity.code"),
            @Mapping(target = "trackNo", source = "soB2cLogisticsEntity.code"),
            @Mapping(target = "shipDateTime", source = "soB2cLogisticsEntity.deliveryTime"),
            @Mapping(target = "detailList", ignore = true)
    })
    WalmartShipDTO soB2cEntityToWalmartShipDTO(SoB2cEntity soB2cEntity, SoB2cLogisticsEntity soB2cLogisticsEntity);


    @Mappings({
            @Mapping(target = "mainId", source = "customerInfoEntity.id"),
            @Mapping(target = "originSellerId", source = "customerInfoEntity.sellerId"),
            @Mapping(target = "originSellerName", source = "customerInfoEntity.sellerName"),
            @Mapping(target = "changeSellerId", source = "addDTO.changeSellerId"),
            @Mapping(target = "changeSellerName", source = "addDTO.changeSellerName"),
            @Mapping(target = "startDate", source = "addDTO.startDate"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "approveStatus", expression = "java(com.common.business.enums.ApproveStatusEnum.WAIT_SUBMIT)"),
            @Mapping(target = "remark", ignore = true),
    })
    CustomerB2bSellerChangeEntity toCustomerB2bSellerChangeConvert(CustomerInfoEntity customerInfoEntity, CustomerB2bSellerChangeDTO.AddDTO addDTO);
}
