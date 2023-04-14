package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.QcBillEntity;
import com.erp.server.wms.mapper.QcBillMapper;
import com.erp.server.wms.service.QcBillService;
import org.springframework.stereotype.Service;

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

}
