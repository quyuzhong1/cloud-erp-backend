package com.erp.server.wms.service;

import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-05
     */
    BaseResultDTO.AddDTO batchAdd(VirtualWarehouseAllocationDetailDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-05
     */
    Boolean batchUpdate(VirtualWarehouseAllocationDetailDTO.UpdateDTO dto);


    void batchAdd(VirtualWarehouseAllocationDTO.AddDTO addDTO, String id);

    Boolean batchUpdate(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO, String id);


    BatchResultDTO manualFinish(VirtualWarehouseAllocationDetailEntity vmAllocationDetailEntity, VirtualWarehouseAllocationEntity vmAllocationEntity, VirtualWarehouseAllocationDTO.ManualFinishDto dto);

    void submit(VirtualWarehouseAllocationEntity allocationEntity);

    void updateByMainId(String mainId, String syncStatus);

    BatchResultDTO sync(VirtualWarehouseAllocationDetailEntity vmAllocationDetailEntity, VirtualWarehouseAllocationEntity vmAllocationEntity);
}
