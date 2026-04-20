package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 报关规则主表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-04-20
 */
public interface CfgDeclareRuleService extends SuperService<CfgDeclareRuleEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgDeclareRuleDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @return
    */
    Boolean update(CfgDeclareRuleDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-04-20
    * @param pagingParamDTO
    * @return PagingVO<CfgDeclareRuleDTO.ListDTO>>
    */
    PagingVO<CfgDeclareRuleDTO.ListDTO> paging(PagingDTO<CfgDeclareRuleDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @return List<CfgDeclareRuleDTO.TabListDTO>>
    */
    List<CfgDeclareRuleDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-04-20
    * @param id
    * @return
    */
    CfgDeclareRuleDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgDeclareRuleDTO.ExportDTO dto, HttpServletResponse response);
}
