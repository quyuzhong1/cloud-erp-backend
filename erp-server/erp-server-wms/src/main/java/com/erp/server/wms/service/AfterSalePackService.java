package com.erp.server.wms.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.entity.AfterSalePackEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 售后装箱表 服务类
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
public interface AfterSalePackService extends SuperService<AfterSalePackEntity> {

    /**
     * 申请箱唛
     *
     * @param dto AfterSalePackDTO.BoxCodeApplicationDTO
     * @return List<String>
     * @author lei.nie
     * @date: 2026-05-12
     */
    List<String> boxCodeApplication(AfterSalePackDTO.BoxCodeApplicationDTO dto);

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    BaseResultDTO.AddDTO add(AfterSalePackDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    Boolean update(AfterSalePackDTO.UpdateDTO dto);


    /**
     * 分页列表查询
     *
     * @param pagingParamDTO
     * @return PagingVO<AfterSalePackDTO.ListDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    PagingVO<AfterSalePackDTO.ListDTO> paging(PagingDTO<AfterSalePackDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    AfterSalePackDTO.ViewDTO view(String id);


    /**
     * 导出Excel
     *
     * @param dto
     * @param response
     * @return
     * @author lei.nie
     * @date: 2026-05-12
     */
    void exportList(AfterSalePackDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 删除
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return List<BatchResultDTO>
     * @author lei.nie
     * @date: 2026-05-12
     */
    List<BatchResultDTO> delete(BaseIdsDTO.IdsDTO dto);

    /**
     * 根据code查询详情
     *
     * @param code String
     * @return AfterSalePackDTO.ViewDTO
     * @author lei.nie
     * @date: 2026-05-12
     */
    AfterSalePackDTO.ViewDTO viewByCode(String code);
}
