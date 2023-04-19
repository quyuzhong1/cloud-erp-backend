package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.dto.QcRemarkDTO;
import com.erp.model.wms.entity.QcBillRemarkEntity;
import com.erp.server.wms.mapper.QcBillRemarkMapper;
import com.erp.server.wms.service.QcBillRemarkService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 质检单备注表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcBillRemarkServiceImpl extends SuperServiceImpl<QcBillRemarkMapper, QcBillRemarkEntity> implements QcBillRemarkService {

    @Override
    public void draft(String billId, List<QcRemarkDTO.AddDTO> remarkList) {

    }
}
