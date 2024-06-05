package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;

/**
 * <p>
 * 虚拟仓分货单 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
public interface VirtualWarehouseAllocationService extends SuperService<VirtualWarehouseAllocationEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-05
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehouseAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-05
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehouseAllocationDTO.UpdateDTO dto);

    /**
     * 列表查询
     *
     * @param dto
     * @return PagingVO
     * @author hyj
     * @date: 2024-06-05
     */
    PagingVO<VirtualWarehouseAllocationDTO.ListDTO> paging(PagingDTO<VirtualWarehouseAllocationDTO.PagingParamDTO> dto);
}
