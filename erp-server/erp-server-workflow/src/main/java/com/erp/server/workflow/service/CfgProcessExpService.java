package com.erp.server.workflow.service;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.entity.CfgProcessExpEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgProcessExpDTO;

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

    void delete(List<String> ids);

    List<CfgProcessExpDTO.ViewDTO> view(String ruleId);
}
