package com.erp.server.workflow.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.workflow.dto.CfgProcessExpDTO;
import com.erp.model.workflow.entity.CfgProcessExpEntity;

import java.util.List;

/**
 * <p>
 * 流程设置审核条件 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
public interface CfgProcessExpService extends SuperService<CfgProcessExpEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(String cfgProcessId,String ruleId,List<CfgProcessExpDTO.AddOrUpdateDTO> dto);
    /**
    * 修改
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.UpdateDTO addOrUpdate(String cfgProcessId,String ruleId,List<CfgProcessExpDTO.AddOrUpdateDTO> dto);

    /**
     * 删除
     * @author hcg
     * @date: 2025-05-12
     * @param ids
     * @return
     */
    void delete(List<String> ids);

    /**
     * 高级查询
     * @author hcg
     * @date: 2025-05-12
     * @param
     * @return
     */
    List<CfgProcessExpDTO.ViewDTO> view(String ruleId);
    /**
     * 根据规则id集合查询条件设置
     * @author will
     * @date 2025/5/19 16:24
     * @param ruleIdList
     * @return List<CfgProcessExpEntity>
     */
    List<CfgProcessExpEntity> listByRuleIdList(List<String> ruleIdList);
}
