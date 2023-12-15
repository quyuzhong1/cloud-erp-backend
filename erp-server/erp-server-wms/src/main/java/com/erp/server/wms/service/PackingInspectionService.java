package com.erp.server.wms.service;

import com.erp.model.wms.dto.PackingInspectionDTO;

/**
 * <p>
 * 包装验货 服务类
 * </p>
 *
 * @author liuruipeng
 * @since 2023-12-13
 */
public interface PackingInspectionService {

    PackingInspectionDTO.ViewDTO scan(PackingInspectionDTO.ScanDTO dto);

    void reset(String id);
}
