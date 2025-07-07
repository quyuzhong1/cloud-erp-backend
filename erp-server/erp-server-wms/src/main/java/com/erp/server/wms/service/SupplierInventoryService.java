package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SupplierInventoryDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * 即时库存
 * @author will
 * @date 2025/6/19 15:09
 */
public interface SupplierInventoryService {

    /**
     * 分页查询
     * @author will
     * @date 2025/6/18 17:21
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<SupplierInventoryDTO.ListDTO> paging(PagingDTO<SupplierInventoryDTO.PagingParamDTO> dto);

    /**
     * 导出
     * @author will
     * @date 2025/6/18 18:00
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(SupplierInventoryDTO.PagingParamDTO dto, HttpServletResponse response);
}
