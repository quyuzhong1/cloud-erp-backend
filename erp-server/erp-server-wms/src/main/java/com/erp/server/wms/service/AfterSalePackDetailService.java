package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AfterSalePackDetailDTO;
import com.erp.model.wms.entity.AfterSalePackDetailEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 售后装箱明细表 服务类
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
public interface AfterSalePackDetailService extends SuperService<AfterSalePackDetailEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    BaseResultDTO.AddDTO add(AfterSalePackDetailDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    Boolean update(AfterSalePackDetailDTO.UpdateDTO dto);


    /**
     * 分页列表查询
     *
     * @param pagingParamDTO
     * @return PagingVO<AfterSalePackingDetailDTO.ListDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    PagingVO<AfterSalePackDetailDTO.ListDTO> paging(PagingDTO<AfterSalePackDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    AfterSalePackDetailDTO.ViewDTO view(String id);


    /**
     * 导出Excel
     *
     * @param dto
     * @param response
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    void exportList(AfterSalePackDetailDTO.ExportDTO dto, HttpServletResponse response);
}
