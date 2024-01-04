package com.erp.server.wms.service;

import com.erp.model.wms.dto.WeightingOutboundDTO;

/**
 * <p>
 * 称重出库 服务类
 * </p>
 *
 * @author liuruipeng
 * @since 2023-12-13
 */
public interface WeightingOutboundService {

    WeightingOutboundDTO.ViewDTO scan(WeightingOutboundDTO.ScanDTO dto);

    void reset(String id);
}
