package com.erp.server.dmp.convert;

import com.erp.model.dmp.dto.DmpLogisticsTrackRegisterDTO;
import com.erp.model.dmp.entity.DmpLogisticsTrackRegisterEntity;
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

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    DmpLogisticsTrackRegisterEntity converterRegisterToEntity(DmpLogisticsTrackRegisterDTO.AddDTO addDTOList);
    List<DmpLogisticsTrackRegisterEntity> converterRegisterToEntity(List<DmpLogisticsTrackRegisterDTO.AddDTO> addDTOList);
}
