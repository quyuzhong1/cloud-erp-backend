package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ThirdProcessDefinitionDTO;

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


    List<ThirdProcessDefinitionDTO.DropDownDTO> dropDown();
}
