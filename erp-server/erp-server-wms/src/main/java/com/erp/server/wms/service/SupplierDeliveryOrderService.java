package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;

import java.util.List;

/**
 * 供应商送货单接口
 */
public interface SupplierDeliveryOrderService {

    Boolean export(DeliveryOrderDTO.ParamDTO dto);

    List<BatchResultDTO> generateReceive(DeliveryOrderDTO.GenerateDTO dto);

    PagingVO<DeliveryOrderExportExcelDTO> exportSupplierDeliveryOrder(PagingDTO<DeliveryOrderDTO.ParamDTO> dto);
}
