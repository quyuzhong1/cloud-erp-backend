package com.sdk.wms.iml.convert;

import com.common.business.dto.PlatformCityDictDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.dto.PlatformWarehouseDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.business.utils.MD5Util;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.sdk.wms.iml.dto.response.ImlProductResp;
import com.sdk.wms.iml.dto.response.ImlRegionResp;
import com.sdk.wms.iml.dto.response.ImlWarehouseResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
@Component
public interface ImlConverter {

    ImlConverter INSTANCE = Mappers.getMapper(ImlConverter.class);

    @Mappings({
            @Mapping(target = "platformType", constant = "warehouse"),
            @Mapping(target = "platformSkuNo", source = "productSku"),
            @Mapping(target = "platformProductNo", source = "productSku"),
            @Mapping(target = "platformProductName", source = "productDeclaredName"),
            @Mapping(target = "productImageUrl", source = "productDescUrl"),
            @Mapping(target = "productSpec", source = "productModel"),
            @Mapping(target = "type", expression ="java(ImlConverter.getType())"),
            @Mapping(target = "platformUpdateTime", source = "productModifyTime"),
            @Mapping(target = "downloadTime", expression = "java(ImlConverter.getNowTime())"),
            @Mapping(target = "uniqueId", expression = "java(ImlConverter.getUniqueKey(sourceData))"),
            @Mapping(target = "matchResult", constant = "false"),
            @Mapping(target = "platform", constant = "iml")
    })
    PlatformProductDTO productConversion(ImlProductResp sourceData);

    List<PlatformProductDTO> productConversion(List<ImlProductResp> sourceDataList);

    static String getNowTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    static String getType(){
        return RuleTypeEnum.THIRD_WAREHOUSE.getCode();
    }

    static String getUniqueKey(ImlProductResp sourceData){
        return MD5Util.toMD5("iml"+sourceData.getProductSku());
    }

    static String getProvider(){
        return OmsPlatformEnum.OMS_IML.getCode();
    }

    static String getWarehousePlatformType(){
        return WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode();
    }

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(ImlConverter.getWarehousePlatformType())"),
            @Mapping(target = "provider",  expression = "java(ImlConverter.getProvider())"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "warehouseName",  source = "warehouseName"),
            @Mapping(target = "countryCode",  source = "countryCode"),
            @Mapping(target = "providerErpId",  source = "authId"),
            @Mapping(target = "platform",  expression = "java(ImlConverter.getProvider())")
    })
    PlatformWarehouseDTO warehouseConversion(ImlWarehouseResp sourceData);
    List<PlatformWarehouseDTO> warehouseConversion(List<ImlWarehouseResp> sourceDataList);

    @Mappings({
            @Mapping(target = "provider",  expression = "java(ImlConverter.getProvider())"),
            @Mapping(target = "regionId",  source = "regionId"),
            @Mapping(target = "parentRegionId",  source = "parentRegionId"),
            @Mapping(target = "regionName",  source = "regionName"),
            @Mapping(target = "regionLevel",  source = "regionLevel"),
            @Mapping(target = "platform",  expression = "java(ImlConverter.getProvider())")
    })
    PlatformCityDictDTO regionConversion(ImlRegionResp sourceDataList);
    List<PlatformCityDictDTO> regionConversion(List<ImlRegionResp> sourceDataList);
}
