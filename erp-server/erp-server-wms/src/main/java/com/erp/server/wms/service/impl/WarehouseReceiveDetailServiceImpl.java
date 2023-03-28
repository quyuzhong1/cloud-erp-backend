package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.server.wms.mapper.WarehouseReceiveDetailMapper;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库签收明细单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class WarehouseReceiveDetailServiceImpl extends SuperServiceImpl<WarehouseReceiveDetailMapper, WarehouseReceiveDetailEntity> implements WarehouseReceiveDetailService {

}
