package com.erp.server.tms.convert;

import com.common.business.dto.base.BaseChildDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {TypeConversionWorker.class, BooleanMapperWork.class})
public interface TransferLogisticsChannelConverter {

    TransferLogisticsChannelConverter INSTANCE = Mappers.getMapper(TransferLogisticsChannelConverter.class);

    @Mappings({
            @Mapping(target = "code", source = "id"),
            @Mapping(target = "value", source = "name"),
            @Mapping(target = "disabled", source = "disabled"),

    })
    BaseDropDownDTO.DisabledDTO convertByChannelDown(TransferLogisticsChannelEntity logisticsChannel);
    List<BaseDropDownDTO.DisabledDTO> convertByChannelDown(List<TransferLogisticsChannelEntity> list);


    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "disabled", source = "disabled"),

    })
    BaseChildDTO.ListChildTreeDTO convertTree(TransferLogisticsChannelEntity entity);
    List<BaseChildDTO.ListChildTreeDTO> convertTree(List<TransferLogisticsChannelEntity> channelList);
}
