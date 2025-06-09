package com.sdk.wms.jifeng.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface JiFengConverter {

    JiFengConverter INSTANCE = Mappers.getMapper(JiFengConverter.class);

}
