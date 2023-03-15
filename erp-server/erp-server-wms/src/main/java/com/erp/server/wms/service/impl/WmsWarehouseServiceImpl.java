package com.erp.server.wms.service.impl;

import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.wms.entity.WmsWarehouseEntity;
import com.erp.server.wms.mapper.WmsWarehouseMapper;
import com.erp.server.wms.service.WmsWarehouseService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 仓库表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
public class WmsWarehouseServiceImpl extends SuperServiceImpl<WmsWarehouseMapper, WmsWarehouseEntity> implements WmsWarehouseService {

}
