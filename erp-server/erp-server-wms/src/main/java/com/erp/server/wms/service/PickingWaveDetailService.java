package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.PickingWaveDetailEntity;

import java.util.List;

public interface PickingWaveDetailService extends SuperService<PickingWaveDetailEntity> {
    /**
     * 根据主表id查询明细
     * @param mainId 主表id
     */
    List<PickingWaveDetailEntity> listByMainId(String mainId);

}
