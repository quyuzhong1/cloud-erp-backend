package com.erp.server.wms.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.wms.entity.WmsWarehouseReceiveEntity;
import com.erp.server.wms.mapper.WmsWarehouseReceiveMapper;
import com.erp.server.wms.service.WmsWarehouseReceiveService;
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
public class WmsWarehouseReceiveServiceImpl extends SuperServiceImpl<WmsWarehouseReceiveMapper, WmsWarehouseReceiveEntity> implements WmsWarehouseReceiveService {

}
