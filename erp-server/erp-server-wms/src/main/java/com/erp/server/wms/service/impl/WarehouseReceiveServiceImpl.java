package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.server.wms.mapper.WarehouseReceiveMapper;
import com.erp.server.wms.service.WarehouseReceiveService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库签收单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class WarehouseReceiveServiceImpl extends SuperServiceImpl<WarehouseReceiveMapper, WarehouseReceiveEntity> implements WarehouseReceiveService {

}
