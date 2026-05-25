package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbsInventoryDTO;
import com.erp.model.wms.entity.FbsInventoryEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * FBS库存 服务类
 * </p>
 *
 * @author Cursor
 * @since 2026-05-25
 */
public interface FbsInventoryService extends SuperService<FbsInventoryEntity> {

    /**
     * 分页列表查询
     *
     * @param pagingParamDTO 分页查询参数
     * @return 分页数据
     */
    PagingVO<FbsInventoryDTO.ListDTO> paging(PagingDTO<FbsInventoryDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 导出Excel
     *
     * @param dto 导出参数
     * @param response response
     */
    void exportList(FbsInventoryDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 列表汇总数量
     *
     * @param pagingParamDTO 分页查询参数
     * @return 汇总数量
     */
    FbsInventoryDTO.SummaryNumber summaryNumber(PagingDTO<FbsInventoryDTO.PagingParamDTO> pagingParamDTO);
}
