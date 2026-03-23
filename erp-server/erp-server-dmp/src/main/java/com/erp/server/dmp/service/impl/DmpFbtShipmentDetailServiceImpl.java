package com.erp.server.dmp.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpFbtShipmentDetailEntity;
import com.erp.server.dmp.mapper.DmpFbtShipmentDetailMapper;
import com.erp.server.dmp.service.DmpFbtShipmentDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * FBT货件明细表 服务实现类
 * </p>
 */
@Slf4j
@Service
public class DmpFbtShipmentDetailServiceImpl extends SuperServiceImpl<DmpFbtShipmentDetailMapper, DmpFbtShipmentDetailEntity> implements DmpFbtShipmentDetailService {
}
