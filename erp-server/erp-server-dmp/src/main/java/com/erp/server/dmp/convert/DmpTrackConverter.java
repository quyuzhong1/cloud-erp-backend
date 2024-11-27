package com.erp.server.dmp.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * @author zdy
 * @ClassName DmpTrackConverter
 * @description: TODO
 * @date 2024年10月08日
 * @version: 1.0
 */
@Mapper
@Component
public interface DmpTrackConverter {
    DmpTrackConverter INSTANCE = Mappers.getMapper(DmpTrackConverter.class);
}
