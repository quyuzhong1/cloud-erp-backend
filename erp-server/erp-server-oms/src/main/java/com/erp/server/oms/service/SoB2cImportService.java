package com.erp.server.oms.service;

import com.common.business.dto.base.BaseDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.excel.B2CManualDeliveryImportExcelDTO;
import com.erp.model.oms.entity.SoB2cEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * b2c订单拆分操作服务类
 */
public interface SoB2cImportService extends SuperService<SoB2cEntity> {

    void importManualDelivery(BaseDTO.ImportDTO dto);

    void importB2cManualDelivery(BaseDTO.ImportDTO dto);

    void downloadManualDeliveryTemplate(HttpServletResponse response);

    void handleManualDeliveryImportSuccessList(List<B2CManualDeliveryImportExcelDTO> successList, List<String> errorNoList, List<B2CManualDeliveryImportExcelDTO> errorList2);
}
