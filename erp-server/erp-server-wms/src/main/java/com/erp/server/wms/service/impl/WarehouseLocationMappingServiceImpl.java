package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.WarehouseLocationMappingEntity;
import com.erp.server.wms.mapper.WarehouseLocationMappingMapper;
import com.erp.server.wms.service.WarehouseLocationMappingService;
import org.springframework.stereotype.Service;

/**
 * 第三方仓位映射业务类
 * @date 2024-08-14
 * @author tanmujin
 */
@Service
public class WarehouseLocationMappingServiceImpl extends SuperServiceImpl<WarehouseLocationMappingMapper, WarehouseLocationMappingEntity> implements WarehouseLocationMappingService {
}
