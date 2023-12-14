package com.erp.server.oms.convert;

import com.common.business.dto.PlatformProductDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.sdk.tms.weishi.dto.response.WeiShiChannel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 映射工具类
 * </p>
 *
 * @author Jim
 * @since 2023-11-15
 */
@Mapper
@Component
public interface B2cOrderConverter {
    B2cOrderConverter INSTANCE = Mappers.getMapper(B2cOrderConverter.class);


    @Mappings({
            @Mapping(target = "customerId", source = "customerId"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "receiverName", source = "receiverName"),
            @Mapping(target = "receiverTelNumber", source = "receiverTelNumber"),
            @Mapping(target = "fullAddress", source = "fullAddress"),
            @Mapping(target = "postCode", source = "postCode"),
            @Mapping(target = "country", source = "country"),
            @Mapping(target = "countryName", source = "countryName"),
            @Mapping(target = "provinceName", source = "provinceName"),
            @Mapping(target = "cityName", source = "cityName"),
            @Mapping(target = "districtName", source = "districtName"),
            @Mapping(target = "firstAddress", source = "firstAddress"),
            @Mapping(target = "secondAddress", source = "secondAddress"),

    })
    LogisticsBillDTO.ReceiverDTO convertReceiver(SoB2cReceiverEntity receiverEntity);

    @Mappings({
            @Mapping(target = "weight", source = "weight"),
            @Mapping(target = "length", source = "length"),
            @Mapping(target = "width", source = "width"),
            @Mapping(target = "height", source = "height"),

    })
    LogisticsBillDTO.PackageDTO convertPackage(SoB2cLogisticsEntity soB2cLogisticsEntity);


    @Mappings({
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "qty", source = "qty"),

    })
    LogisticsBillDTO.SkuDTO convertSku(SoB2cDetailEntity data);
    List<LogisticsBillDTO.SkuDTO> convertSku(List<SoB2cDetailEntity> detailList);


    @Mappings({
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "phone", source = "receiverTelNumber"),
            @Mapping(target = "countryCode", source = "country"),
            @Mapping(target = "province", source = "provinceName"),
            @Mapping(target = "city", source = "cityName"),
            @Mapping(target = "zipcode", source = "postCode"),
            @Mapping(target = "address1", source = "firstAddress"),
            @Mapping(target = "address2", source = "secondAddress"),

    })
    ThirdWarehouseCreateOutboundReq.ReceiverInfo convertThirdWarehouseReceiver(SoB2cReceiverEntity receiverEntity);


    @Mappings({
            @Mapping(target = "productSku", source = "warehouseSkuNo"),
            @Mapping(target = "quantity", source = "qty"),
    })
    ThirdWarehouseCreateOutboundReq.Item convertThirdWarehouseItem(SoB2cDetailEntity detail);
    List<ThirdWarehouseCreateOutboundReq.Item> convertThirdWarehouseItem(List<SoB2cDetailEntity> detailList);
}
