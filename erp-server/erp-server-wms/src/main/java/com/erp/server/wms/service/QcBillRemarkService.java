package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcRemarkDTO;
import com.erp.model.wms.entity.QcBillRemarkEntity;

import java.util.List;

/**
 * <p>
 * 质检单备注表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
public interface QcBillRemarkService extends SuperService<QcBillRemarkEntity> {

    void draft(String billId, List<QcRemarkDTO.AddDTO> remarkList);
}
