package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgConditionDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 条件配置表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-04-22
 */
public interface CfgConditionService extends SuperService<CfgConditionEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-04-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-04-22
    * @param dto
    * @return
    */
    Boolean update(CfgConditionDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-04-22
    * @param pagingParamDTO
    * @return PagingVO<CfgConditionDTO.ListDTO>>
    */
    PagingVO<CfgConditionDTO.ListDTO> paging(PagingDTO<CfgConditionDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-04-22
    * @param dto
    * @return List<CfgConditionDTO.TabListDTO>>
    */
    List<CfgConditionDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-04-22
    * @param id
    * @return
    */
    CfgConditionDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-04-22
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgConditionDTO.ExportDTO dto, HttpServletResponse response);
}
