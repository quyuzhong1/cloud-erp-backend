package com.erp.server.tms.convert;

import com.common.business.dto.base.BaseChildDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.sdk.tms.disifang.model.chanel.response.ChanelInfo;
import com.sdk.tms.shopee.model.logistics.response.LogisticsChannel;
import com.sdk.tms.tongyou.dto.response.TongYouChannel;
import com.sdk.tms.ubi.model.catalog.response.ServiceCataLog;
import com.sdk.tms.weishi.dto.response.WeiShiChannel;
import com.sdk.tms.yanwen.dto.response.YanWenChannel;
import com.sdk.tms.yuntu.dto.response.YunTuChannel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @description: 物流商
 * @author Will
 * @date: 2023/11/17 16:36
 */
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class})
public interface LogisticsSupplierConverter {

    LogisticsSupplierConverter INSTANCE = Mappers.getMapper(LogisticsSupplierConverter.class);

    @Mappings({
            @Mapping(target = "code", source = "id"),
            @Mapping(target = "value", source = "supplierName"),
            @Mapping(target = "disabled", source = "disabled"),

    })
    BaseDropDownDTO.DisabledDTO convertBySupplierDown(LogisticsSupplierEntity logisticsChannel);
    List<BaseDropDownDTO.DisabledDTO> convertBySupplierDown(List<LogisticsSupplierEntity> list);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "supplierName"),
            @Mapping(target = "disabled", source = "disabled"),

    })
    BaseChildDTO.ListChildTreeDTO convertTree(LogisticsSupplierEntity entity);
    List<BaseChildDTO.ListChildTreeDTO> convertTree(List<LogisticsSupplierEntity> dbList);
}
