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

    /**
     * 引用仓位分区
     * @param ids
     * @return
     */
    void quoteLocation(List<String> ids);

    /**
     * 根据仓库仓位id获取详细信息（包含分区信息）
     * @param id
     * @return
     */
    WarehouseLocationDTO.LocationDetailDTO findById(String id);

    /**
     * 根据仓库id和仓位编码获取
     * @param warehouseId
     * @param code
     * @return
     */
    WarehouseLocationEntity findByWarehouseIdAndCode(String warehouseId, String code);

}
