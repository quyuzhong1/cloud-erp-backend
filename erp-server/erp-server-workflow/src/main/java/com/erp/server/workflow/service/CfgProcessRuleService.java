package com.erp.server.workflow.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.workflow.entity.CfgProcessRuleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgProcessRuleDTO;

import java.util.List;

/**
 * <p>
 * 流程设置执行条件 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-13
 */
public interface CfgProcessRuleService extends SuperService<CfgProcessRuleEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author hcg
     * @date: 2025-05-13
     */
    BaseResultDTO.AddDTO add(String bussinessKey, String id, List<CfgProcessRuleDTO.AddOrUpdateDTO> dto);

    /**
     * 更新
     *
     * @param dto
     * @return
     * @author hcg
     * @date: 2025-05-13
     */
    BaseResultDTO.UpdateDTO update(String bussinessKey, String id, List<CfgProcessRuleDTO.AddOrUpdateDTO> dto);

    /**
     * 删除
     *
     * @param ids
     * @return
     * @author hcg
     * @date: 2025-05-13
     */
    void delete(List<String> ids);

    BaseResultDTO.UpdateDTO updateDefault(CfgProcessRuleDTO.UpdateStateDTO dto);
}
