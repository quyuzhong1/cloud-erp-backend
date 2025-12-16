package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.OtherOutstockTrackNoDTO;
import com.erp.model.wms.entity.OtherOutstockTrackNoEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 其他出库单跟踪号映射表 服务类
 * </p>
 *
 * @author system
 * @since 2025-12-16
 */
public interface OtherOutstockTrackNoService extends SuperService<OtherOutstockTrackNoEntity> {

    /**
     * 批量更新跟踪号
     * @param dto 批量更新DTO
     * @return 是否成功
     */
    Boolean batchUpdateTrackNo(OtherOutstockTrackNoDTO.BatchUpdateDTO dto);

    /**
     * 根据出库单ID查询跟踪号列表
     * @param otherOutstockId 出库单ID
     * @return 跟踪号DTO
     */
    OtherOutstockTrackNoDTO.ViewDTO getTrackNoByOutstockId(String otherOutstockId);

    /**
     * 批量查询出库单的跟踪号
     * @param otherOutstockIds 出库单ID列表
     * @return 出库单ID和跟踪号的映射
     */
    Map<String, List<String>> getTrackNoMapByOutstockIds(List<String> otherOutstockIds);

    /**
     * 根据主表ids删除跟踪号
     * @param mainIds 主表ID列表
     */
    void removeByMainIds(List<String> mainIds);
}
