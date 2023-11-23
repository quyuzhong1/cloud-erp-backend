package com.erp.server.wms.convert;

import com.common.business.dto.PlatformTransferWarehouseDTO;
import com.common.business.dto.PlatformWarehouseDTO;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.MD5Util;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.entity.OverseasTransferWarehouseEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.goodcang.dto.response.GoodCangSkuResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import javax.annotation.Resource;

/**
 * 海外仓
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface OverseasWarehouseConverter {

    OverseasWarehouseConverter INSTANCE = Mappers.getMapper(OverseasWarehouseConverter.class);

    @Mappings({
            @Mapping(target = "mainId", source = "providerErpId"),
            @Mapping(target = "platformWarehouseCode", source = "warehouseCode"),
            @Mapping(target = "platformWarehouseName", source = "warehouseName"),
            @Mapping(target = "country", source = "countryCode"),
            @Mapping(target = "countryName", source = "countryName"),
            @Mapping(target = "warehouseCode", ignore = true),
            @Mapping(target = "warehouseId", ignore = true),
            @Mapping(target = "warehouseName",  ignore = true)
    })
    OverseasProviderWarehouseEntity mqDtoToDbDto(PlatformWarehouseDTO dto);

    @Mappings({
            @Mapping(target = "dictPlatform", source = "provider"),
            @Mapping(target = "platformWarehouseCode", source = "transferWarehouseCode"),
            @Mapping(target = "name", source = "transferWarehouseName"),
            @Mapping(target = "platformToWarehouseCode", source = "destinationWarehouseCode"),
            @Mapping(target = "platformToWarehouseName", source = "destinationWarehouseName"),
            @Mapping(target = "logisticsProductCode", source = "logisticsChannelCode"),
            @Mapping(target = "logisticsProductName", source = "logisticsChannelName")
    })
    OverseasTransferWarehouseEntity transferDtoConvert(PlatformTransferWarehouseDTO dto);
}
