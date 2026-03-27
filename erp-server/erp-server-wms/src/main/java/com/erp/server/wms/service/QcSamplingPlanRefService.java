package com.erp.server.wms.service;

import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.entity.QcSamplingPlanRefEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2026-03-25
 */
public interface QcSamplingPlanRefService extends SuperService<QcSamplingPlanRefEntity> {

    void add(String billId, QcNoticeDTO.QcStandardAddDTO qcStandardAddDTO);

    void removeByMainIds(List<String> ids);
}
