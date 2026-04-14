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

    /**
     * 根据第三方配置映射表，查询待注册的物流单详情 (由配置驱动)
     *
     * @param query 查询条件
     * @param platformType 第三方平台类型 (如 TRACK123)
     * @return 待注册单据列表
     */
    List<LogisticsTrackDTO.UpdateTrackDTO> listWaitingRegisterByConfig(LogisticsBillDetailQueryDTO query, String platformType);
}
