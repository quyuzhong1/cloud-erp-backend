package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseAllocationHandleDetailDTO;
import com.erp.model.wms.entity.VirtualWarehousePushHandleEntity;

/**
 * <p>
 * 分货单拆单明细表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
public interface VirtualWarehouseAllocationHandleDetailService extends SuperService<VirtualWarehousePushHandleDetailEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehouseAllocationHandleDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehouseAllocationHandleDetailDTO.UpdateDTO dto);


    void handleDetail(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity);
}
