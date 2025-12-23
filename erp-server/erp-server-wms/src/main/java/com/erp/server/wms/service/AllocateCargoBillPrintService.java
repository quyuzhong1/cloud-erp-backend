package com.erp.server.wms.service;

import com.erp.model.wms.dto.AllocateCargoBillPrintDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 配货单打印 服务类
 * </p>
 *
 */
public interface AllocateCargoBillPrintService {

    AllocateCargoBillPrintDTO.ScanWaveDTO scanWaveOrPickingCarCode(String businessCode);

    String print(String waveId, HttpServletResponse response);
}
