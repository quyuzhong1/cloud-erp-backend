package com.erp.server.wms.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 虚拟库存表 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
public interface VirtualInventoryService extends SuperService<VirtualInventoryEntity> {

    /**
     * 虚拟库存列表
     * @author will
     * @date 2024/6/3 14:58
     * @param dto PagingVO<ListDTO>
     */
    PagingVO<VirtualInventoryDTO.ListDTO> paging(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto);
    /**
     * 库存差异明细列表
     * @author will
     * @date 2024/6/3 16:56
     * @param dto
     * @return PagingVO<ListDetailDTO>
     */
    PagingVO<VirtualInventoryDTO.ListDetailDTO> detailPaging(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto);
    /**
     * 库存差异明细导出
     * @author will
     * @date 2024/6/3 17:17
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(VirtualInventoryDTO.SearchParamDTO dto, HttpServletResponse response);
}
