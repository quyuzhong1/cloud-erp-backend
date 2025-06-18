package com.erp.server.workflow.service;

import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;

import java.util.List;

/**
 * <p>
 * 流程设置字段配置 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
public interface CfgProcessFieldMapService extends SuperService<CfgProcessFieldMapEntity> {

    /**
     * 新增
     *
     * @param addDTO
     * @return
     * @author hcg
     * @date: 2025-05-12
     */
    BaseResultDTO.AddDTO add(String bussinessKey, String cfgProcessId, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> addDTO, String processDefinitionId, String type);

    /**
     * 修改
     *
     * @param addDTO
     * @return
     * @author hcg
     * @date: 2025-05-12
     */
    BaseResultDTO.AddDTO addOrUpdate(String bussinessKey, String cfgProcessId, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> addDTO, String processDefinitionId, String type);

    /**
     * 字段映射详情接口：初次调用解析飞书FormJson
     * @author hcg
     * @date: 2025-05-12
     * @param ruleId
     * @return
     */
    List<CfgProcessFieldMapDTO.ViewDTO> view(String ruleId,String type);

    /**
     * 删除
     * @author hcg
     * @date: 2025-05-12
     * @param ids
     * @return
     */
    void delete(List<String> ids);
}
