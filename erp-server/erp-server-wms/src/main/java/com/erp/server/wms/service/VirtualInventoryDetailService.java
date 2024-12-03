package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;

/**
 * <p>
 * 虚拟仓库明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface VirtualInventoryDetailService extends SuperService<VirtualInventoryDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualInventoryDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    Boolean update(VirtualInventoryDetailDTO.UpdateDTO dto);

    /**
     * 分页列表
     * @author will
     * @date 2024/12/3 17:34
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<VirtualInventoryAgeDTO.ListDTO> paging(PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto);
}
