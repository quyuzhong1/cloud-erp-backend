package com.sdk.wms.goodcang.convert;

import com.common.business.dto.PlatformProductDTO;
import com.common.core.utils.Md5Util;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.sdk.wms.goodcang.dto.response.GoodCangSkuResp;
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
public interface GoodCangConverter {

    GoodCangConverter INSTANCE = Mappers.getMapper(GoodCangConverter.class);


    @Mappings({
            @Mapping(target = "platformType", constant = "warehouse"),
            @Mapping(target = "platformSkuNo", source = "productSku"),
            @Mapping(target = "platformProductNo", source = "productSku"),
            @Mapping(target = "platformProductName", source = "productTitleCn"),
            @Mapping(target = "productImageUrl", source = "productLink"),
            @Mapping(target = "productSpec", source = "productModel"),
            @Mapping(target = "type", expression ="java(GoodCangConverter.getType())"),
            @Mapping(target = "platformUpdateTime", source = "productModifyTime"),
            @Mapping(target = "downloadTime", expression = "java(GoodCangConverter.getNowTime())"),
            @Mapping(target = "uniqueId", expression = "java(GoodCangConverter.getUniqueKey(sourceData))"),
            @Mapping(target = "matchResult", constant = "false"),
            @Mapping(target = "platform", constant = "goodcang")
    })
    PlatformProductDTO productConversion(GoodCangSkuResp sourceData);

    List<PlatformProductDTO> productConversion(List<GoodCangSkuResp> sourceDataList);

    static String getNowTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    static String getType(){
        return RuleTypeEnum.THIRD_WAREHOUSE.getCode();
    }

    static String getUniqueKey(GoodCangSkuResp sourceData){
        String key = "goodcang"+sourceData.getProductSku();
        return key;
    }
}
