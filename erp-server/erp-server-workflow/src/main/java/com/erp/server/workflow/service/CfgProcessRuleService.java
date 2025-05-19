package com.erp.server.workflow.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.workflow.dto.CfgProcessRuleDTO;
import com.erp.model.workflow.entity.CfgProcessRuleEntity;

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
    /**
     * 根据流程定义id查询规则
     * @author will
     * @date 2025/5/16 10:06
     * @param id
     * @return CfgProcessRuleEntity
     */
    CfgProcessRuleEntity getByDefinitionId(String id);
    /**
     * 根据流程id查询
     * @author will
     * @date 2025/5/19 15:34
     * @param id
     * @param type
     * @return CfgProcessRuleEntity
     */
    List<CfgProcessRuleEntity> listByProcessId(String id,String type);
}
