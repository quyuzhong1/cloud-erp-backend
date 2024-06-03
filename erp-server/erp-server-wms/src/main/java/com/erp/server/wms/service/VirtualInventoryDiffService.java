package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * 库存差异 服务类
 *
 * @author will
 * @since 2024-06-03
 */
public interface VirtualInventoryDiffService extends SuperService<VirtualInventoryEntity> {

    /**
     * 库存差异列表
     * @author will
     * @date 2024/6/3 16:34
     * @param dto 
     * @return PagingVO<ListDTO>
     */
    PagingVO<VirtualInventoryDiffDTO.ListDTO> diffPaging(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto);
    /**
     * 库存差异明细列表
     * @author will
     * @date 2024/6/3 16:45
     * @param dto
     * @return PagingVO<ListDetailDTO>
     */
    PagingVO<VirtualInventoryDiffDTO.ListDetailDTO> diffDetailPaging(PagingDTO<VirtualInventoryDiffDTO.SearchParamDetailDTO> dto);
    /**
     * 库存差异列表导出
     * @author will
     * @date 2024/6/3 17:58
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(VirtualInventoryDiffDTO.SearchParamDTO dto, HttpServletResponse response);
}
