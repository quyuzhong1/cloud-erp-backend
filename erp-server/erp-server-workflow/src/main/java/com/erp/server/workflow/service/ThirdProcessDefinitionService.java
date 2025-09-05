package com.erp.server.workflow.service;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ThirdProcessDefinitionDTO;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 三方审批定义 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
public interface ThirdProcessDefinitionService extends SuperService<ThirdProcessDefinitionEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdProcessDefinitionDTO.AddDTO dto);

    /**
    * 修改
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(ThirdProcessDefinitionDTO.UpdateDTO dto);


    List<ThirdProcessDefinitionDTO.DropDownDTO> dropDown(String type);

    PagingVO<ThirdProcessDefinitionDTO.ListDTO> paging(PagingDTO<ThirdProcessDefinitionDTO.PagingParamDTO> pagingParamDTO);

    ThirdProcessDefinitionDTO.ViewDTO view(String id);

    BatchResultDTO delete(String ruleId);

    BatchResultDTO enable(String id, @NotNull(message = "启用状态不能为空") Boolean enableStatus);

    void exportList(ThirdProcessDefinitionDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
     * 获取飞书应用ID
     * @author will
     * @date 2025/9/5 10:57
     * @return String
     */
    String getFsAppId();
}
