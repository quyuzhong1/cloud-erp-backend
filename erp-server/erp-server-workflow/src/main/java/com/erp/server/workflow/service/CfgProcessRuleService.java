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
    * @author hcg
    * @date: 2025-05-13
    * @param dto
    * @return
    */
//    BaseResultDTO.AddDTO addOrUpdate(List<CfgProcessRuleDTO.AddOrUpdateDTO> dto);
    BaseResultDTO.AddDTO addOrUpdate(String bussinessKey,String id,List<CfgProcessRuleDTO.AddOrUpdateDTO> dto);
    /**
    * 删除
    * @author hcg
    * @date: 2025-05-13
    * @param ids
    * @return
    */
    void delete(List<String> ids);
}
