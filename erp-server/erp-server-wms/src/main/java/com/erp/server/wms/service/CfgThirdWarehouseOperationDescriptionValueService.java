package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgThirdWarehouseOperationDescriptionValueEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgThirdWarehouseOperationDescriptionValueDTO;
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
public interface CfgThirdWarehouseOperationDescriptionValueService extends SuperService<CfgThirdWarehouseOperationDescriptionValueEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgThirdWarehouseOperationDescriptionValueDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @return
    */
    Boolean update(CfgThirdWarehouseOperationDescriptionValueDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2026-03-13
    * @param pagingParamDTO
    * @return PagingVO<CfgThirdWarehouseOperationDescriptionValueDTO.ListDTO>>
    */
    PagingVO<CfgThirdWarehouseOperationDescriptionValueDTO.ListDTO> paging(PagingDTO<CfgThirdWarehouseOperationDescriptionValueDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @return List<CfgThirdWarehouseOperationDescriptionValueDTO.TabListDTO>>
    */
    List<CfgThirdWarehouseOperationDescriptionValueDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wtr
    * @date: 2026-03-13
    * @param id
    * @return
    */
    CfgThirdWarehouseOperationDescriptionValueDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author wtr
    * @date: 2026-03-13
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgThirdWarehouseOperationDescriptionValueDTO.ExportDTO dto, HttpServletResponse response);
}
