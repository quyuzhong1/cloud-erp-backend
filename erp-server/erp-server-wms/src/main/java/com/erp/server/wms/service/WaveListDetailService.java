package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.WaveListDetailEntity;

import java.util.List;
import java.util.Map;

public interface WaveListDetailService extends SuperService<WaveListDetailEntity> {
    /**
     * 根据主表id查询明细
     * @param mainId 主表id
     */
    List<WaveListDetailEntity> listByMainId(String mainId);

    Map<String, String> getOrderBasketNoMap(List<String> soIds);

    WaveListDetailDTO.ViewDTO view(String waveId) throws ServiceException;

    ApiResult<?> moveOut(WaveListDetailDTO.MoveOutDTO moveOutDTO);

    List<WaveListDetailEntity> listByMainIds(List<String> waveIds);

    ApiResult<?> moveOut(String deliveryId, Boolean isIntercept);

    /**
     * 根据发货单查询波次
     *
     * @param deliveryIds 发货单
     */
    List<WaveListDetailEntity> listCancelByDeliveryIds(List<String> deliveryIds);

    void deleteByMainId(String mainId);

    List<PickingDetailEntity> getPickingDetail(List<String> deliveryIds);
}
