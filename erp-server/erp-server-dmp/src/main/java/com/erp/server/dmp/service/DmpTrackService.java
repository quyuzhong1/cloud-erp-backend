package com.erp.server.dmp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.DmpThirdOutboundDTO;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.model.response.TrackDetail;

import java.util.List;

/**
 * <p>
 * 物流轨迹 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-08
 */
public interface DmpTrackService {
    String listMongoTractDataByTrackNoList(List<String> trackNoList);
}
