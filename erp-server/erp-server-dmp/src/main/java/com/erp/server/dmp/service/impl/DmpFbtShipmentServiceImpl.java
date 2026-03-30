package com.erp.server.dmp.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpFbtShipmentEntity;
import com.erp.server.dmp.mapper.DmpFbtShipmentMapper;
import com.erp.server.dmp.service.DmpFbtShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * FBT货件主表 服务实现类
 * </p>
 */
@Slf4j
@Service
public class DmpFbtShipmentServiceImpl extends SuperServiceImpl<DmpFbtShipmentMapper, DmpFbtShipmentEntity> implements DmpFbtShipmentService {
}
