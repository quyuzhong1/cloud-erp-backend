package com.erp.server.wms.service;

import com.erp.model.srm.dto.DeliveryOrderDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * 供应商送货单接口
 */
public interface SupplierDeliveryOrderService {

    Boolean export(DeliveryOrderDTO.ParamDTO dto, HttpServletResponse response);
}
