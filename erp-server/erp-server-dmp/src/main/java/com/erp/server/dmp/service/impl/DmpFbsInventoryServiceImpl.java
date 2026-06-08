package com.erp.server.dmp.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpFbsInventoryEntity;
import com.erp.server.dmp.mapper.DmpFbsInventoryMapper;
import com.erp.server.dmp.service.DmpFbsInventoryService;
import org.springframework.stereotype.Service;

/**
 * DMP FBS 库存 Service 实现
 */
@Service
public class DmpFbsInventoryServiceImpl extends SuperServiceImpl<DmpFbsInventoryMapper, DmpFbsInventoryEntity>
        implements DmpFbsInventoryService {
}
