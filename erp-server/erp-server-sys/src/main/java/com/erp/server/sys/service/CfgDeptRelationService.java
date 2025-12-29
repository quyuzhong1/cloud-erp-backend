package com.erp.server.sys.service;
import com.erp.model.sys.entity.CfgDeptRelationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.CfgDeptRelationDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 部门关联表 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-12-29
 */
public interface CfgDeptRelationService extends SuperService<CfgDeptRelationEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-12-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgDeptRelationDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-12-29
    * @param dto
    * @return
    */
    Boolean update(CfgDeptRelationDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author lrp
    * @date: 2025-12-29
    * @param pagingParamDTO
    * @return PagingVO<CfgDeptRelationDTO.ListDTO>>
    */
    PagingVO<CfgDeptRelationDTO.ListDTO> paging(PagingDTO<CfgDeptRelationDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author lrp
    * @date: 2025-12-29
    * @param dto
    * @return List<CfgDeptRelationDTO.TabListDTO>>
    */
    List<CfgDeptRelationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author lrp
    * @date: 2025-12-29
    * @param id
    * @return
    */
    CfgDeptRelationDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author lrp
    * @date: 2025-12-29
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgDeptRelationDTO.ExportDTO dto, HttpServletResponse response);
}
