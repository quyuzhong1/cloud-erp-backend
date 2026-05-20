package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgThirdWarehouseOperationDescriptionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgThirdWarehouseOperationDescriptionDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2026-03-13
 */
public interface CfgThirdWarehouseOperationDescriptionService extends SuperService<CfgThirdWarehouseOperationDescriptionEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgThirdWarehouseOperationDescriptionDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @return
    */
    Boolean update(CfgThirdWarehouseOperationDescriptionDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2026-03-13
    * @param pagingParamDTO
    * @return PagingVO<CfgThirdWarehouseOperationDescriptionDTO.ListDTO>>
    */
    PagingVO<CfgThirdWarehouseOperationDescriptionDTO.ListDTO> paging(PagingDTO<CfgThirdWarehouseOperationDescriptionDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @return List<CfgThirdWarehouseOperationDescriptionDTO.TabListDTO>>
    */
    List<CfgThirdWarehouseOperationDescriptionDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wtr
    * @date: 2026-03-13
    * @param id
    * @return
    */
    CfgThirdWarehouseOperationDescriptionDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgThirdWarehouseOperationDescriptionDTO.ExportDTO dto, HttpServletResponse response);
}
