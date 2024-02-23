package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.srm.dto.DeliveryOrderDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 供应商送货单接口
 */
public interface SupplierDeliveryOrderService {

    Boolean export(DeliveryOrderDTO.ParamDTO dto, HttpServletResponse response);

    List<BatchResultDTO> generateReceive(DeliveryOrderDTO.GenerateDTO dto);
}
