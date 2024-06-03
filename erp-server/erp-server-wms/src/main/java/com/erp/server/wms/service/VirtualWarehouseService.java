package com.erp.server.wms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import org.springframework.transaction.annotation.Transactional;

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
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-02
     */
    BaseResultDTO.AddDTO add(VirtualWarehouseDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-02
     */
    Boolean update(VirtualWarehouseDTO.UpdateDTO dto);


    PagingVO<VirtualWarehouseDTO.ListDTO> paging(PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto);

    /**
     * 修改状态
     */
    Boolean updateState(VirtualWarehouseDTO.UpdateStateDTO updateStateDTO);

    /**
     * 预览
     *
     * @param id
     * @return
     */
    VirtualWarehouseDTO.ViewDTO view(String id);
}
