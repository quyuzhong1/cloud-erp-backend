package com.erp.server.dmp.service;

import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;

import java.util.List;

/**
 * 外部表查询服务
 *
 * @author Jim
 * @date 2024/11/22 19:34
 */
public interface ForeignService {

    /**
     * 查询物流轨迹
     */
    List<LogisticsTrackDTO.UpdateTrackDTO> listTrackDto(LogisticsBillDetailQueryDTO query);
}
