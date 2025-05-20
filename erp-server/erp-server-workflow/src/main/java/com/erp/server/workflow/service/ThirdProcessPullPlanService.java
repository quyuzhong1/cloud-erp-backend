package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.ThirdProcessPullPlanEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.ThirdProcessPullPlanDTO;

/**
 * <p>
 * 三方流程实例拉取任务 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
public interface ThirdProcessPullPlanService extends SuperService<ThirdProcessPullPlanEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdProcessPullPlanDTO.AddDTO dto);

    /**
    * 修改
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(ThirdProcessPullPlanDTO.UpdateDTO dto);


}
