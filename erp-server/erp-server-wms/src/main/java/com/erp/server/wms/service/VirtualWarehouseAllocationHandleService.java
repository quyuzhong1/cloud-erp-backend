package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseAllocationHandleDTO;

/**
 * <p>
 * 分货单拆单主表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
public interface VirtualWarehouseAllocationHandleService extends SuperService<VirtualWarehousePushHandleEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehouseAllocationHandleDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehouseAllocationHandleDTO.UpdateDTO dto);


    void handleData(VirtualWarehouseAllocationEntity allocationEntity);

    void forceDeleteById(String id);
}
