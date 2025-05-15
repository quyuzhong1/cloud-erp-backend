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
     * @param dto
     * @return
     * @author hcg
     * @date: 2025-05-12
     */
    BaseResultDTO.AddDTO add(String bussinessKey, String cfgProcessId, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author hcg
     * @date: 2025-05-12
     */
    BaseResultDTO.AddDTO addOrUpdate(String bussinessKey, String cfgProcessId, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> dto);


    List<CfgProcessFieldMapDTO.ViewDTO> view(String ruleId);

    void delete(List<String> ids);
}
