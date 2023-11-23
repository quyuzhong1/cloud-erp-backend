package com.erp.server.tms.convert;

import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zdy
 * @ClassName TrackDataConverter
 * @description: TODO
 * @date 2023年11月23日
 * @version: 1.0
 */
@Mapper
@Component
public interface TrackDataConverter {
    TrackDataConverter INSTANCE = Mappers.getMapper(TrackDataConverter.class);

    LogisticsTrackEntity platformToTrack(PlatformTrackDetail detail);

    List<LogisticsTrackEntity> platformToTrack(List<PlatformTrackDetail> details);
}
