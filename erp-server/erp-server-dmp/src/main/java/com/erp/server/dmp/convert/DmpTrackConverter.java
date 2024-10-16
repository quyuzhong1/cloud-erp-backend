package com.erp.server.dmp.convert;

import com.sdk.tms.track123.dto.PlatformTrackDetail;
import com.sdk.tms.track123.model.response.TrackingDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

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
