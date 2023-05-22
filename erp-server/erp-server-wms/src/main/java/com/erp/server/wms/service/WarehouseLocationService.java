package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;

import java.util.List;

/**
 * <p>
 * 仓库仓位表 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
public interface WarehouseLocationService extends SuperService<WarehouseLocationEntity> {

    /**
     * 仓位下拉列表
     * @return
     */
    List<WarehouseLocationDTO.LocationListDTO> select(String warehouseId);

}
