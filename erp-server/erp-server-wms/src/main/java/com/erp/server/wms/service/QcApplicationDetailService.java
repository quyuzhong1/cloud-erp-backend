package com.erp.server.wms.service;
import com.erp.model.wms.entity.QcApplicationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 质检申请单明细表 服务类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
public interface QcApplicationDetailService extends SuperService<QcApplicationDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(QcApplicationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return
    */
    Boolean update(QcApplicationDetailDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author will
    * @date: 2026-03-20
    * @param pagingParamDTO
    * @return PagingVO<QcApplicationDetailDTO.ListDTO>>
    */
    PagingVO<QcApplicationDetailDTO.ListDTO> paging(PagingDTO<QcApplicationDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return List<QcApplicationDetailDTO.TabListDTO>>
    */
    List<QcApplicationDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2026-03-20
    * @param id
    * @return
    */
    QcApplicationDetailDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(QcApplicationDetailDTO.ExportDTO dto, HttpServletResponse response);
}
