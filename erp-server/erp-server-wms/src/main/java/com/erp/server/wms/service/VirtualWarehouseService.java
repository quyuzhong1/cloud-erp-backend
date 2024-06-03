package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseDTO;

/**
 * <p>
 * 虚拟仓 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
public interface VirtualWarehouseService extends SuperService<VirtualWarehouseEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehouseDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-02
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehouseDTO.UpdateDTO dto);


    PagingVO<VirtualWarehouseDTO.ListDTO> paging(PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto);
}
