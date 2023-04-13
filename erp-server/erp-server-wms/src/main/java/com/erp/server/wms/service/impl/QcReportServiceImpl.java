package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.QcReportEntity;
import com.erp.server.wms.mapper.QcReportMapper;
import com.erp.server.wms.service.QcReportService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 质检报告 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
@Service
public class QcReportServiceImpl extends SuperServiceImpl<QcReportMapper, QcReportEntity> implements QcReportService {

}
