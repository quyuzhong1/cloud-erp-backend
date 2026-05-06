package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.SoB2bDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2bDeliveryInterceptDetailEntity;

import java.util.List;

/**
 * <p>
 * b2b发货拦截单详情 服务类
 * </p>
 *
 * @author Codex
 */
public interface SoB2bDeliveryInterceptDetailService extends SuperService<SoB2bDeliveryInterceptDetailEntity> {

    void add(SoB2bDeliveryInterceptDTO.AddDTO addDTO, String mainId);

    List<SoB2bDeliveryInterceptDetailEntity> listByMainIds(List<String> mainIds);
}
