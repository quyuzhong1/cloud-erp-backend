package com.erp.server.wms.service;
import com.erp.model.wms.dto.QcDefectDTO;
import com.erp.model.wms.entity.QcDefectEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2026-03-23
 */
public interface QcDefectService extends SuperService<QcDefectEntity> {

    void add(String billId, List<QcDefectDTO.AddDTO> qcDefectList);

}
