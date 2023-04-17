package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.dto.QcBillDTO;
import com.erp.model.wms.entity.QcBillEntity;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.QcBillService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 质检单表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcBillServiceImpl extends SuperServiceImpl<QcBillMapper, QcBillEntity> implements QcBillService {


    /**
     * 暂存 质检单
     * @param dto
     * @return
     */
    @Override
    public String draft(QcBillDTO.SaveOrUpdateDTO dto) {
        return null;
    }

    @Override
    public List<QcBillEntity> listByPoIds(List<String> poIds) {
        return lambdaQuery().in(QcBillEntity::getPurchaseOrderId,poIds).list();
    }
}
