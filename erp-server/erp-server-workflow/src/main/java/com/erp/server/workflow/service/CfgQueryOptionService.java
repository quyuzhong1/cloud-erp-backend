package com.erp.server.workflow.service;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;

import java.util.List;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
 */
public interface CfgQueryOptionService extends SuperService<CfgQueryOptionEntity> {


    /**
    * 新增
    * @author hcg
    * @date: 2025-05-15
    * @param
    * @return
    */
    List<CfgQueryOptionDTO.ListDTO> proDropDown(String bussinessKey);

    List<CfgQueryOptionDTO.cfgApproveSyncDropDownDTO> cfgApproveSyncDropDown(String bussinessKey);

    List<CfgQueryOptionDTO.TreeDTO> tree(String bussinessKey);

}
