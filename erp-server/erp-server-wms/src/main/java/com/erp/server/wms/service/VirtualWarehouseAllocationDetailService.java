package com.erp.server.wms.service;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;

/**
 * <p>
 * 虚拟仓分货单明细 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
public interface VirtualWarehouseAllocationDetailService extends SuperService<VirtualWarehouseAllocationDetailEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-05
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO batchAdd(VirtualWarehouseAllocationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-05
    * @param dto
    * @return
    */
    Boolean batchUpdate(VirtualWarehouseAllocationDetailDTO.UpdateDTO dto);


    void batchAdd(VirtualWarehouseAllocationDTO.AddDTO addDTO, String id);

    Boolean batchUpdate(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO, String id);
}
