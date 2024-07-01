package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.entity.WaveListDetailEntity;

import java.util.List;
import java.util.Map;

public interface PickingWaveDetailService extends SuperService<WaveListDetailEntity> {
    /**
     * 根据主表id查询明细
     * @param mainId 主表id
     */
    List<WaveListDetailEntity> listByMainId(String mainId);

    Map<String, String> getOrderBasketNoMap(List<String> soIds);
}
