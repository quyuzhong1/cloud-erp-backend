package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.ThirdProcessInstanceEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ThirdProcessInstanceDTO;

/**
 * <p>
 * 三方流程实例清单 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
public interface ThirdProcessInstanceService extends SuperService<ThirdProcessInstanceEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdProcessInstanceDTO.AddDTO dto);

    /**
    * 修改
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(ThirdProcessInstanceDTO.UpdateDTO dto);


}
