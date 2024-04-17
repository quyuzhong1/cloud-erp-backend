package com.erp.server.oms.convert;

import com.common.business.dto.WalmartShipDTO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

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
            @Mapping(target = "approveTime", ignore = true),
            @Mapping(target = "approveUserId", ignore = true),
            @Mapping(target = "approveUserName", ignore = true),
    })
    CustomerB2bSellerChangeEntity toCustomerB2bSellerChangeConvert(CustomerInfoEntity customerInfoEntity, CustomerB2bSellerChangeDTO.AddDTO addDTO);

    /**
     * 填充客户字段
     *
     * @param addDTO
     * @param id
     * @return
     */
    @Mappings({
            @Mapping(target = "platformType", source = "addDTO.dictPlatform"),
            @Mapping(target = "name", source = "addDTO.receiverDTO.receiverName"),
            @Mapping(target = "countryId", source = "addDTO.receiverDTO.country"),
            @Mapping(target = "currency", source = "addDTO.currency"),
            @Mapping(target = "remark", source = "addDTO.remark"),
            @Mapping(target = "conditionDict", constant = "onlineStorePayment"),
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceType", constant = "soB2c")
    })
    CustomerB2CDTO.AddDTO soB2cAddToCustomerBase(SoB2cDTO.AddDTO addDTO, String id);

    /**
     * 订单客户信息映射到客户列表
     * @param receiverDTO
     * @return
     */
    @Mappings({
            @Mapping(target = "person", source = "receiverName"),
//            @Mapping(target = "position", constant = ""),
            @Mapping(target = "telNumber", source = "receiverTelNumber"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "isDefault", constant = "true"),
            @Mapping(target = "disabled", constant = "false")
    })
    CustomerContactDTO.AddDTO soB2cAddReceiveToContact(SoB2cReceiverDTO.AddDTO receiverDTO);
    @Mappings({
//            @Mapping(target = "address", source = "receiverName"),
            @Mapping(target = "person", source = "receiverName"),
            @Mapping(target = "type", constant = "receive"),
            @Mapping(target = "telNumber", source = "receiverTelNumber"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "zipCode", source = "postCode"),
            @Mapping(target = "isDefault", constant = "true"),
            @Mapping(target = "disabled", constant = "false")
    })
    CustomerAddressDTO.AddDTO soB2cAddReceiveToAddress(SoB2cReceiverDTO.AddDTO receiverDTO);
    /**
     * 填充客户字段
     *
     * @param updateDTO
     * @return
     */
    @Mappings({
            @Mapping(target = "platformType", source = "updateDTO.dictPlatform"),
            @Mapping(target = "name", source = "updateDTO.receiverDTO.receiverName"),
            @Mapping(target = "countryId", source = "updateDTO.receiverDTO.country"),
            @Mapping(target = "currency", source = "updateDTO.currency"),
            @Mapping(target = "remark", source = "updateDTO.remark"),
            @Mapping(target = "conditionDict", constant = "onlineStorePayment"),
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceType", constant = "soB2c")
    })
    CustomerB2CDTO.AddDTO soB2cUpdateToCustomerBase(SoB2cDTO.UpdateDTO updateDTO);
    /**
     * 订单客户信息映射到客户列表
     * @param receiverDTO
     * @return
     */
    @Mappings({
            @Mapping(target = "person", source = "receiverName"),
//            @Mapping(target = "position", constant = ""),
            @Mapping(target = "telNumber", source = "receiverTelNumber"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "isDefault", constant = "true"),
            @Mapping(target = "disabled", constant = "false")
    })
    CustomerContactDTO.AddDTO soB2cUpdateReceiveToContact(SoB2cReceiverDTO.UpdateDTO receiverDTO);
    @Mappings({
//            @Mapping(target = "address", source = "receiverName"),
            @Mapping(target = "person", source = "receiverName"),
            @Mapping(target = "type", constant = "receive"),
            @Mapping(target = "telNumber", source = "receiverTelNumber"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "zipCode", source = "postCode"),
            @Mapping(target = "isDefault", constant = "true"),
            @Mapping(target = "disabled", constant = "false")
    })
    CustomerAddressDTO.AddDTO soB2cUpdateReceiveToAddress(SoB2cReceiverDTO.UpdateDTO receiverDTO);
}
