package com.erp.server.wms.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.wms.entity.WmsWarehouseSignEntity;
import com.erp.server.wms.mapper.WmsWarehouseSignMapper;
import com.erp.server.wms.service.WmsWarehouseSignService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库签收单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
public class WmsWarehouseSignServiceImpl extends SuperServiceImpl<WmsWarehouseSignMapper, WmsWarehouseSignEntity> implements WmsWarehouseSignService {

}
