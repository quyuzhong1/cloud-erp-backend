package com.sdk.wms.iml.convert;

import com.common.business.dto.PlatformProductDTO;
import com.common.business.utils.MD5Util;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.sdk.wms.iml.dto.response.ImlProductResp;
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
}
