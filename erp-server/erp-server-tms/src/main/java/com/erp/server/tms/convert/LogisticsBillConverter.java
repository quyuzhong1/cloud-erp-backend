package com.erp.server.tms.convert;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.vo.request.ReceiverInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 *物流单转化
 *@author yl
 *@date 2023-11-23
 */
@Mapper
public interface LogisticsBillConverter {

    LogisticsBillConverter INSTANCE = Mappers.getMapper(LogisticsBillConverter.class);

    @Mappings({
            @Mapping(target = "actId", source = "customerId"),
            @Mapping(target = "contact", source = "receiverName"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "telNumber", source = "receiverTelNumber"),
            @Mapping(target = "country", source = "country"),
            @Mapping(target = "province", source = "provinceName"),
            @Mapping(target = "city", source = "cityName"),
            @Mapping(target = "district", source = "districtName"),
            @Mapping(target = "streetAddress", source = "fullAddress"),
            @Mapping(target = "addressFirst", source = "firstAddress"),
            @Mapping(target = "addressSecond", source = "secondAddress"),
            @Mapping(target = "zipCode", source = "postCode"),

    })
    ReceiverInfoVO convertReceiver(LogisticsBillDTO.ReceiverDTO  receiver);





}
