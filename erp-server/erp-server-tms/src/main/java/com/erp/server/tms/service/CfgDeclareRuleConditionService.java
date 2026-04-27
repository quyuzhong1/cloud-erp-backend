package com.erp.server.tms.service;
import com.erp.model.tms.entity.CfgDeclareRuleConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.CfgDeclareRuleConditionDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 报关规则条件表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-04-20
 */
public interface CfgDeclareRuleConditionService extends SuperService<CfgDeclareRuleConditionEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgDeclareRuleConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @return
    */
    Boolean update(CfgDeclareRuleConditionDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-04-20
    * @param pagingParamDTO
    * @return PagingVO<CfgDeclareRuleConditionDTO.ListDTO>>
    */
    PagingVO<CfgDeclareRuleConditionDTO.ListDTO> paging(PagingDTO<CfgDeclareRuleConditionDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @return List<CfgDeclareRuleConditionDTO.TabListDTO>>
    */
    List<CfgDeclareRuleConditionDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-04-20
    * @param id
    * @return
    */
    CfgDeclareRuleConditionDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author jack
    * @date: 2026-04-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(CfgDeclareRuleConditionDTO.ExportDTO dto, HttpServletResponse response);
}
