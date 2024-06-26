package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualWarehousePushHandleRelationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseAllocationHandleRelationDTO;

/**
 * <p>
 * 分货单拆单关联关系表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
public interface VirtualWarehouseAllocationHandleRelationService extends SuperService<VirtualWarehousePushHandleRelationEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehouseAllocationHandleRelationDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-07
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehouseAllocationHandleRelationDTO.UpdateDTO dto);


}
